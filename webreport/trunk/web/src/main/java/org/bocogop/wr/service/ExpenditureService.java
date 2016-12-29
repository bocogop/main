package org.bocogop.wr.service;

import org.springframework.dao.DataIntegrityViolationException;

import org.bocogop.shared.service.validation.ServiceValidationException;
import org.bocogop.wr.model.expenditure.Expenditure;

public interface ExpenditureService {

	/**
	 * 
	 * @param o
	 *            The organization to save or update
	 * @return The updated organization after it's been persisted / updated
	 * @throws DataIntegrityViolationException
	 *             In the rare case that another user requested a new
	 *             Organization to be created at almost exactly the same time
	 *             and the same code was assigned to both Organizations. In this
	 *             case, retrying the method should suffice.
	 * @throws ServiceValidationException
	 *             If a business-level validation exception occurred during the
	 *             save/update process
	 */
	Expenditure saveOrUpdate(Expenditure expenditure) throws ServiceValidationException;

	/**
	 * 
	 * @param donorId
	 *            The donorId of the donor to be deleted
	 * @return void
	 */
	void delete(long expenditureId) throws ServiceValidationException;

}
