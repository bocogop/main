package org.bocogop.wr.persistence;

import java.util.List;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.bocogop.shared.util.PersistenceUtil;
import org.bocogop.wr.AbstractTransactionalWebDAOTest;
import org.bocogop.wr.model.organization.AbstractBasicOrganization;
import org.bocogop.wr.model.volunteer.Volunteer;
import org.bocogop.wr.model.volunteer.VolunteerOrganization;
import org.bocogop.wr.persistence.dao.CustomizableAppDAO;
import org.bocogop.wr.persistence.dao.volunteer.VolunteerDAO;
import org.bocogop.wr.persistence.dao.volunteer.VolunteerOrganizationDAO;

public class TestVolunteerOrganizationDAO extends AbstractTransactionalWebDAOTest<VolunteerOrganization> {

	@Autowired
	private VolunteerOrganizationDAO dao;
	@Autowired
	private VolunteerDAO volunteerDAO;
	
	@Override
	protected CustomizableAppDAO<VolunteerOrganization> getDAO() {
		return dao;
	}

	@Override
	protected VolunteerOrganization getInstanceToSave() {
		VolunteerOrganization vo = new VolunteerOrganization();

		AbstractBasicOrganization o = organizationDAO.findSome(1).get(0);
		vo.setOrganization(o);

		Volunteer v = volunteerDAO.findSome(1).get(0);
		vo.setVolunteer(v);
		return vo;
	}

	@Override
	protected boolean testDelete() {
		return true;
	}

	@Test
	public void testInactivateForInactiveVolunteers() {
		dao.inactivateForInactiveVolunteers();
	}
	
	@Test
	public void testFindByCriteria() {
		dao.findByCriteria(null, null, true, facilityDAO.findSome(1).get(0).getId());
	}
	
	@Test
	public void testBulkUpdate() {
		List<Volunteer> some = volunteerDAO.findSome(2);
		AbstractBasicOrganization o = organizationDAO.findSome(1).get(0);
		dao.bulkUpdatePrimaryOrganizationsByCriteria(PersistenceUtil.translateObjectsToIds(some), true, o.getId());
		dao.bulkUpdatePrimaryOrganizationsByCriteria(PersistenceUtil.translateObjectsToIds(some), true, null);
	}
}
