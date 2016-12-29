package org.bocogop.wr.service.impl;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;

import org.bocogop.shared.model.Permission;
import org.bocogop.shared.service.validation.ServiceValidationException;
import org.bocogop.wr.AbstractTransactionalWebTest;
import org.bocogop.wr.model.benefitingService.BenefitingService;
import org.bocogop.wr.model.benefitingService.BenefitingServiceRole;
import org.bocogop.wr.model.benefitingService.BenefitingServiceRoleTemplate;
import org.bocogop.wr.model.benefitingService.BenefitingServiceTemplate;
import org.bocogop.wr.model.facility.Facility;
import org.bocogop.wr.model.facility.Location;
import org.bocogop.wr.model.organization.AbstractBasicOrganization;
import org.bocogop.wr.model.time.OccasionalWorkEntry;
import org.bocogop.wr.model.volunteer.Volunteer;
import org.bocogop.wr.model.volunteer.VolunteerAssignment;
import org.bocogop.wr.service.BenefitingServiceRoleTemplateService;

public class TestBenefitingServiceRoleTemplateService extends AbstractTransactionalWebTest {

	@Autowired
	private BenefitingServiceRoleTemplateService service;

	@Test
	@WithMockUser(username = UNIT_TEST_USER, authorities = { Permission.VOLUNTEER_CREATE,
			Permission.BENEFITING_SERVICE_CREATE })
	public void testMerge() throws ServiceValidationException {
		Facility f442 = facilityDAO.findByStationNumber("442");
		Facility f552 = facilityDAO.findByStationNumber("552");
		Facility f660 = facilityDAO.findByStationNumber("660");

		/* Add a physical location at 552 */
		Location l552 = new Location();
		l552.setName("Garage");
		l552.setParent(f552);
		l552 = locationDAO.saveOrUpdate(l552);

		AbstractBasicOrganization o = organizationDAO.findSome(1).get(0);

		/* Create the templates and roles */
		BenefitingServiceTemplate fromBst = new BenefitingServiceTemplate();
		fromBst.setName("UnitTestTemplateService1");
		fromBst = benefitingServiceTemplateDAO.saveOrUpdate(fromBst);

		BenefitingServiceRoleTemplate fromBsrt = new BenefitingServiceRoleTemplate();
		fromBsrt.setName("UnitTestTemplateRole1");
		fromBsrt.setBenefitingServiceTemplate(fromBst);
		fromBsrt = benefitingServiceRoleTemplateDAO.saveOrUpdate(fromBsrt);
		benefitingServiceTemplateDAO.refresh(fromBst);

		BenefitingServiceTemplate toBst = new BenefitingServiceTemplate();
		toBst.setName("UnitTestTemplateService2");
		toBst = benefitingServiceTemplateDAO.saveOrUpdate(toBst);

		BenefitingServiceRoleTemplate toBsrt = new BenefitingServiceRoleTemplate();
		toBsrt.setName("UnitTestTemplateRole2");
		toBsrt.setBenefitingServiceTemplate(toBst);
		toBsrt = benefitingServiceRoleTemplateDAO.saveOrUpdate(toBsrt);
		benefitingServiceTemplateDAO.refresh(toBst);

		/* Claim national services at three sites */
		BenefitingService bs442 = new BenefitingService(fromBst, f442);
		bs442 = benefitingServiceDAO.saveOrUpdate(bs442);
		BenefitingServiceRole bsr442 = new BenefitingServiceRole(fromBsrt, bs442, f442);
		bsr442 = benefitingServiceRoleDAO.saveOrUpdate(bsr442);
		benefitingServiceDAO.refresh(bs442);

		BenefitingService bs552 = new BenefitingService(fromBst, f552);
		bs552 = benefitingServiceDAO.saveOrUpdate(bs552);
		// simulate a site claiming a service at a physical location
		BenefitingServiceRole bsr552 = new BenefitingServiceRole(fromBsrt, bs552, l552);
		bsr552 = benefitingServiceRoleDAO.saveOrUpdate(bsr552);
		benefitingServiceDAO.refresh(bs552);

		BenefitingService bs660 = new BenefitingService(fromBst, f660);
		bs660 = benefitingServiceDAO.saveOrUpdate(bs660);
		// simulate a site having both the from and to already
		BenefitingServiceRole bsr660From = new BenefitingServiceRole(fromBsrt, bs660, f660);
		bsr660From = benefitingServiceRoleDAO.saveOrUpdate(bsr660From);
		BenefitingServiceRole bsr660To = new BenefitingServiceRole(toBsrt, bs660, f660);
		bsr660To = benefitingServiceRoleDAO.saveOrUpdate(bsr660To);
		benefitingServiceDAO.refresh(bs660);

		benefitingServiceRoleTemplateDAO.refresh(fromBsrt);
		benefitingServiceRoleTemplateDAO.refresh(toBsrt);

		/* Add assignments and occasional hours to site roles */
		List<Volunteer> volunteers = volunteerDAO.findSome(4);
		Volunteer v1 = volunteers.get(0);
		Volunteer v2 = volunteers.get(1);
		Volunteer v3 = volunteers.get(2);
		Volunteer v4 = volunteers.get(3);

		VolunteerAssignment va1 = new VolunteerAssignment(v1, bsr442);
		va1 = volunteerAssignmentDAO.saveOrUpdate(va1);
		OccasionalWorkEntry owe1 = new OccasionalWorkEntry(o, bsr442, LocalDate.of(2012, 1, 1), 10, 2.5, "a");
		owe1 = occasionalWorkEntryDAO.saveOrUpdate(owe1);
		benefitingServiceRoleDAO.refresh(bsr442);

		VolunteerAssignment va2 = new VolunteerAssignment(v2, bsr552);
		va2 = volunteerAssignmentDAO.saveOrUpdate(va2);
		OccasionalWorkEntry owe2 = new OccasionalWorkEntry(o, bsr552, LocalDate.of(2013, 1, 1), 20, 2.0, "a");
		owe2 = occasionalWorkEntryDAO.saveOrUpdate(owe2);
		benefitingServiceRoleDAO.refresh(bsr552);

		VolunteerAssignment va3 = new VolunteerAssignment(v3, bsr660From);
		va3 = volunteerAssignmentDAO.saveOrUpdate(va3);
		OccasionalWorkEntry owe3 = new OccasionalWorkEntry(o, bsr660From, LocalDate.of(2014, 1, 1), 30, 3.5, "a");
		owe3 = occasionalWorkEntryDAO.saveOrUpdate(owe3);
		benefitingServiceRoleDAO.refresh(bsr660From);

		VolunteerAssignment va4 = new VolunteerAssignment(v4, bsr660To);
		va4 = volunteerAssignmentDAO.saveOrUpdate(va4);
		OccasionalWorkEntry owe4 = new OccasionalWorkEntry(o, bsr660To, LocalDate.of(2015, 1, 1), 40, 2.5, "a");
		owe4 = occasionalWorkEntryDAO.saveOrUpdate(owe4);
		benefitingServiceRoleDAO.refresh(bsr660To);

		/* Do the template merge */
		service.merge(fromBsrt.getId(), toBsrt.getId());
		benefitingServiceRoleDAO.flush();

		List<BenefitingServiceRole> newRolesAt442 = benefitingServiceRoleDAO.findByCriteria(null,
				Arrays.asList(f442.getId()), false, true);
		BenefitingServiceRole bsr = newRolesAt442.stream().filter(p -> "UnitTestTemplateRole2".equals(p.getName()))
				.findFirst().orElse(null);
		benefitingServiceRoleDAO.refresh(bsr);
		Assert.assertNotNull(bsr);
		Assert.assertEquals(bsr.getTemplate(), toBsrt);
		Assert.assertEquals(1, bsr.getVolunteerAssignments().size());
		Assert.assertEquals(va1, bsr.getVolunteerAssignments().get(0));
		Assert.assertEquals(1, bsr.getOccasionalWorkEntries().size());
		Assert.assertEquals(owe1, bsr.getOccasionalWorkEntries().get(0));

		List<BenefitingServiceRole> newRolesAt552 = benefitingServiceRoleDAO.findByCriteria(null,
				Arrays.asList(l552.getId()), false, true);
		BenefitingServiceRole newBsr552 = newRolesAt552.stream()
				.filter(p -> "UnitTestTemplateRole2".equals(p.getName())).findFirst().orElse(null);
		benefitingServiceRoleDAO.refresh(newBsr552);
		Assert.assertEquals(toBsrt, newBsr552.getTemplate());
		Assert.assertEquals(1, newBsr552.getVolunteerAssignments().size());
		Assert.assertEquals(va2, newBsr552.getVolunteerAssignments().get(0));
		Assert.assertEquals(1, newBsr552.getOccasionalWorkEntries().size());
		Assert.assertEquals(owe2, newBsr552.getOccasionalWorkEntries().get(0));

		benefitingServiceRoleDAO.refresh(bsr660To);
		Assert.assertEquals(2, bsr660To.getVolunteerAssignments().size());
		Assert.assertTrue(bsr660To.getVolunteerAssignments().remove(va4));
		Assert.assertEquals(bsr660To.getVolunteerAssignments().get(0), va3);
		Assert.assertEquals(bsr660To.getOccasionalWorkEntries().size(), 2);
		Assert.assertTrue(bsr660To.getOccasionalWorkEntries().remove(owe4));
		Assert.assertEquals(bsr660To.getOccasionalWorkEntries().get(0), owe3);
	}
}
