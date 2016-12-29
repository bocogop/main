package org.bocogop.wr.persistence;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.bocogop.wr.AbstractTransactionalWebDAOTest;
import org.bocogop.wr.model.requirement.VolunteerRequirement;
import org.bocogop.wr.model.views.VolunteerRequirementActive;
import org.bocogop.wr.persistence.dao.CustomizableAppDAO;
import org.bocogop.wr.persistence.dao.requirement.VolunteerRequirementDAO;

public class TestVolunteerRequirementDAO extends AbstractTransactionalWebDAOTest<VolunteerRequirement> {

	@Autowired
	private VolunteerRequirementDAO volunteerRequirementDAO;

	@Override
	protected CustomizableAppDAO<VolunteerRequirement> getDAO() {
		return volunteerRequirementDAO;
	}

	@Test
	public void testFindByCriteria() {
		Long volunteerId = new Long(53859L);// volunteerDAO.findSome(1).get(0).getId();
		Long facilityId = new Long(218L);
		// test list meal ticket by facility and date
		List<VolunteerRequirementActive> vrList = volunteerRequirementDAO
				.findByCriteria(VolunteerRequirementActive.class, volunteerId, facilityId);
		Assert.assertFalse("Failed search for VolunteerRequirement - ", vrList.isEmpty());
	}

	@Override
	protected VolunteerRequirement getInstanceToSave() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Test
	public void testBulkInsertForVolunteer() {
		Long volunteerId = volunteerDAO.findSome(1).get(0).getId();
		int result = volunteerRequirementDAO.bulkAddNecessaryRequirements(volunteerId, null, null, null);
	}

	@Test
	public void testBulkInsertAll() {
		Long requirementId = requirementDAO.findSome(1).get(0).getId();
		int result = volunteerRequirementDAO.bulkAddNecessaryRequirements(null, requirementId, null, null);
	}

	@Test
	public void testBulkUpdateInvalidStatusesToNew() {
		Long requirementId = requirementDAO.findSome(1).get(0).getId();
		volunteerRequirementDAO.bulkUpdateInvalidStatusesToNew(requirementId);
	}

}
