package org.bocogop.wr.service.impl;

import static org.bocogop.shared.model.Permission.PermissionType.VOLUNTEER_SELF_SERVICE_NOTIFICATION_VIEW;
import static org.bocogop.shared.model.Role.RoleType.NATIONAL_ADMIN;
import static org.bocogop.shared.model.Role.RoleType.SITE_ADMINISTRATOR;
import static org.bocogop.shared.util.StringUtil.normalizeLineBreaks;
import static org.bocogop.wr.model.notification.NotificationLinkType.LEIE_REPORT;
import static org.bocogop.wr.model.notification.NotificationLinkType.VOLUNTEER_AUDIT_COMPARE;
import static org.bocogop.wr.model.notification.NotificationLinkType.VOLUNTEER_PROFILE;
import static org.bocogop.wr.model.notification.NotificationSeverityType.HIGH;
import static org.bocogop.wr.model.notification.NotificationType.LEIE;
import static org.bocogop.wr.model.volunteer.VolunteerStatus.VolunteerStatusType.ACTIVE;
import static org.bocogop.wr.model.volunteer.VolunteerStatus.VolunteerStatusType.INACTIVE;
import static org.bocogop.wr.model.volunteer.VolunteerStatus.VolunteerStatusType.TERMINATED;
import static org.bocogop.wr.model.volunteer.VolunteerStatus.VolunteerStatusType.TERMINATED_WITH_CAUSE;
import static org.apache.commons.lang.WordUtils.capitalizeFully;
import static org.apache.commons.lang3.StringUtils.equalsIgnoreCase;
import static org.apache.commons.lang3.StringUtils.trim;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import org.bocogop.shared.model.AppUser;
import org.bocogop.shared.model.CoreUserDetails;
import org.bocogop.shared.model.Permission;
import org.bocogop.shared.model.Permission.PermissionType;
import org.bocogop.shared.service.validation.ServiceValidationException;
import org.bocogop.shared.util.SecurityUtil;
import org.bocogop.wr.model.benefitingService.BenefitingService;
import org.bocogop.wr.model.benefitingService.BenefitingServiceRole;
import org.bocogop.wr.model.facility.Facility;
import org.bocogop.wr.model.leie.ExcludedEntity;
import org.bocogop.wr.model.lookup.Language;
import org.bocogop.wr.model.lookup.Language.LanguageType;
import org.bocogop.wr.model.notification.Notification;
import org.bocogop.wr.model.notification.NotificationSeverityType;
import org.bocogop.wr.model.notification.NotificationType;
import org.bocogop.wr.model.organization.AbstractBasicOrganization;
import org.bocogop.wr.model.organization.ScopeType;
import org.bocogop.wr.model.time.WorkEntry;
import org.bocogop.wr.model.volunteer.AvailableIdentifyingCode;
import org.bocogop.wr.model.volunteer.Volunteer;
import org.bocogop.wr.model.volunteer.VolunteerAssignment;
import org.bocogop.wr.model.volunteer.VolunteerOrganization;
import org.bocogop.wr.model.volunteer.VolunteerStatus;
import org.bocogop.wr.model.volunteer.VolunteerStatus.VolunteerStatusType;
import org.bocogop.wr.persistence.dao.leie.ExcludedEntityMatch;
import org.bocogop.wr.service.NotificationService;
import org.bocogop.wr.service.volunteer.VolunteerService;
import org.bocogop.wr.util.DateUtil;
import org.bocogop.wr.util.context.SessionUtil;

@Service
public class VolunteerServiceImpl extends AbstractServiceImpl implements VolunteerService {
	private static final Logger log = LoggerFactory.getLogger(VolunteerServiceImpl.class);

	@Value("${maxIdleDaysBeforeVolunteerInactivation}")
	private int maxIdleDaysBeforeVolunteerInactivation;
	@Value("${volunteerInactivationGracePeriod}")
	private int volunteerInactivationGracePeriod;
	@Value("${notification.volunteerSelfService.expirationDaysOut}")
	private int expirationDaysOut;

	@Autowired
	private NotificationService notificationService;

