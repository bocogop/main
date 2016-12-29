package org.bocogop.wr.persistence.impl;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.bocogop.wr.AbstractTransactionalWebDAOTest;
import org.bocogop.wr.model.benefitingService.BenefitingService;
import org.bocogop.wr.model.benefitingService.BenefitingServiceRole;
import org.bocogop.wr.model.facility.Facility;
import org.bocogop.wr.model.organization.AbstractBasicOrganization;
import org.bocogop.wr.model.time.WorkEntry;
import org.bocogop.wr.model.volunteer.Volunteer;
import org.bocogop.wr.model.volunteer.VolunteerAssignment;
import org.bocogop.wr.persistence.dao.CustomizableAppDAO;
import org.bocogop.wr.persistence.dao.volunteer.VolunteerAssignmentDAO;

public class TestVolunteerAssignmentDAOImpl extends AbstractTransactionalWebDAOTest<VolunteerAssignment> {

	@Autowired
	private VolunteerAssignmentDAO dao;

	@Override
	protected CustomizableAppDAO<VolunteerAssignment> getDAO() {
		return dao;
	}

	@Override
	protected VolunteerAssignment getInstanceToSave() {
		Facility f = getFacility();
		Volunteer v = volunteerDAO.findSome(1).get(0);
		BenefitingServiceRole bsr = benefitingServiceRoleDAO.findSome(1).get(0);

		VolunteerAssignment d = new VolunteerAssignment();
		d.setRootFacility(f);
		d.setFacility(f);
		d.setBenefitingServiceRole(bsr);
		d.setBenefitingService(bsr.getBenefitingService());
		d.setVolunteer(v);
		return d;
	}

	@Test
	public void testBulkDeleteByCriteria() {
		dao.bulkDeleteByCriteria(null, null, -1L, null);
		dao.bulkDeleteByCriteria(-1L, null, null, null);
		dao.bulkDeleteByCriteria(null, null, null, -1L);
		dao.bulkDeleteByCriteria(null, -1L, null, null);
		dao.bulkDeleteByCriteria(-2L, -4L, -1L, -3L);
	}

	@Test
	public void testBulkChangeBenefitingServiceRole() {
		List<BenefitingServiceRole> some = benefitingServiceRoleDAO.findSome(2);
		dao.bulkChangeForBenefitingServiceRoleMerge(some.get(0).getId(), some.get(1).getId());
		dao.bulkDeleteDuplicatesAfterChange(some.get(0).getId(), some.get(1).getId());
	}

	@Test
	public void testCountByCriteria() {
		dao.countByCriteria(15L);
	}

	@Test
	public void testInactivateStaleAssignments() {
		Volunteer v = volunteerDAO.findSome(1).get(0);
		BenefitingService bs = benefitingServiceDAO.findSome(1).get(0);
		BenefitingServiceRole bsr = benefitingServiceRoleDAO.findSome(1).get(0);

		AbstractBasicOrganization o = organizationDAO.findSome(1).get(0);

		VolunteerAssignment d = new VolunteerAssignment();
		d.setVolunteer(v);
		d.setBenefitingService(bs);
		d.setBenefitingServiceRole(bsr);
		d.setFacility(bs.getFacility());
		d.setRootFacility(bs.getFacility());
		d.setModifiedDateOverride(ZonedDateTime.now(ZoneId.of("US/Eastern")).minusDays(400));
		d = volunteerAssignmentDAO.saveOrUpdate(d);

		WorkEntry we = new WorkEntry();
		we.setDateWorked(LocalDate.now().minusDays(400));
		we.setVolunteerAssignment(d);
		we.setOrganization(o);
		we = workEntryDAO.saveOrUpdate(we);
		Assert.assertTrue(d.isActive());

		dao.inactivateStaleAssignments(ZonedDateTime.now(ZoneId.of("US/Eastern")).minusDays(365),
				ZonedDateTime.now(ZoneId.of("US/Eastern")).minusDays(30));

		volunteerAssignmentDAO.refresh(d);
		Assert.assertFalse(d.isActive());
	}

	@Override
	protected boolean testDelete() {
		return true;
	}

}
