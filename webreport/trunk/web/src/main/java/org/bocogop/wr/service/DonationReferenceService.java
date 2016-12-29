package org.bocogop.wr.service;

import org.bocogop.shared.service.validation.ServiceValidationException;
import org.bocogop.wr.model.donation.DonationReference;

public interface DonationReferenceService {

	DonationReference saveOrUpdate(DonationReference donationReference) throws ServiceValidationException;

	void delete(long donReferenceId) throws ServiceValidationException;

	public void reactivate(long donReferenceId) throws ServiceValidationException;

	public void inactivate(long donReferenceId) throws ServiceValidationException;

}
