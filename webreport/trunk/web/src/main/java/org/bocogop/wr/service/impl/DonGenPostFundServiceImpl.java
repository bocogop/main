package org.bocogop.wr.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import org.bocogop.shared.service.validation.ServiceValidationException;
import org.bocogop.wr.model.donation.DonGenPostFund;
import org.bocogop.wr.service.DonGenPostFundService;

@Service
public class DonGenPostFundServiceImpl extends AbstractServiceImpl implements DonGenPostFundService {
	private static final Logger log = LoggerFactory.getLogger(DonGenPostFundServiceImpl.class);

	@Override
	public DonGenPostFund saveOrUpdate(DonGenPostFund donGenPostFund) throws ServiceValidationException {
		return donGenPostFundDAO.saveOrUpdate(donGenPostFund);
	}

	@Override
	public void delete(long donGenPostFundId) throws ServiceValidationException {
		donGenPostFundDAO.delete(donGenPostFundId);
	}
	
	public void inactivate(long donGenPostFundId) throws ServiceValidationException{
		DonGenPostFund gpf = donGenPostFundDAO.findRequiredByPrimaryKey(donGenPostFundId);
		gpf.setInactive(true);
		donGenPostFundDAO.saveOrUpdate(gpf);
	}

	@Override
	public void reactivate(long donGenPostFundId) throws ServiceValidationException {
		DonGenPostFund gpf = donGenPostFundDAO.findRequiredByPrimaryKey(donGenPostFundId);
		boolean wasInactive = gpf.isInactive();
		if (wasInactive) {
			gpf.setInactive(false);
			donGenPostFundDAO.saveOrUpdate(gpf);
		}
	}


}
