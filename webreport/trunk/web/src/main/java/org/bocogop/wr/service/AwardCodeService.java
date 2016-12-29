package org.bocogop.wr.service;

import org.bocogop.shared.service.validation.ServiceValidationException;
import org.bocogop.wr.model.award.Award;

public interface AwardCodeService {

	/**
	 * @param awardCode
	 *            The Award to save or update
	 * @return The updated awardCode after it's been merged

	 */
	Award saveOrUpdate(Long awardCodeId, Award awardCode) throws ServiceValidationException;

	/**
	 * Deletes the Award with the specified awardCodeId
	 * 
	 * @param awardCodeId
	 *            The ID of the award to delete
	 */
	void delete(long awardCodeId) throws ServiceValidationException;

}
