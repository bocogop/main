package org.bocogop.wr.persistence;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.bocogop.shared.model.Role.RoleType;
import org.bocogop.wr.AbstractTransactionalWebDAOTest;
import org.bocogop.wr.model.facility.Facility;
import org.bocogop.wr.model.notification.Notification;
import org.bocogop.wr.model.voluntaryService.VoluntaryServiceStaff;
import org.bocogop.wr.persistence.dao.CustomizableAppDAO;
import org.bocogop.wr.persistence.dao.volunteer.VoluntaryServiceStaffDAO;

public class TestVoluntaryServiceStaffDAO extends AbstractTransactionalWebDAOTest<VoluntaryServiceStaff> {

	@Autowired
	private VoluntaryServiceStaffDAO voluntaryServiceStaffDAO;

	@Override
	protected CustomizableAppDAO<VoluntaryServiceStaff> getDAO() {
		return voluntaryServiceStaffDAO;
	}

	@Override
	protected VoluntaryServiceStaff getInstanceToSave() {
		VoluntaryServiceStaff v = new VoluntaryServiceStaff();
		v.setAppUser(user);
		v.setReportOrder(1);
		Facility facility = findFacility("442");
		v.setFacility(facility);
		return v;
	}

	@Test
	public void testFindByCriteria() {
		voluntaryServiceStaffDAO.findByPrimaryKey(1800L);
		// Assert.assertFalse("Failed - 442 didn't return any results",
		// staffsFor442 == null);
	}

	@Test
	public void testFindLinkedToNotification() {
		Notification n = new Notification();
		n.setTargetUser(this.user);
		voluntaryServiceStaffDAO.findLinkedToNotification(n);
		
		n.setTargetUser(null);
		n.setTargetVolunteer(volunteerDAO.findSome(1).get(0));
		voluntaryServiceStaffDAO.findLinkedToNotification(n);
		
		Facility f442 = facilityDAO.findByStationNumber("442");
		
		n.setTargetVolunteer(null);
		n.setTargetFacility(f442);
		voluntaryServiceStaffDAO.findLinkedToNotification(n);
		
		n.setTargetRole(roleDAO.findByLookup(RoleType.SITE_ADMINISTRATOR));
		voluntaryServiceStaffDAO.findLinkedToNotification(n);
		
		n.setTargetFacility(null);
		voluntaryServiceStaffDAO.findLinkedToNotification(n);
	}

	protected Facility findFacility(String stationNum) {
		return facilityDAO.findByStationNumber(stationNum);
	}

	@Override
	protected boolean testDelete() {
		return true;
	}

}