	@Override
	// @PreAuthorize("hasAuthority('" + Permission.VOLUNTEER_CREATE + "')")
	public Volunteer saveOrUpdate(Volunteer vol, boolean createDataChangeNotifications,
			boolean autoTerminateIfLEIEMatch) throws ServiceValidationException {
		if (log.isDebugEnabled())
			log.debug("vol saveOrUpdate ver1=" + vol.getVersion());

		CoreUserDetails userContext = null;

		/*
		 * If the person has LOGIN_KIOSK permission, allow them to proceed only
		 * if they are editing the same volunteer as the logged in context.
		 * Otherwise let the person proceed only if they have VOLUNTEER_CREATE
		 * permission.
		 */
		userContext = getCurrentUser();
		if (userContext instanceof Volunteer
				&& SecurityUtil.hasAllPermissionsAtCurrentFacility(PermissionType.LOGIN_KIOSK)) {
			if (!userContext.getId().equals(vol.getId())) {
				log.warn(userContext.getClass().getSimpleName() + " with ID " + userContext.getId()
						+ " tried to update volunteer " + vol.getId() + " data");
				throw new AccessDeniedException(
						"You do not have proper permission to update or create this volunteer.");
			}
		} else {
			if (!SecurityUtil.hasAllPermissionsAtCurrentFacility(PermissionType.VOLUNTEER_CREATE)) {
				log.warn(userContext.getClass().getSimpleName() + " with ID " + userContext.getId()
						+ " tried to update volunteer " + vol.getId() + " but does not have permission");
				throw new AccessDeniedException(
						"You do not have proper permission to update or create this volunteer.");
			}
		}

		boolean isNew = !vol.isPersistent();
		int fromVer = vol.getVersion();

		/*
		 * Detaching to ensure we get a fresh copy of the vol data from the DB
		 * and it doesn't use what's in the session cache (otherwise the
		 * existingVolunteer == volunteer)- CPB
		 */
		volunteerDAO.detach(vol);
		if (log.isDebugEnabled())
			log.debug("vol saveOrUpdate ver2=" + vol.getVersion());

		Volunteer existingVol = isNew ? null : volunteerDAO.findRequiredByPrimaryKey(vol.getId());
		if (existingVol != null)
			volunteerDAO.detach(existingVol);
		if (log.isDebugEnabled())
			log.debug("vol saveOrUpdate ver3=" + vol.getVersion());

		boolean appendUserToMainRemarks = StringUtils.isNotBlank(vol.getRemarks()) && (isNew
				|| existingVol.getRemarks() == null
				|| !normalizeLineBreaks(existingVol.getRemarks()).equals(normalizeLineBreaks(vol.getRemarks())));
		if (appendUserToMainRemarks)
			vol.setRemarks(vol.getRemarks() + getAuditText(userContext));

		boolean appendUserToMealsRemarks = StringUtils.isNotBlank(vol.getMealRemarks())
				&& (isNew || existingVol.getMealRemarks() == null || !normalizeLineBreaks(existingVol.getMealRemarks())
						.equals(normalizeLineBreaks(vol.getMealRemarks())));
		if (appendUserToMealsRemarks)
			vol.setMealRemarks(vol.getMealRemarks() + getAuditText(userContext));

		boolean appendUserToTerminationRemarks = StringUtils.isNotBlank(vol.getTerminationRemarks())
				&& (isNew || existingVol.getTerminationRemarks() == null
						|| !normalizeLineBreaks(existingVol.getTerminationRemarks())
								.equals(normalizeLineBreaks(vol.getTerminationRemarks())));
		if (appendUserToTerminationRemarks)
			vol.setTerminationRemarks(vol.getTerminationRemarks() + getAuditText(userContext));

		if (isNew) {
			vol.setEntryDate(getTodayAtFacility());
			vol.setOriginallyCreatedAt(getRequiredFacilityContext());
			vol.setPreferredLanguage(languageDAO.findByLookup(LanguageType.ENGLISH));
			AvailableIdentifyingCode c = availableIdentifyingCodeDAO.getFirstUnused();
			vol.setIdentifyingCode(c.getCode());
			availableIdentifyingCodeDAO.delete(c);
		}
		vol.setFirstName(trim(capitalizeFully(vol.getFirstName())));
		vol.setMiddleName(trim(capitalizeFully(vol.getMiddleName())));
		vol.setLastName(trim(capitalizeFully(vol.getLastName())));
		vol.setSuffix(trim(capitalizeFully(vol.getSuffix())));
		vol.setZip(trim(vol.getZip()));

		vol = volunteerDAO.saveOrUpdate(vol);
		if (log.isDebugEnabled())
			log.debug("vol saveOrUpdate ver4=" + vol.getVersion());

		if (isNew) {
			/* Set the primary facility and initial status */
			volunteerDAO.updateFieldsWithoutVersionIncrement(vol.getId(), false, null, true,
					getRequiredFacilityContext().getId(), VolunteerStatusType.ACTIVE, getTodayAtFacility(), null);
		} else {
			if (vol.getStatus().isVolunteerTerminated()) {
				if (!existingVol.getStatus().isVolunteerTerminated()
						|| vol.getStatus().getLookupType() != existingVol.getStatus().getLookupType())
					// reuse terminate method for consistency; no need to append
					// remarks since we handled that above
					terminateVolunteer(vol.getId(), vol.getStatus().getLookupType() == TERMINATED_WITH_CAUSE, null);
			} else {
				volunteerDAO.updateFieldsWithoutVersionIncrement(vol.getId(), false, null, false, null,
						vol.getStatus().getLookupType(), vol.getStatusDate(), null);
				if (log.isDebugEnabled())
					log.debug("vol saveOrUpdate ver5=" + vol.getVersion());

				if (!vol.getStatus().isVolunteerActive())
					inactiveAllAssignmentsAndOrgs(vol);
				vol = updateVolunteerStatusBasedOnNewAssignments(vol).updatedVolunteer;
			}
		}

		volunteerDAO.flushAndRefresh(vol);
		vol = syncWithLEIE(vol, existingVol, autoTerminateIfLEIEMatch);

		if (!isNew && createDataChangeNotifications) {
			volunteerDAO.flushAndRefresh(vol);
			int toVer = vol.getVersion();
			createDataChangeNotifications(vol, existingVol, fromVer, toVer,
					permissionDAO.findByLookup(VOLUNTEER_SELF_SERVICE_NOTIFICATION_VIEW));
		}
		return vol;
	}

