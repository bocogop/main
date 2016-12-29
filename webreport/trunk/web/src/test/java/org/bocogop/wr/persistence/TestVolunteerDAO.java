package org.bocogop.wr.persistence;

import java.time.LocalDate;
import java.time.ZoneId;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.bocogop.shared.model.lookup.sds.Gender.GenderType;
import org.bocogop.wr.AbstractTransactionalWebDAOTest;
import org.bocogop.wr.model.lookup.Language.LanguageType;
import org.bocogop.wr.model.volunteer.Volunteer;
import org.bocogop.wr.persistence.dao.CustomizableAppDAO;
import org.bocogop.wr.persistence.dao.volunteer.VolunteerDAO;

public class TestVolunteerDAO extends AbstractTransactionalWebDAOTest<Volunteer> {

	@Autowired
	private VolunteerDAO dao;

	@Override
	protected CustomizableAppDAO<Volunteer> getDAO() {
		return dao;
	}

	@Override
	protected Volunteer getInstanceToSave() {
		Volunteer v = new Volunteer();
		v.setFirstName("Connor");
		v.setLastName("Barry");
		v.setDateOfBirth(LocalDate.of(1950, 1, 1));
		v.setZip("80026");
		v.setCity("Lafayette");
		v.setState(stateDAO.findStateByPostalCode("CO"));
		v.setGender(genderDAO.findByLookup(GenderType.MALE));
		v.setAddressLine1("658 Wild Ridge Cir");
		v.setEntryDate(LocalDate.now());
		v.setPreferredLanguage(languageDAO.findByLookup(LanguageType.ENGLISH));
		// v.setPrimaryFacility(facilityDAO.findByStationNumber("442"));
		// v.setContactInfo(vci);
		return v;
	}

	@Override
	protected boolean testDelete() {
		return true;
	}

	@Test
	public void testGetTimeSummary() {
		Volunteer instanceToSave = getInstanceToSave();
		if (instanceToSave != null)
			instanceToSave = getDAO().saveOrUpdate(instanceToSave);

		dao.getTimeSummary(instanceToSave.getId(), ZoneId.of("US/Eastern"));
	}

	@Test
	public void testQuickSearch() {
		long v = dao.findSome(1).get(0).getId();
		dao.quickSearch("Te", null, 218, true, true, true);
		dao.quickSearch(null, v, 218, true, true, true);
	}

	@Test
	public void testInactivateStaleVolunteers() {
		dao.inactivateStaleVolunteers(LocalDate.now().minusDays(365), ZoneId.of("US/Eastern"));
	}

}
