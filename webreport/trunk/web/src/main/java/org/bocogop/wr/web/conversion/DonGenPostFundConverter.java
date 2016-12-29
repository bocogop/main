package org.bocogop.wr.web.conversion;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import org.bocogop.wr.model.donation.DonGenPostFund;
import org.bocogop.wr.persistence.dao.donGenPostFund.DonGenPostFundDAO;

@Component
public class DonGenPostFundConverter extends AbstractStringToPersistentConverter<DonGenPostFund> {

	@Autowired
	protected DonGenPostFundConverter(DonGenPostFundDAO dao) {
		super(dao);
	}
}