	private void createDataChangeNotifications(Volunteer newV, Volunteer oldV, int fromVer, int toVer,
			Permission perm) {
		boolean validDataChanged = false;
		// hardcode criteria that we're interested in, for now - CPB
		validDataChanged |= !equalsIgnoreCase(trim(newV.getFirstName()), trim(oldV.getFirstName()));
		validDataChanged |= !equalsIgnoreCase(trim(newV.getNickname()), trim(oldV.getNickname()));
		validDataChanged |= !equalsIgnoreCase(trim(newV.getMiddleName()), trim(oldV.getMiddleName()));
		validDataChanged |= !equalsIgnoreCase(trim(newV.getLastName()), trim(oldV.getLastName()));
		validDataChanged |= !equalsIgnoreCase(trim(newV.getSuffix()), trim(oldV.getSuffix()));
		validDataChanged |= !Objects.equals(newV.getDateOfBirth(), oldV.getDateOfBirth());
		validDataChanged |= !Objects.equals(newV.getGender(), oldV.getGender());
		validDataChanged |= !equalsIgnoreCase(trim(newV.getAddressLine1()), trim(oldV.getAddressLine1()));
		validDataChanged |= !equalsIgnoreCase(trim(newV.getAddressLine2()), trim(oldV.getAddressLine2()));
		validDataChanged |= !equalsIgnoreCase(trim(newV.getCity()), trim(oldV.getCity()));
		validDataChanged |= !Objects.equals(newV.getState(), oldV.getState());
		validDataChanged |= !equalsIgnoreCase(trim(newV.getZip()), trim(oldV.getZip()));
		validDataChanged |= !equalsIgnoreCase(trim(newV.getEmail()), trim(oldV.getEmail()));
		validDataChanged |= !equalsIgnoreCase(trim(newV.getPhone()), trim(oldV.getPhone()));
		validDataChanged |= !equalsIgnoreCase(trim(newV.getPhoneAlt()), trim(oldV.getPhoneAlt()));
		validDataChanged |= !equalsIgnoreCase(trim(newV.getPhoneAlt2()), trim(oldV.getPhoneAlt2()));
		validDataChanged |= !equalsIgnoreCase(trim(newV.getEmergencyContactName()),
				trim(oldV.getEmergencyContactName()));
		validDataChanged |= !equalsIgnoreCase(trim(newV.getEmergencyContactRelationship()),
				trim(oldV.getEmergencyContactRelationship()));
		validDataChanged |= !equalsIgnoreCase(trim(newV.getEmergencyContactPhone()),
				trim(oldV.getEmergencyContactPhone()));
		validDataChanged |= !equalsIgnoreCase(trim(newV.getEmergencyContactPhoneAlt()),
				trim(oldV.getEmergencyContactPhoneAlt()));

		if (!validDataChanged)
			return;

		newV.getAssignmentsByStatus(true).stream().map(p -> p.getFacility().getFacility()).distinct().forEach(f -> {
			Notification n = new Notification("Volunteer Self Service",
					"Volunteer has updated their profile. Click to view a list of updated fields.",
					NotificationSeverityType.LOW, NotificationType.SYSTEM, LocalDate.now(),
					LocalDate.now().plusDays(expirationDaysOut), null, SessionUtil.getFacilityContext(), true,
					VOLUNTEER_AUDIT_COMPARE, VOLUNTEER_PROFILE) //
							.withRefVolunteer(newV) //
							.withRefAuditFromVersion(fromVer) //
							.withRefAuditToVersion(toVer) //
							.withTargetPermission(perm) //
							.withTargetFacility(f);
			n = notificationService.saveOrUpdate(n);
		});
	}

	public void inactiveAllAssignmentsAndOrgs(Volunteer v) {
		for (VolunteerAssignment va : v.getVolunteerAssignments()) {
			if (va.isActive()) {
				va.setInactive(true);
				va = volunteerAssignmentDAO.saveOrUpdate(va);
			}
		}
		for (VolunteerOrganization vo : v.getVolunteerOrganizations()) {
			if (vo.isActive()) {
				vo.setInactive(true);
				vo = volunteerOrganizationDAO.saveOrUpdate(vo);
			}
		}
	}

