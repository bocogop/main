package org.bocogop.wr.service.requirement;

import java.util.concurrent.Future;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;

import org.bocogop.shared.model.Permission;
import org.bocogop.wr.AbstractTransactionalWebTest;
import org.bocogop.wr.model.requirement.AbstractRequirement;
import org.bocogop.wr.model.requirement.FacilityRoleTypeRequirement;

public class TestVolunteerRequirementService extends AbstractTransactionalWebTest {

	@Autowired
	private VolunteerRequirementService service;

	@Test
	@WithMockUser(authorities = { Permission.VOLUNTEER_CREATE }, username = UNIT_TEST_USER)
	public void testSyncRequirementsForBulkChange() throws Exception {
		long volunteerId = volunteerDAO.findSome(1).get(0).getId();
		AbstractRequirement r = requirementDAO.findSomeByType(AbstractRequirement.class, 1).get(0);
		long requirementId = r.getId();
		long benefitingServiceRoleId = benefitingServiceRoleDAO.findSome(1).get(0).getId();
		long benefitingServiceRoleTemplateId = benefitingServiceRoleTemplateDAO.findSome(1).get(0).getId();
		
		Future<Integer> r1 = service.bulkAddNecessaryRequirementsLater(volunteerId, null, null, null);
		Future<Integer> r2 = service.bulkAddNecessaryRequirementsLater(null, requirementId, null, null);
		Future<Integer> r3 = service.bulkAddNecessaryRequirementsLater(null, null, benefitingServiceRoleId, null);
		Future<Integer> r4 = service.bulkAddNecessaryRequirementsLater(null, null, null, benefitingServiceRoleTemplateId);
		
		r1.get();
		r2.get();
		r3.get();
		r4.get();
	}

}
