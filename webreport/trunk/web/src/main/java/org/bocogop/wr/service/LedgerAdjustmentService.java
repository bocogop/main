package org.bocogop.wr.service;

import org.bocogop.shared.service.validation.ServiceValidationException;
import org.bocogop.wr.model.expenditure.LedgerAdjustment;

public interface LedgerAdjustmentService {

	LedgerAdjustment saveOrUpdate(LedgerAdjustment expenditure) throws ServiceValidationException;

	/**
	 * 
	 * @param donorId
	 *            The donorId of the donor to be deleted
	 * @return void
	 */
	void delete(long ledgerAdjustmentId) throws ServiceValidationException;

}
