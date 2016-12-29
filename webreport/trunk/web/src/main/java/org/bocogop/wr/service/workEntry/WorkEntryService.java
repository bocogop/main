package org.bocogop.wr.service.workEntry;

import java.time.LocalDate;
import java.util.List;

import org.bocogop.shared.service.validation.ServiceValidationException;
import org.bocogop.wr.model.time.WorkEntry;

public interface WorkEntryService {

	void saveMultipleNew(List<WorkEntry> workEntries, boolean allowMergeTime) throws ServiceValidationException;

	/**
	 * @param workEntry
	 *            The workEntry to save or update
	 * @param updateExistingHours
	 *            TODO
	 * @return The updated workEntry after it's been persisted / updated
	 * @throws ServiceValidationException
	 *             If a business-level validation exception occurred during the
	 *             save/update process
	 */
	WorkEntry saveOrUpdate(WorkEntry workEntry, boolean updateExistingHours)
			throws ServiceValidationException;

	/**
	 * Deletes the WorkEntry with the specified workEntryId
	 * 
	 * @param serviceStaffId
	 *            The ID of the voluntary service staff to delete
	 */
	void delete(long workEntryId);

	void saveOrUpdateMultipleForVolunteerAndDayAndFacility(List<WorkEntry> workEntries, long volunteerId, long facilityId, LocalDate day)
			throws ServiceValidationException;

}
