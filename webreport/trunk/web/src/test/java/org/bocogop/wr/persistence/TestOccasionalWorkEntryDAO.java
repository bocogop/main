package org.bocogop.wr.persistence;

import java.time.LocalDate;
import java.util.List;

import org.junit.Test;

import org.bocogop.wr.AbstractTransactionalWebDAOTest;
import org.bocogop.wr.model.benefitingService.BenefitingServiceRole;
import org.bocogop.wr.model.time.OccasionalWorkEntry;
import org.bocogop.wr.persistence.dao.CustomizableAppDAO;

public class TestOccasionalWorkEntryDAO extends AbstractTransactionalWebDAOTest<OccasionalWorkEntry> {

	@Override
	protected CustomizableAppDAO<OccasionalWorkEntry> getDAO() {
		return occasionalWorkEntryDAO;
	}

	@Override
	protected OccasionalWorkEntry getInstanceToSave() {
		OccasionalWorkEntry we = new OccasionalWorkEntry();
		BenefitingServiceRole benefitingServiceRole = benefitingServiceRoleDAO.findSome(1).get(0);
		we.setBenefitingServiceRole(benefitingServiceRole);
		we.setBenefitingService(benefitingServiceRole.getBenefitingService());
		we.setFacility(facilityDAO.findByStationNumber(TEST_STATION_NUMBER));
		we.setOrganization(organizationDAO.findSome(1).get(0));
		we.setDateWorked(LocalDate.of(2014, 06, 10));
		we.setHoursWorked(320);
		// TODO CPB
		return we;
	}

	@Test
	public void testExistsForCriteria() {
		occasionalWorkEntryDAO.existsForCriteria(12L, null, null, null);
		occasionalWorkEntryDAO.existsForCriteria(null, 12L, null, null);
		occasionalWorkEntryDAO.existsForCriteria(null, null, 12L, null);
		occasionalWorkEntryDAO.existsForCriteria(null, null, null, 12L);
		occasionalWorkEntryDAO.existsForCriteria(16L, null, 12L, null);
	}

	@Test
	public void testBulkMove() {
		List<BenefitingServiceRole> some = benefitingServiceRoleDAO.findSome(2);
		occasionalWorkEntryDAO.bulkMove(some.get(0).getId(), some.get(1).getId());
	}

	@Override
	protected boolean testDelete() {
		return true;
	}

}
