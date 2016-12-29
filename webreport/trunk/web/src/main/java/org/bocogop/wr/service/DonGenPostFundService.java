package org.bocogop.wr.service;

import org.bocogop.shared.service.validation.ServiceValidationException;
import org.bocogop.wr.model.donation.DonGenPostFund;

public interface DonGenPostFundService {

	DonGenPostFund saveOrUpdate(DonGenPostFund donGenPostFund) throws ServiceValidationException;

	void delete(long donGenPostFundId) throws ServiceValidationException;
	
	public void reactivate(long donGenPostFundId) throws ServiceValidationException ;
	
	public void inactivate(long donGenPostFundId) throws ServiceValidationException;
}
