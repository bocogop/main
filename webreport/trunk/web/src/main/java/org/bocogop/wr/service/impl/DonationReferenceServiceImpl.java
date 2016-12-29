package org.bocogop.wr.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import org.bocogop.shared.service.validation.ServiceValidationException;
import org.bocogop.wr.model.donation.DonationReference;
import org.bocogop.wr.service.DonationReferenceService;

@Service
public class DonationReferenceServiceImpl extends AbstractServiceImpl implements DonationReferenceService {
	private static final Logger log = LoggerFactory.getLogger(DonationReferenceServiceImpl.class);

	@Override
	public DonationReference saveOrUpdate(DonationReference donReference) throws ServiceValidationException {
		return donationReferenceDAO.saveOrUpdate(donReference);
	}

	@Override
	public void delete(long donReferenceId) throws ServiceValidationException {
		donationReferenceDAO.delete(donReferenceId);
	}

	public void inactivate(long donReferenceId) throws ServiceValidationException {
		DonationReference ref = donationReferenceDAO.findRequiredByPrimaryKey(donReferenceId);
		ref.setInactive(true);
		donationReferenceDAO.saveOrUpdate(ref);
	}

	@Override
	public void reactivate(long donReferenceId) throws ServiceValidationException {
		DonationReference ref = donationReferenceDAO.findRequiredByPrimaryKey(donReferenceId);
		boolean wasInactive = ref.isInactive();
		if (wasInactive) {
			ref.setInactive(false);
			donationReferenceDAO.saveOrUpdate(ref);
		}
	}
}
