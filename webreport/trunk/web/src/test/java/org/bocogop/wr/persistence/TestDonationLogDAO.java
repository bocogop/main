package org.bocogop.wr.persistence;

import org.junit.Test;

import org.bocogop.wr.AbstractTransactionalWebDAOTest;
import org.bocogop.wr.model.donation.DonationLog;
import org.bocogop.wr.persistence.dao.CustomizableAppDAO;

public class TestDonationLogDAO extends AbstractTransactionalWebDAOTest<DonationLog> {

	@Override
	protected CustomizableAppDAO<DonationLog> getDAO() {
		return donationLogDAO;
	}

	@Override
	protected DonationLog getInstanceToSave() {
		DonationLog dl = new DonationLog();
		return dl;
	}

	@Override
	protected boolean testDelete() {
		return true;
	}

	@Test
	public void testCountByCriteria() {
		donationLogDAO.countByCriteria("442", true);
	}

}