	private Volunteer syncWithLEIE(Volunteer vol, Volunteer existingVol, boolean autoTerminateIfLEIEMatch)
			throws ServiceValidationException {
		boolean isNew = existingVol == null;

		// TODO externalize to messages - CPB
		LEIETerminationParams terminationParams = new LEIETerminationParams("Excluded Individual Found (Self Service)",
				"The volunteer \"" + vol.getDisplayName()
						+ "\" updated their profile and an LEIE match was found. This volunteer was terminated and needs review.",
				"Excluded Individual Found (Self Service)", "The volunteer \"" + vol.getDisplayName()
						+ "\" updated their profile and an LEIE match was found. This volunteer was terminated and needs review.");

		if (isNew) {
			List<ExcludedEntityMatch> excludedEntitiesForVolunteer = excludedEntityDAO
					.findExcludedEntitiesForVolunteer(vol.getId(), null);
			if (!excludedEntitiesForVolunteer.isEmpty()) {
				if (autoTerminateIfLEIEMatch) {
					vol = terminateVolunteerForLEIEMatch(excludedEntitiesForVolunteer.get(0), terminationParams);
				} else {
					/*
					 * Assume that we prompted the user in the UI about this
					 * match, and they agreed to continue to create the
					 * volunteer.
					 */
					vol.setLeieApprovalOverride(true);
					vol.setLeieExclusionDate(
							excludedEntitiesForVolunteer.get(0).getExcludedEntity().getExclusionDate());
					vol = volunteerDAO.saveOrUpdate(vol);

					addNotificationsForLEIEExclusion(vol);
				}
			}
		} else {
			if (autoTerminateIfLEIEMatch) {
				List<ExcludedEntityMatch> excludedEntitiesForVolunteer = excludedEntityDAO
						.findExcludedEntitiesForVolunteer(vol.getId(), null);
				if (!excludedEntitiesForVolunteer.isEmpty()) {
					vol = terminateVolunteerForLEIEMatch(excludedEntitiesForVolunteer.get(0), terminationParams);
				}
			} else {
				// delegate to nightly job to find match and auto-terminate
				if (existingVol.getLeieExclusionDate() != null) {
					boolean clearLEIEFields = !existingVol.getStatus().isVolunteerTerminated()
							&& vol.getStatus().isVolunteerTerminated();
					clearLEIEFields |= !existingVol.getFirstName().equalsIgnoreCase(vol.getFirstName());
					clearLEIEFields |= !StringUtils.equalsIgnoreCase(existingVol.getMiddleName(), vol.getMiddleName());
					clearLEIEFields |= !existingVol.getLastName().equalsIgnoreCase(vol.getLastName());
					clearLEIEFields |= !existingVol.getDateOfBirth().equals(vol.getDateOfBirth());

					if (clearLEIEFields) {
						vol.setLeieExclusionDate(null);
						vol.setLeieApprovalOverride(false);
						vol = volunteerDAO.saveOrUpdate(vol);
						removeLEIENotifications(vol.getId());
					} else {
						if (!existingVol.isLeieApprovalOverride() && vol.isLeieApprovalOverride()) {
							/*
							 * let's remove outstanding notifications before
							 * adding new ones in case they submit several times
							 * in a row and flip-flop the flag - CPB
							 */
							removeLEIENotifications(vol.getId());
							addNotificationsForLEIEExclusion(vol);
						}
					}
				}
			}
		}

		return vol;
	}

	public void removeLEIENotifications(Long volunteerId) throws ServiceValidationException {
		List<Notification> existingNotifications = notificationDAO.findByCriteria(NotificationSeverityType.HIGH,
				NotificationType.LEIE, null, false, null, false, null, false, null, true, volunteerId);
		for (Notification n : existingNotifications)
			notificationService.delete(n.getId());
	}

	private void addNotificationsForLEIEExclusion(Volunteer v) throws ServiceValidationException {
		Facility volFac = v.getPrimaryOrOriginallyCreatedAtFacility();

		Notification n = new Notification("Excluded Individual found",
				"The volunteer \"" + v.getDisplayName() + "\" (identifying code " + v.getIdentifyingCode()
						+ ") matches an LEIE excluded individual but was saved as a false positive.",
				HIGH, LEIE, getTodayAtFacility(), null, getCurrentUserAsOrNull(AppUser.class), getFacilityContext(),
				true, VOLUNTEER_PROFILE, LEIE_REPORT).withRefVolunteer(v)
						.withTargetRole(roleDAO.findByLookup(SITE_ADMINISTRATOR)).withTargetFacility(volFac);
		n = notificationService.saveOrUpdate(n);

		n = new Notification("Excluded Individual found",
				"The volunteer \"" + v.getDisplayName() + "\" (identifying code " + v.getIdentifyingCode()
						+ ") matches an LEIE excluded individual but was saved as a false positive.",
				HIGH, LEIE, getTodayAtFacility(), null, getCurrentUserAsOrNull(AppUser.class), volFac, true,
				VOLUNTEER_PROFILE, LEIE_REPORT).withRefVolunteer(v)
						.withTargetRole(roleDAO.findByLookup(NATIONAL_ADMIN));
		n = notificationDAO.saveOrUpdate(n);
	}

