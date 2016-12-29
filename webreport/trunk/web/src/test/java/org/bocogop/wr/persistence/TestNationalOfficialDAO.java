package org.bocogop.wr.persistence;

import java.time.LocalDate;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.bocogop.wr.AbstractTransactionalWebDAOTest;
import org.bocogop.wr.model.organization.AbstractBasicOrganization;
import org.bocogop.wr.model.organization.NationalOfficial;
import org.bocogop.wr.model.organization.StdVAVSTitle.StdVAVSTitleValue;
import org.bocogop.wr.persistence.dao.CustomizableAppDAO;
import org.bocogop.wr.persistence.dao.NationalOfficialDAO;

public class TestNationalOfficialDAO extends AbstractTransactionalWebDAOTest<NationalOfficial> {

	@Autowired
	private NationalOfficialDAO nationalOfficialDAO;

	@Override
	protected CustomizableAppDAO<NationalOfficial> getDAO() {
		return nationalOfficialDAO;
	}

	@Override
	protected NationalOfficial getInstanceToSave() {
		AbstractBasicOrganization org = organizationDAO.findByPrimaryKey(new Long(6));

		NationalOfficial o = new NationalOfficial(org);
		o.setLastName("Chen");
		o.setStreetAddress("5400 Legay Dr");
		o.setCity("Plano");
		o.setState(stateDAO.findStateByPostalCode("CO"));
		o.setZip("75024");
		o.setStdVAVSTitle(stdVAVSTitleDAO.findByLookup(StdVAVSTitleValue.DEPUTY_NATIONAL_REPRESENTATIVE));
		o.setNationalCommitteeMember(false);
		return o;
	}

	@Test
	public void testFindByCriteria() {
		Long orgId = organizationDAO.findSome(1).get(0).getId();
		List<NationalOfficial> officials = nationalOfficialDAO.findByCriteria(orgId, LocalDate.now());
		Assert.assertFalse("Failed - org didn't return any results", officials.isEmpty());
	}

	/*
	 * @Test public void testfindByVAVSTitle() { NationalOfficial official =
	 * nationalOfficialDAO.findByVAVSTitle(new Long(2),
	 * StdVAVSTitleValue.NATIONAL_REPRESENTATIVE.getName()); Assert.
	 * assertFalse("Failed - 2, National Representative didn't return any results"
	 * , official == null); }
	 */

	@Override
	protected boolean testDelete() {
		return true;
	}

}
