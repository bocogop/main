package org.bocogop.wr.service;

import org.springframework.dao.DataIntegrityViolationException;

import org.bocogop.shared.service.validation.ServiceValidationException;
import org.bocogop.wr.model.donation.Donor;

public interface DonorService {

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
	Donor saveOrUpdate(Donor donor) throws ServiceValidationException;

	/**
	 * 
	 * @param donorId
	 *            The donorId of the donor to be linked
	 * @param volunteerId
	 *            The id of the volunteer to be linked
	 * @return
	 */
	Donor linkVolunteer(Long donorId, Long volunteerId) throws ServiceValidationException;

	/**
	 * 
	 * @param donorId
	 *            The donorId of the donor to be linked
	 * @param orgId
	 *            The id of the organization to be linked
	 * @return
	 */
	Donor linkOrganization(Long donorId, Long orgId) throws ServiceValidationException;

	/**
	 * 
	 * @param donorId
	 *            The donorId of the donor to be deleted
	 * @return void
	 */
	void delete(Long donorId) throws ServiceValidationException;

}