	@Override
	public Volunteer terminateVolunteer(long volunteerId, boolean withCause, String remarksToAppend) {
		Volunteer vol = volunteerDAO.findRequiredByPrimaryKey(volunteerId);
		if (StringUtils.isNotBlank(remarksToAppend)) {
			String newRemarks = StringUtils.isNotBlank(vol.getTerminationRemarks()) ? vol.getTerminationRemarks() + "\n"
					: "";
			newRemarks += remarksToAppend + getAuditText(getCurrentUser());
			vol.setTerminationRemarks(newRemarks);
		}
		inactiveAllAssignmentsAndOrgs(vol);
		vol = volunteerDAO.saveOrUpdate(vol);

		volunteerDAO.updateFieldsWithoutVersionIncrement(vol.getId(), false, null, false, null,
				withCause ? TERMINATED_WITH_CAUSE : TERMINATED, getTodayAtFacility(), null);
		volunteerDAO.flushAndRefresh(vol);
		return vol;
	}

	@Override
	public Volunteer terminateVolunteerForLEIEMatch(ExcludedEntityMatch match, LEIETerminationParams termParams)
			throws ServiceValidationException {
		Volunteer v = match.getVolunteer();
		ExcludedEntity e = match.getExcludedEntity();

		v = terminateVolunteer(v.getId(), false, "System terminated due to LEIE match");

		v.setLeieExclusionDate(e.getExclusionDate());
		v = volunteerDAO.saveOrUpdate(v);
		volunteerDAO.flush();

		List<Notification> existingNotifications = notificationDAO.findByCriteria(NotificationSeverityType.HIGH,
				NotificationType.LEIE, null, false, null, false, null, false, null, true, v.getId());
		for (Notification n : existingNotifications)
			notificationDAO.delete(n.getId());

		Facility volFac = v.getPrimaryOrOriginallyCreatedAtFacility();
		Notification n = new Notification(termParams.facAdminNotificationName,
				termParams.facAdminNotificationDescription, HIGH, LEIE, getTodayAtFacility(), null, null, volFac, true,
				VOLUNTEER_PROFILE, LEIE_REPORT).withRefVolunteer(v)
						.withTargetRole(roleDAO.findByLookup(SITE_ADMINISTRATOR)).withTargetFacility(volFac);
		n = notificationService.saveOrUpdate(n);

		n = new Notification(termParams.natAdminNotificationName, termParams.natAdminNotificationDescription, HIGH,
				LEIE, getTodayAtFacility(), null, null, volFac, true, VOLUNTEER_PROFILE, LEIE_REPORT)
						.withRefVolunteer(v).withTargetRole(roleDAO.findByLookup(NATIONAL_ADMIN));
		n = notificationService.saveOrUpdate(n);
		return v;
	}

	private String getAuditText(CoreUserDetails appUser) {
		ZoneId timeZone = appUser.getTimeZone();
		if (timeZone == null)
			timeZone = ZoneId.systemDefault();

		return "\n - " + appUser.getDisplayName() + " [" + appUser.getUsername() + "] @"
				+ ZonedDateTime.now(timeZone).format(DateUtil.MILITARY_DATE_TIME_WITH_ZONE_FORMAT);
	}

	@Override
	@PreAuthorize("hasAuthority('" + Permission.VOLUNTEER_CREATE + "')")
	public void delete(long volunteerId) {
		volunteerDAO.delete(volunteerId);
	}

	@Override
	@PreAuthorize("hasAuthority('" + Permission.VOLUNTEER_CREATE + "')")
	public void setPrimaryOrganization(long volunteerId, long organizationId) {
		volunteerDAO.updateFieldsWithoutVersionIncrement(volunteerId, true, organizationId, false, null, null, null,
				null);
	}

	@Override
	@PreAuthorize("hasAuthority('" + Permission.VOLUNTEER_CREATE + "')")
	public void setPrimaryFacility(long volunteerId, long facilityId) {
		volunteerDAO.updateFieldsWithoutVersionIncrement(volunteerId, false, null, true, facilityId, null, null, null);
	}

	@Override
	@PreAuthorize("hasAuthority('" + Permission.VOLUNTEER_CREATE + "')")
	public boolean addOrReactivateOrganization(long volunteerId, long organizationId)
			throws ServiceValidationException {
		List<VolunteerOrganization> list = volunteerOrganizationDAO.findByCriteria(volunteerId, organizationId, null,
				null);
		VolunteerOrganization vo = null;
		Volunteer v = volunteerDAO.findRequiredByPrimaryKey(volunteerId);

		if (v.getStatus().isVolunteerInactiveOrTerminated())
			throw new ServiceValidationException(
					"volunteer.error.organization.addOrganizationToInactiveOrTerminatedVolunteer");

		if (list.isEmpty()) {
			AbstractBasicOrganization o = organizationDAO.findRequiredByPrimaryKey(organizationId);
			if (o.isInactive())
				throw new ServiceValidationException(o.getScope() == ScopeType.NATIONAL
						? "volunteer.error.organization.addInactiveNationalOrganization"
						: "volunteer.error.organization.addInactiveLocalOrganization");
			vo = new VolunteerOrganization(v, o);
		} else {
			vo = list.get(0);
			AbstractBasicOrganization o = vo.getOrganization();
			if (o.isInactive())
				throw new ServiceValidationException(o.getScope() == ScopeType.NATIONAL
						? "volunteer.error.organization.reactivateInactiveNationalOrganization"
						: "volunteer.error.organization.reactivateInactiveLocalOrganization");
			vo.setInactive(false);
		}

		vo = volunteerOrganizationDAO.saveOrUpdate(vo);

		VolunteerStatusUpdateResult r = updateVolunteerStatusBasedOnNewAssignments(v);

		if (v.getPrimaryOrganization() == null)
			volunteerDAO.updateFieldsWithoutVersionIncrement(v.getId(), true, organizationId, false, null, null, null,
					null);

		return r.statusChanged;
	}

