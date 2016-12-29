package org.bocogop.wr.persistence;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.bocogop.wr.AbstractTransactionalWebDAOTest;
import org.bocogop.wr.model.donation.Donor;
import org.bocogop.wr.model.donation.DonorType.DonorTypeValue;
import org.bocogop.wr.persistence.dao.CustomizableAppDAO;
import org.bocogop.wr.persistence.dao.DonorDAO;
import org.bocogop.wr.persistence.dao.lookup.DonorTypeDAO;
import org.bocogop.wr.persistence.impl.DonorDAOImpl.DonorSearchResult;

public class TestDonorDAO extends AbstractTransactionalWebDAOTest<Donor> {

	@Autowired
	private DonorDAO donorDAO;
	@Autowired
	protected DonorTypeDAO donorTypeDAO;

	@Override
	protected CustomizableAppDAO<Donor> getDAO() {
		return donorDAO;
	}

	@Override
	protected Donor getInstanceToSave() {
		Donor d = new Donor();
		d.setDonorType(donorTypeDAO.findByLookup(DonorTypeValue.INDIVIDUAL));
		d.setLastName("Chen");
		d.setAddressLine1("5400 Legay Dr");
		d.setCity("Plano");
		d.setState(stateDAO.findStateByPostalCode("TX"));
		d.setZip("75024");

		return d;
	}

	@Test
	public void testFindByCriteria() {
		Long facilityId = new Long(218L);
		// test individual donor
		List<DonorSearchResult> donorList = donorDAO.findByCriteria(donorTypeDAO.findByLookup(DonorTypeValue.INDIVIDUAL), 
				null, null, "uif", null, null, null, null, null, null, facilityId);
		Assert.assertFalse("Failed search for individual - ", donorList.isEmpty());
	}
	
	@Override
	protected boolean testDelete() {
		return true;
	}

}
