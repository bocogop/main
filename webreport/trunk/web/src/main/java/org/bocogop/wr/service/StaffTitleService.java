package org.bocogop.wr.service;

import org.bocogop.shared.service.validation.ServiceValidationException;
import org.bocogop.wr.model.facility.StaffTitle;

public interface StaffTitleService {

	/**
	 * @param staffTitle
	 *            The StaffTitle to save or update
	 * @return The updated staffTitle after it's been merged
	 * @throws ServiceValidationException 
	 */
	StaffTitle saveOrUpdate(StaffTitle staffTitle);

	/**
	 * Deletes the Staff Title with the specified staffTitleId
	 * 
	 * @param staffTitleId
	 *            The ID of the staffTitle to delete
	 */
	void delete(long staffTitleId) throws ServiceValidationException;

}
