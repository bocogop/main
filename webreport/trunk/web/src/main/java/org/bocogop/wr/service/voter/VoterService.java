package org.bocogop.wr.service.voter;

import org.bocogop.shared.service.validation.ServiceValidationException;
import org.bocogop.wr.model.voter.Voter;

public interface VoterService {

	/**
	 * @param voter
	 *            The voter to save or update
	 * @param createDataChangeNotifications
	 *            TODO
	 * @param autoTerminateIfLEIEMatch
	 *            TODO
	 * @param userContext
	 *            TODO
	 * @return The updated voter after it's been persisted / updated
	 * @throws ServiceValidationException
	 *             If a business-level validation exception occurred during the
	 *             save/update process
	 */
	Voter saveOrUpdate(Voter voter, boolean createDataChangeNotifications, boolean autoTerminateIfLEIEMatch)
			throws ServiceValidationException;

	void delete(long voterId);

}
