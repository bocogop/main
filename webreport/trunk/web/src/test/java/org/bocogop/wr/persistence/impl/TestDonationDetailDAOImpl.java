package org.bocogop.wr.persistence.impl;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.bocogop.wr.AbstractTransactionalWebDAOTest;
import org.bocogop.wr.model.donation.DonationDetail;
import org.bocogop.wr.persistence.dao.CustomizableAppDAO;
import org.bocogop.wr.persistence.dao.DonationDetailDAO;

public class TestDonationDetailDAOImpl extends AbstractTransactionalWebDAOTest<DonationDetail> {

	@Autowired
	private DonationDetailDAO donationDetailDAO;

	@Override
	protected CustomizableAppDAO<DonationDetail> getDAO() {
		return donationDetailDAO;
	}

	@Override
	protected DonationDetail getInstanceToSave() {
		return null;
	}

	@Test
	public void testFindByCriteria() {
		donationDetailDAO.findByDonationSummaryId(18, true);
		// Assert.assertFalse("Failed - didn't return any results",
		// donDetailList != null);

	}

	@Override
	protected boolean testDelete() {
		return true;
	}

}
