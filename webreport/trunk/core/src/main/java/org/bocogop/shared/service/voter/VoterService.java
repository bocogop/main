package org.bocogop.shared.service.voter;

import org.bocogop.shared.model.voter.Voter;
import org.bocogop.shared.service.validation.ServiceValidationException;

public interface VoterService {

	/**
	 * @param voter
	 *            The voter to save or update
	 * @return The updated voter after it's been persisted / updated
	 * @throws ServiceValidationException
	 *             If a business-level validation exception occurred during the
	 *             save/update process
	 */
	Voter saveOrUpdate(Voter voter) throws ServiceValidationException;

	void delete(long voterId);

}
