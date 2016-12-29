package org.bocogop.wr.service.impl;

import static org.bocogop.wr.model.notification.NotificationLinkType.VOLUNTEER_PROFILE;
import static org.bocogop.wr.model.requirement.RequirementStatus.RequirementStatusValue.MET;
import static java.time.temporal.ChronoUnit.DAYS;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.bocogop.shared.model.Permission.PermissionType;
import org.bocogop.shared.model.Role;
import org.bocogop.shared.model.Role.RoleType;
import org.bocogop.shared.service.validation.ServiceValidationException;
import org.bocogop.shared.util.SecurityUtil;
import org.bocogop.wr.model.facility.Facility;
import org.bocogop.wr.model.notification.Notification;
import org.bocogop.wr.model.notification.NotificationSeverityType;
import org.bocogop.wr.model.notification.NotificationType;
import org.bocogop.wr.model.requirement.VolunteerRequirement;
import org.bocogop.wr.model.voluntaryService.VoluntaryServiceStaff;
import org.bocogop.wr.service.NotificationService;
import org.bocogop.wr.service.email.EmailService;
import org.bocogop.wr.util.DateUtil;

@Service
public class NotificationServiceImpl extends AbstractServiceImpl implements NotificationService {
	private static final Logger log = LoggerFactory.getLogger(NotificationServiceImpl.class);

	@Value("${notification.maxResults}")
	int maxResults;
	@Autowired
	private EmailService emailService;

	@Override
	public Notification saveOrUpdate(Notification notification) {
		boolean isNew = !notification.isPersistent();
		notification = notificationDAO.saveOrUpdate(notification);

		if (isNew && notification.getSeverity() == NotificationSeverityType.HIGH) {
			List<VoluntaryServiceStaff> allStaff = voluntaryServiceStaffDAO.findLinkedToNotification(notification);
			for (VoluntaryServiceStaff s : allStaff) {
				if (!s.isEmailNotifications())
					continue;

				String email = s.getAppUser().getEmail();
				if (StringUtils.isNotBlank(email)) {
					try {
						emailService.sendEmail(notification.getName(), notification.getDescription(),
								new String[] { email }, null);
					} catch (Exception e) {
						log.error("There was an error sending an email to " + email, e);
					}
				} else {
					log.error(
							"The voluntary service staff member {} registered for notifications but does not have a valid email on file.",
							s.getAppUser());
				}
			}
		}

		return notification;
	}

	@Override
	public void delete(long notificationId) throws ServiceValidationException {
		Notification n = notificationDAO.findRequiredByPrimaryKey(notificationId);
		if (!n.isClearable())
			throw new ServiceValidationException("notification.error.notificationNotClearable");
		notificationDAO.delete(notificationId);
	}

	@Override
	@Transactional(readOnly = true)
	public NotificationSearchResult getNotificationsForFacility(long facilityId) {
		LocalDate today = getTodayAtFacility();

		boolean isNationalAdmin = getCurrentUser().isNationalAdmin();
		boolean isFacilityAdmin = SecurityUtil
				.hasAnyPermissionAtCurrentFacility(PermissionType.REQUIREMENTS_LOCAL_MANAGE);
		if (!isNationalAdmin && !isFacilityAdmin)
			return new NotificationSearchResult(false, new ArrayList<>());

		List<Notification> notifications = new ArrayList<>();
		if (isNationalAdmin || isFacilityAdmin) {
			// boolean implied by above but just including for clarity
			notifications.addAll(notificationDAO.findByUserAndFacility(today, getCurrentUser().getId(), facilityId));
		}

		if (isFacilityAdmin) {
			List<VolunteerRequirement> volReqs = volunteerRequirementDAO
					.findForExpiringRequirementsByFacility(facilityId, maxResults + 1);
			Facility facility = facilityDAO.findRequiredByPrimaryKey(facilityId);
			Role facilityAdminRole = roleDAO.findByLookup(RoleType.SITE_ADMINISTRATOR);
			ZoneId facilityTimeZone = getFacilityTimeZone();

			for (VolunteerRequirement vr : volReqs) {
				String requirementName = vr.getRequirement().getName();
				long volunteerRequirementId = vr.getId();
				LocalDate vrDate = vr.getRequirementDate();

				LocalDate beginDate = today;
				String severity = null;
				boolean dateTypeHasNotification = !vr.getRequirement().getDateType().isSkipNotification();

				if (vrDate != null && dateTypeHasNotification && !today.isBefore(vrDate)) {
					severity = "expired";
					beginDate = vrDate;
				} else {
					if (vr.getStatus().getLookupType() != MET) {
						severity = "unmet";
						beginDate = vr.getCreatedDate().withZoneSameInstant(facilityTimeZone).toLocalDate();
					} else if (vrDate != null) {
						Integer daysNotification = vr.getRequirement().getDaysNotification();
						if (dateTypeHasNotification && daysNotification != null
								&& DAYS.between(today, vrDate) <= daysNotification) {
							severity = "warning";
							beginDate = vrDate.minusDays(daysNotification);
						}
					}
				}

				String volRequirementStatus = vr.getStatus().getName();

				LocalDate expirationDate = vrDate;
				Integer daysUntilExpiration = expirationDate != null ? (int) DAYS.between(today, vrDate) : null;

				String name = null;
				String description = null;
				NotificationSeverityType severityType = NotificationSeverityType.LOW;
				if ("unmet".equals(severity)) {
					name = "Requirement Unmet";
					description = "The requirement \"" + requirementName + "\" is unmet (currently \""
							+ volRequirementStatus + "\")";
					severityType = NotificationSeverityType.MEDIUM;
				} else if ("expired".equals(severity)) {
					name = "Requirement Expired";
					description = "The requirement \"" + requirementName + "\" expired on "
							+ expirationDate.format(DateUtil.DATE_ONLY_FORMAT) + " and is past due";
					severityType = NotificationSeverityType.MEDIUM;
				} else if ("warning".equals(severity)) {
					name = "Requirement Nearing Expiration";
					description = "The requirement \"" + requirementName + "\" expires in " + daysUntilExpiration
							+ " days on " + expirationDate.format(DateUtil.DATE_ONLY_FORMAT);
				}

				if (name != null && description != null) {
					Notification n = new Notification(name, description, severityType, NotificationType.SYSTEM,
							beginDate, null, null, facility, false, VOLUNTEER_PROFILE).withRefVolunteerRequirement(vr)
									.withTargetRole(facilityAdminRole).withTargetFacility(facility)
									.setUniqueIdentifier("ExpiringReq" + volunteerRequirementId);

					notifications.add(n);
				}
			}
		}
		Collections.sort(notifications);
		boolean hitMaxResults = false;
		if (notifications.size() > maxResults) {
			notifications = notifications.subList(0, maxResults);
			hitMaxResults = true;
		}
		return new NotificationSearchResult(hitMaxResults, notifications);
	}

	@Override
	public int purgeExpiredNotifications() {
		return notificationDAO.purgeExpiredNotifications();
	}

}
