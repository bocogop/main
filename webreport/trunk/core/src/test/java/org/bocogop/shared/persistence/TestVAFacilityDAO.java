package org.bocogop.shared.persistence;

import org.junit.Test;
import org.springframework.util.Assert;

import org.bocogop.shared.AbstractTransactionalCoreDAOTest;
import org.bocogop.shared.model.lookup.sds.VAFacility;

public class TestVAFacilityDAO extends AbstractTransactionalCoreDAOTest<VAFacility> {

	@Override
	protected VAFacility getInstanceToSave() {
		return null;
	}

	@Override
	protected AppDAO<VAFacility> getDAO() {
		return vaFacilityDAO;
	}

	@Override
	protected boolean testDelete() {
		return false;
	}

	@Test
	public void testFindAllVAMCsSorted() {
		vaFacilityDAO.findAllThreeDigitStationsSorted();
		// for (VAFacility vamc : vamcs) {
		// System.out.println("\t" + vamc);
		// }
	}

	@Test
	public void testFindAllVISNSSorted() {
		vaFacilityDAO.findAllVISNsSorted();
	}

	@Test
	public void testFindAllSorted() {
		vaFacilityDAO.findAllSorted();
	}

	@Test
	public void testFindByStationNumber() {
		VAFacility i = vaFacilityDAO.findByStationNumber("442");
		Assert.notNull(i, "The object was not found");
	}

}
