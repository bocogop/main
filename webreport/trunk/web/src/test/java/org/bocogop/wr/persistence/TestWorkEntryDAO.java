package org.bocogop.wr.persistence;

import java.time.LocalDate;
import java.util.List;

import org.junit.Test;

import org.bocogop.wr.AbstractTransactionalWebDAOTest;
import org.bocogop.wr.model.benefitingService.BenefitingServiceRole;
import org.bocogop.wr.model.time.WorkEntry;
import org.bocogop.wr.model.volunteer.Volunteer;
import org.bocogop.wr.persistence.dao.CustomizableAppDAO;

public class TestWorkEntryDAO extends AbstractTransactionalWebDAOTest<WorkEntry> {

	@Override
	protected CustomizableAppDAO<WorkEntry> getDAO() {
		return workEntryDAO;
	}

	@Override
	protected WorkEntry getInstanceToSave() {
		WorkEntry we = new WorkEntry();
		we.setDateWorked(LocalDate.now());
		we.setVolunteerAssignment(volunteerAssignmentDAO.findSome(1).get(0));
		we.setOrganization(organizationDAO.findSome(1).get(0));
		// TODO CPB
		return we;
	}

	@Test
	public void testGetMostRecentVolunteeredDateMap() {
		List<Volunteer> v = volunteerDAO.findSome(1);
		workEntryDAO.getMostRecentVolunteeredDateByFacilityMap(v.get(0).getId());
	}

	@Test
	public void testBulk() {
		List<BenefitingServiceRole> some = benefitingServiceRoleDAO.findSome(2);
		workEntryDAO.bulkChangeForBenefitingServiceRoleMerge(some.get(0).getId(), some.get(1).getId());
	}

	@Override
	protected boolean testDelete() {
		return true;
	}

}
