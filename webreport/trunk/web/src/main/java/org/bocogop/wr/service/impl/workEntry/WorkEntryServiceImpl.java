package org.bocogop.wr.service.impl.workEntry;

import static java.util.function.Function.identity;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import org.bocogop.shared.service.validation.ServiceValidationException;
import org.bocogop.wr.model.organization.AbstractBasicOrganization;
import org.bocogop.wr.model.time.WorkEntry;
import org.bocogop.wr.model.volunteer.Volunteer;
import org.bocogop.wr.model.volunteer.VolunteerAssignment;
import org.bocogop.wr.service.impl.AbstractServiceImpl;
import org.bocogop.wr.service.workEntry.WorkEntryService;
import org.bocogop.wr.util.DateUtil;

@Service
public class WorkEntryServiceImpl extends AbstractServiceImpl implements WorkEntryService {
	private static final Logger log = LoggerFactory.getLogger(WorkEntryServiceImpl.class);

	@Override
	public WorkEntry saveOrUpdate(WorkEntry workEntry, boolean updateExistingHours) throws ServiceValidationException {
		/*
		 * workEntry may be persistent or new. If persistent, we allow update
		 * even if currently the org or assignment are inactive. If new, we can
		 * choose to search for and merge into an existing persistent WorkEntry
		 * having the same volunteer/org/date worked combination
		 * ("updateExistingHours" flag). Otherwise, we throw an exception as a
		 * duplicate.
		 */

		/* Expect that they will delete work entries separately */
		if (workEntry.getHoursWorked() == 0)
			throw new ServiceValidationException("timePost.error.nonzeroHours");

		if (workEntry.getDateWorked().isBefore(dateUtil.getEarliestAcceptableDateEntryAsOfNow(getFacilityTimeZone())))
			throw new ServiceValidationException("timePost.error.dateTooEarly");
		if (workEntry.getDateWorked().isAfter(getTodayAtFacility()))
			throw new ServiceValidationException("timePost.error.futureDateDisallowed");

		VolunteerAssignment va = workEntry.getVolunteerAssignment();
		Volunteer v = va.getVolunteer();

		if (v.getStatus().isVolunteerInactiveOrTerminated())
			throw new ServiceValidationException("timePost.error.volunteerTerminated");

		AbstractBasicOrganization o = workEntry.getOrganization();

		List<WorkEntry> existingEntries = workEntryDAO.findByCriteria(null, va.getId(), null,
				o == null ? null : o.getId(), workEntry.getDateWorked(), null);

		WorkEntry returnedWorkEntry = null;

		if (workEntry.isPersistent()) {
			/* "edit" being called from the staff app */
			long workEntryId = workEntry.getId();
			WorkEntry other = existingEntries.stream().filter(p -> !p.getId().equals(workEntryId)).findFirst()
					.orElse(null);
			if (other != null) {
				/*
				 * If a duplicate exists, merge into it and remove ourself.
				 */
				other.setHoursWorked(other.getHoursWorked() + workEntry.getHoursWorked());
				workEntryDAO.delete(workEntry);
				returnedWorkEntry = workEntryDAO.saveOrUpdate(other);
			} else {
				/* Otherwise just save ourself */
				returnedWorkEntry = workEntryDAO.saveOrUpdate(workEntry);
			}
		} else {
			if (updateExistingHours) {
				/* Being called from the kiosk */

				if (!existingEntries.isEmpty()) {
					WorkEntry other = existingEntries.get(0);
					/*
					 * If our item exists with the same assignment, org and
					 * date, update just the hours, even if the assignment or
					 * org is currently inactive
					 */
					other.setHoursWorked(workEntry.getHoursWorked());
					returnedWorkEntry = workEntryDAO.saveOrUpdate(other);
				} else {
					/*
					 * Otherwise, ensure the assignment and org are active and
					 * save a new item
					 */
					ensureAssignmentAndOrgAreActive(va, o);
					returnedWorkEntry = workEntryDAO.saveOrUpdate(workEntry);
				}
			} else {
				/* "new" being called from the staff app */
				if (!existingEntries.isEmpty())
					throw new ServiceValidationException("timePost.error.duplicateEntryDetected",
							new Serializable[] { va.getVolunteer().getDisplayName(), va.getDisplayName(),
									workEntry.getDateWorked().format(DateUtil.DATE_ONLY_FORMAT),
									o == null ? "(none)" : o.getDisplayName() });

				ensureAssignmentAndOrgAreActive(va, o);
				returnedWorkEntry = workEntryDAO.saveOrUpdate(workEntry);
			}
		}

		if (workEntryDAO.findByCriteria(v.getId(), null, null, null, returnedWorkEntry.getDateWorked(), null).stream()
				.mapToDouble(p -> p.getHoursWorked()).sum() > 24)
			throw new ServiceValidationException("timePost.error.exceeded24HoursInDay");

		return returnedWorkEntry;
	}

