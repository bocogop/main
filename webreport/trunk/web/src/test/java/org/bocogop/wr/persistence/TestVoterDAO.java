package org.bocogop.wr.persistence;

import java.time.LocalDate;

import org.bocogop.shared.model.lookup.sds.Gender.GenderType;
import org.bocogop.wr.AbstractTransactionalWebDAOTest;
import org.bocogop.wr.model.voter.Voter;
import org.bocogop.wr.persistence.dao.CustomizableAppDAO;
import org.bocogop.wr.persistence.dao.voter.VoterDAO;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class TestVoterDAO extends AbstractTransactionalWebDAOTest<Voter> {

	@Autowired
	private VoterDAO dao;

	@Override
	protected CustomizableAppDAO<Voter> getDAO() {
		return dao;
	}

	@Override
	protected Voter getInstanceToSave() {
		Voter v = new Voter();
		v.setFirstName("Connor");
		v.setLastName("Barry");
		v.setDateOfBirth(LocalDate.of(1950, 1, 1));
		v.setZip("80026");
		v.setCity("Lafayette");
		v.setState(stateDAO.findStateByPostalCode("CO"));
		v.setGender(genderDAO.findByLookup(GenderType.MALE));
		v.setAddressLine1("658 Wild Ridge Cir");
		v.setEntryDate(LocalDate.now());
		// v.setPrimaryPrecinct(precinctDAO.findByStationNumber("442"));
		// v.setContactInfo(vci);
		return v;
	}

	@Override
	protected boolean testDelete() {
		return true;
	}

	@Test
	public void testQuickSearch() {
		long v = dao.findSome(1).get(0).getId();
		dao.quickSearch("Te", null, 218, true, true, true);
		dao.quickSearch(null, v, 218, true, true, true);
	}

}