	@Override
	@PreAuthorize("hasAuthority('" + Permission.VOLUNTEER_CREATE + "')")
	public Map<String, Object> inactivateOrganization(long volunteerOrganizationId) {
		VolunteerOrganization vo = volunteerOrganizationDAO.findRequiredByPrimaryKey(volunteerOrganizationId);
		Volunteer v = vo.getVolunteer();

		AbstractBasicOrganization currentPrimary = v.getPrimaryOrganization();
		AbstractBasicOrganization orgBeingInactivated = vo.getOrganization();

		boolean primaryWasInactivatedOrDeleted = orgBeingInactivated.equals(currentPrimary);

		vo.setInactive(true);
		vo = volunteerOrganizationDAO.saveOrUpdate(vo);

		return updateStatusAndPrimaryOrgIfNeeded(v, primaryWasInactivatedOrDeleted);
	}

	@Override
	@PreAuthorize("hasAuthority('" + Permission.VOLUNTEER_CREATE + "')")
	public Map<String, Object> deleteOrganization(long volunteerOrganizationId) {
		VolunteerOrganization vo = volunteerOrganizationDAO.findRequiredByPrimaryKey(volunteerOrganizationId);
		Volunteer v = vo.getVolunteer();

		AbstractBasicOrganization currentPrimary = v.getPrimaryOrganization();
		AbstractBasicOrganization orgBeingInactivated = vo.getOrganization();

		boolean primaryWasInactivatedOrDeleted = orgBeingInactivated.equals(currentPrimary);

		List<WorkEntry> workEntries = workEntryDAO.findByCriteria(v.getId(), null, null, orgBeingInactivated.getId(),
				null, null);
		if (workEntries.isEmpty()) {
			volunteerOrganizationDAO.delete(volunteerOrganizationId);
		} else {
			vo.setInactive(true);
			vo = volunteerOrganizationDAO.saveOrUpdate(vo);
		}

		return updateStatusAndPrimaryOrgIfNeeded(v, primaryWasInactivatedOrDeleted);
	}

	private Map<String, Object> updateStatusAndPrimaryOrgIfNeeded(Volunteer v, boolean primaryWasInactivatedOrDeleted) {
		volunteerDAO.flushAndRefresh(v);

		VolunteerStatusUpdateResult r = updateVolunteerStatusBasedOnNewAssignments(v);

		boolean reportPrimaryWasInactivated = false;

		Set<VolunteerOrganization> activeOrgAssignments = v.getVolunteerOrganizationsByStatus(true);
		if (activeOrgAssignments.size() == 1) {
			/*
			 * If we deleted or inactivated an org, and afterward there's only
			 * one active one left, this must become our new primary in all
			 * cases. Select it and don't bother notifying the user.
			 */
			AbstractBasicOrganization organization = activeOrgAssignments.iterator().next().getOrganization();
			volunteerDAO.updateFieldsWithoutVersionIncrement(v.getId(), true, organization.getId(), false, null, null,
					null, null);
		} else if (primaryWasInactivatedOrDeleted) {
			/*
			 * If we inactivated/deleted our current primary, and afterward
			 * there's either 0 or more than 1 active orgs left, null out the
			 * primary value. Only report that a new one will need to be
			 * selected if one is available for selection.
			 */
			reportPrimaryWasInactivated = !activeOrgAssignments.isEmpty();
			volunteerDAO.updateFieldsWithoutVersionIncrement(v.getId(), true, null, false, null, null, null, null);
		}

		Map<String, Object> results = new HashMap<>();
		results.put("primaryOrgInactivated", reportPrimaryWasInactivated);
		results.put("volunteerStatusChanged", r.statusChanged);
		return results;
	}

