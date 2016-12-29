package org.bocogop.wr.service;

import org.bocogop.shared.service.validation.ServiceValidationException;
import org.bocogop.wr.model.organization.NationalOfficial;

public interface NationalOfficialService {

	/**
	 * @param nationalOfficial
	 *            The nationalOfficial to save or update
	 * @return The updated nationalOfficial after it's been persisted /
	 *         updated
	 * @throws ServiceValidationException
	 *             If a business-level validation exception occurred during the
	 *             save/update process
	 */
	NationalOfficial saveOrUpdate(NationalOfficial nationalOfficial) throws ServiceValidationException;

	/**
	 * Deletes the NationalOfficial with the specified nationalOfficialId
	 * 
	 * @param nationalOfficialId
	 *            The ID of the national official to delete
	 */
	void delete(long nationalOfficialId);

}
