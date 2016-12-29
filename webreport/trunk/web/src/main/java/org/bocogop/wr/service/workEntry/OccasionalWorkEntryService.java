package org.bocogop.wr.service.workEntry;

import java.util.List;

import org.bocogop.shared.service.validation.ServiceValidationException;
import org.bocogop.wr.model.time.OccasionalWorkEntry;

public interface OccasionalWorkEntryService {

	void saveOrUpdateMultiple(List<OccasionalWorkEntry> workEntries) throws ServiceValidationException;

	/**
	 * @param workEntry
	 *            The OccasionalWorkEntry to save or update
	 * @param requireActiveOrganizationAndRole TODO
	 * @return The updated OccasionalWorkEntry after it's been persisted / updated
	 * @throws ServiceValidationException
	 *             If a business-level validation exception occurred during the
	 *             save/update process
	 */
	OccasionalWorkEntry saveOrUpdate(OccasionalWorkEntry workEntry, boolean requireActiveOrganizationAndRole) throws ServiceValidationException;

	/**
	 * Deletes the OccasionalWorkEntry with the specified workEntryId
	 * 
	 * @param serviceStaffId
	 *            The ID of the OccasionalWorkEntry to delete
	 */
	void delete(long occasionalWorkEntryId);

}