	@Override
	@PreAuthorize("hasAuthority('" + Permission.VOLUNTEER_CREATE + "')")
	public boolean inactivateAssignment(long volunteerAssignmentId) {
		VolunteerAssignment vo = volunteerAssignmentDAO.findRequiredByPrimaryKey(volunteerAssignmentId);
		Volunteer v = vo.getVolunteer();
		Map<Long, LocalDate> workEntries = workEntryDAO.getMostRecentVolunteeredDateByFacilityMap(v.getId());

		Facility currentPrimary = v.getPrimaryFacility();

		vo.setInactive(true);
		vo = volunteerAssignmentDAO.saveOrUpdate(vo);

		boolean statusChanged = updatePrimaryFacilityAndStatus(v, workEntries, currentPrimary);
		return statusChanged;
	}

	@Override
	@PreAuthorize("hasAuthority('" + Permission.VOLUNTEER_CREATE + "')")
	public boolean deleteAssignment(long volunteerAssignmentId) {
		VolunteerAssignment vo = volunteerAssignmentDAO.findRequiredByPrimaryKey(volunteerAssignmentId);
		Volunteer v = vo.getVolunteer();
		Map<Long, LocalDate> workEntries = workEntryDAO.getMostRecentVolunteeredDateByFacilityMap(v.getId());

		Facility currentPrimary = v.getPrimaryFacility();

		if (vo.getWorkEntries().isEmpty()) {
			volunteerAssignmentDAO.delete(volunteerAssignmentId);
		} else {
			vo.setInactive(true);
			vo = volunteerAssignmentDAO.saveOrUpdate(vo);
		}

		boolean statusChanged = updatePrimaryFacilityAndStatus(v, workEntries, currentPrimary);
		return statusChanged;
	}

	private boolean updatePrimaryFacilityAndStatus(Volunteer v, Map<Long, LocalDate> workEntries,
			Facility currentPrimary) {
		volunteerDAO.flushAndRefresh(v);

		SortedMap<LocalDate, Facility> facilityPrioritizedChoices = new TreeMap<>();

		/* Get the active facility with the most recent hours worked */
		Set<VolunteerAssignment> activeAssignments = v.getAssignmentsByStatus(true);
		for (VolunteerAssignment a : activeAssignments) {
			Facility f = a.getFacility().getFacility();
			/*
			 * If we have another active assignment at our current primary
			 * facility, no work needed - return - CPB
			 */
			if (f.equals(currentPrimary))
				return false;

			LocalDate mostRecentWorkDate = workEntries.get(f.getId());
			if (mostRecentWorkDate != null)
				facilityPrioritizedChoices.put(mostRecentWorkDate, f);
		}

		/*
		 * If we don't have any active assignments, get the inactive assignment
		 * facility with the most recent hours worked - CPB
		 */
		if (facilityPrioritizedChoices.isEmpty()) {
			Set<VolunteerAssignment> inactiveAssignments = v.getAssignmentsByStatus(false);
			for (VolunteerAssignment a : inactiveAssignments) {
				Facility f = a.getFacility().getFacility();
				LocalDate mostRecentWorkDate = workEntries.get(f.getId());
				if (mostRecentWorkDate != null)
					facilityPrioritizedChoices.put(mostRecentWorkDate, f);
			}
		}

		VolunteerStatusUpdateResult r = updateVolunteerStatusBasedOnNewAssignments(v);

		if (facilityPrioritizedChoices.isEmpty()) {
			volunteerDAO.updateFieldsWithoutVersionIncrement(v.getId(), false, null, true,
					v.getOriginallyCreatedAt() == null ? null : v.getOriginallyCreatedAt().getId(), null, null, null);
		} else {
			volunteerDAO.updateFieldsWithoutVersionIncrement(v.getId(), false, null, true,
					facilityPrioritizedChoices.get(facilityPrioritizedChoices.lastKey()).getId(), null, null, null);
		}

		return r.statusChanged;
	}