	public void ensureAssignmentAndOrgAreActive(VolunteerAssignment va, AbstractBasicOrganization o)
			throws ServiceValidationException {
		Volunteer v = va.getVolunteer();
		if (v.getVolunteerOrganizations().stream().filter(p -> p.isActive()).map(vo -> vo.getOrganization())
				.noneMatch(org -> org.equals(o)))
			throw new ServiceValidationException("timePost.error.orgNotAssigned",
					new Serializable[] { o.getDisplayName(), v.getDisplayName() });

		Collection<VolunteerAssignment> assignments = v.getAssignmentsByStatus(true);
		if (!assignments.contains(va))
			throw new ServiceValidationException("timePost.error.assignmentForWrongVolunteer",
					new Serializable[] { va.getDisplayName(), v.getDisplayName() });
	}

	@Override
	public void saveOrUpdateMultipleForVolunteerAndDayAndFacility(List<WorkEntry> workEntries, long volunteerId,
			long facilityId, LocalDate day) throws ServiceValidationException {

		for (WorkEntry workEntry : workEntries) {
			if (!workEntry.getDateWorked().equals(day)) {
				throw new ServiceValidationException("timePost.error.dayNotConsistent");
			}

			VolunteerAssignment va = volunteerAssignmentDAO
					.findRequiredByPrimaryKey(workEntry.getVolunteerAssignment().getId());
			if (!va.getVolunteer().getId().equals(volunteerId)) {
				throw new ServiceValidationException("timePost.error.volunteerNotConsistent");
			}

			if (!va.getFacility().getFacility().getId().equals(facilityId)) {
				throw new ServiceValidationException("timePost.error.facilityNotConsistent");
			}
		}

		Map<Long, WorkEntry> existingWorkEntries = workEntryDAO
				.findByCriteria(volunteerId, null, facilityId, null, day, null).stream()
				.collect(Collectors.toMap(k -> k.getId(), identity()));

		for (WorkEntry workEntry : workEntries) {
			if (workEntry.getHoursWorked() == 0)
				continue;
			workEntry = saveOrUpdate(workEntry, true);
			existingWorkEntries.remove(workEntry.getId());
		}

		workEntryDAO.deleteByPrimaryKeys(existingWorkEntries.keySet());

		List<WorkEntry> newItems = workEntryDAO.findByCriteria(volunteerId, null, facilityId, null, day, null);
		double total = newItems.stream().mapToDouble(p -> p.getHoursWorked()).sum();
		if (total > 24)
			throw new ServiceValidationException("timePost.error.exceeded24HoursInDay");
	}

	@Override
	public void saveMultipleNew(List<WorkEntry> workEntries, boolean updateExistingHours)
			throws ServiceValidationException {
		for (WorkEntry workEntry : workEntries) {
			workEntry = saveOrUpdate(workEntry, updateExistingHours);
		}
	}

	@Override
	public void delete(long workEntryId) {
		workEntryDAO.delete(workEntryId);
	}

}
