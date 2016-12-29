package org.bocogop.wr.service;

import org.bocogop.shared.service.validation.ServiceValidationException;
import org.bocogop.wr.model.voluntaryService.VoluntaryServiceStaff;

public interface VoluntaryServiceStaffService {

	/**
	 * @param voluntaryServiceStaff
	 *            The voluntaryServiceStaff to save or update
	 * @return The updated voluntaryServiceStaff after it's been persisted /
	 *         updated
	 * @throws ServiceValidationException
	 *             If a business-level validation exception occurred during the
	 *             save/update process
	 */
	VoluntaryServiceStaff saveOrUpdate(VoluntaryServiceStaff serviceStaff);

	/**
	 * Deletes the VoluntaryServiceStaff with the specified serviceStaffId
	 * 
	 * @param serviceStaffId
	 *            The ID of the voluntary service staff to delete
	 */
	void delete(long serviceStaffId);

	public VoluntaryServiceStaff createOrRetrieveServiceStaff(String staffAppUserName, long facilityId);

}