	@Override
	@PreAuthorize("hasAuthority('" + Permission.VOLUNTEER_CREATE + "')")
	public boolean addOrReactivateAssignment(Long volunteerAssignmentId, Long volunteerId, long facilityId,
			Long benefitingServiceRoleId) throws ServiceValidationException {
		VolunteerAssignment va = null;
		Volunteer v = null;

		if (volunteerAssignmentId != null) {
			va = volunteerAssignmentDAO.findRequiredByPrimaryKey(volunteerAssignmentId);
			v = va.getVolunteer();

			/* Needed for the bulk insert query below - CPB */
			volunteerId = v.getId();

			if (v.getStatus().isVolunteerTerminated())
				throw new ServiceValidationException("volunteerAssignment.reactivate.errorVolunteerTerminated");

			BenefitingService bs = va.getBenefitingService();
			if (bs.isInactive()) {
				if (bs.isNational() && bs.getTemplate().isInactive())
					throw new ServiceValidationException(
							"volunteerAssignment.reactivate.errorNationalBenefitingServiceInactive");

				throw new ServiceValidationException(
						"volunteerAssignment.reactivate.errorLocalBenefitingServiceInactive", bs.getDisplayName());
			}

			BenefitingServiceRole bsr = va.getBenefitingServiceRole();
			if (bsr.isInactive()) {
				if (bsr.isNational() && bsr.getTemplate().isInactive())
					throw new ServiceValidationException(
							"volunteerAssignment.reactivate.errorNationalBenefitingServiceInactive");

				throw new ServiceValidationException(
						"volunteerAssignment.reactivate.errorLocalBenefitingServiceInactive", bs.getDisplayName());
			}

			va.setInactive(false);
			va = volunteerAssignmentDAO.saveOrUpdate(va);
		} else {
			v = volunteerDAO.findRequiredByPrimaryKey(volunteerId);
			if (v.getStatus().isVolunteerTerminated())
				throw new ServiceValidationException("volunteerAssignment.reactivate.errorVolunteerTerminated");

			// prevent duplicate {volunteer, benefiting service role, location}
			List<VolunteerAssignment> potentialDuplicates = volunteerAssignmentDAO.findByCriteria(volunteerId, null,
					benefitingServiceRoleId, facilityId, null);
			if (!potentialDuplicates.isEmpty())
				throw new ServiceValidationException("volunteerAssignment.error.duplicateAssignmentFound");

			BenefitingServiceRole bsr = benefitingServiceRoleDAO.findRequiredByPrimaryKey(benefitingServiceRoleId);
			BenefitingService bs = bsr.getBenefitingService();

			va = new VolunteerAssignment();
			va.setBenefitingService(bs);
			va.setBenefitingServiceRole(bsr);
			va.setFacility(bsr.getFacility());
			va.setRootFacility(bs.getFacility());
			va.setVolunteer(v);
			va = volunteerAssignmentDAO.saveOrUpdate(va);
		}

		VolunteerStatusUpdateResult r = updateVolunteerStatusBasedOnNewAssignments(v);

		return r.statusChanged;
	}

	static class VolunteerStatusUpdateResult {
		Volunteer updatedVolunteer;
		boolean statusChanged;

		public VolunteerStatusUpdateResult(Volunteer updatedVolunteer, boolean statusChanged) {
			this.updatedVolunteer = updatedVolunteer;
			this.statusChanged = statusChanged;
		}
	}

	private VolunteerStatusUpdateResult updateVolunteerStatusBasedOnNewAssignments(Volunteer v) {
		LocalDate cutoffDate = getTodayAtFacility().minusDays(maxIdleDaysBeforeVolunteerInactivation);

		boolean hasActiveAssignments = !v.getAssignmentsByStatus(true).isEmpty();
		boolean hasInactiveAssignments = !v.getAssignmentsByStatus(false).isEmpty();

		boolean changed = false;
		VolunteerStatus status = v.getStatus();

		VolunteerStatusType correctType = null;
		if (v.getEntryDate().isAfter(cutoffDate)) {
			if (hasActiveAssignments) {
				correctType = ACTIVE;
			} else if (hasInactiveAssignments) {
				correctType = INACTIVE;
			} else {
				correctType = ACTIVE;
			}
		} else {
			correctType = hasActiveAssignments ? ACTIVE : INACTIVE;
		}

		if (status.getLookupType() == INACTIVE && correctType == ACTIVE) {
			volunteerDAO.updateFieldsWithoutVersionIncrement(v.getId(), false, null, false, null, ACTIVE,
					getTodayAtFacility(), null);
			changed = true;
		} else if (status.getLookupType() == ACTIVE && correctType == INACTIVE) {
			volunteerDAO.updateFieldsWithoutVersionIncrement(v.getId(), false, null, false, null, INACTIVE,
					getTodayAtFacility(), null);

			for (VolunteerOrganization o : v.getVolunteerOrganizations()) {
				if (o.isActive()) {
					o.setInactive(true);
					o = volunteerOrganizationDAO.saveOrUpdate(o);
				}
			}

			changed = true;
		}

		if (changed) {
			volunteerDAO.refresh(v);
		}
		return new VolunteerStatusUpdateResult(v, changed);
	}

	@Override
	public int inactivateStaleVolunteers(ZoneId switchToTimeZone) {
		ZonedDateTime cutoffDate = ZonedDateTime.now(switchToTimeZone)
				.minusDays(maxIdleDaysBeforeVolunteerInactivation);
		ZonedDateTime gracePeriodCutoff = ZonedDateTime.now(switchToTimeZone)
				.minusDays(volunteerInactivationGracePeriod);
		volunteerAssignmentDAO.inactivateStaleAssignments(cutoffDate, gracePeriodCutoff);
		int updated = volunteerDAO.inactivateStaleVolunteers(cutoffDate.toLocalDate(), switchToTimeZone);
		volunteerOrganizationDAO.inactivateForInactiveVolunteers();
		return updated;
	}

	@Override
	@PreAuthorize("hasAnyAuthority('" + Permission.VOLUNTEER_CREATE + ", " + Permission.LOGIN_KIOSK + "')")
	public void updatePreferredLanguage(long volunteerId, String language) {
		Language l = languageDAO.findByLanguageCode(language);
		if (l != null)
			volunteerDAO.updateFieldsWithoutVersionIncrement(volunteerId, false, null, false, null, null, null,
					l.getId());
	}

}
