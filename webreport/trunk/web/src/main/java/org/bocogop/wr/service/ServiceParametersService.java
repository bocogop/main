package org.bocogop.wr.service;

import org.bocogop.shared.service.validation.ServiceValidationException;
import org.bocogop.wr.model.voluntaryService.VoluntaryServiceParameters;

public interface ServiceParametersService {

	/**
	 * @param volunteer
	 *            The volunteer to save or update
	 * @return The updated volunteer after it's been persisted / updated
	 * @throws ServiceValidationException
	 *             If a business-level validation exception occurred during the
	 *             save/update process
	 */
	VoluntaryServiceParameters saveOrUpdate(VoluntaryServiceParameters serviceParameters)
			throws ServiceValidationException;

	/**
	 * Deletes the Volunteer with the specified volunteerId
	 * 
	 * @param serviceParametersId
	 *            The ID of the service parameters to delete
	 */
	void delete(long serviceParametersId);
}
