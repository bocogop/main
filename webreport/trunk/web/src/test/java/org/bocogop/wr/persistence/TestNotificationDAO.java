package org.bocogop.wr.persistence;

import org.junit.Test;

import org.bocogop.wr.AbstractTransactionalWebDAOTest;
import org.bocogop.wr.model.facility.Facility;
import org.bocogop.wr.model.notification.Notification;
import org.bocogop.wr.model.notification.NotificationType;
import org.bocogop.wr.persistence.dao.CustomizableAppDAO;

public class TestNotificationDAO extends AbstractTransactionalWebDAOTest<Notification> {

	@Override
	protected CustomizableAppDAO<Notification> getDAO() {
		return notificationDAO;
	}

	@Override
	protected Notification getInstanceToSave() {
		return null;
	}

	@Test
	public void testPurge() {
		notificationDAO.purgeExpiredNotifications();
	}

	@Override
	protected boolean testDelete() {
		return false;
	}

	@Test
	public void testDeleteByCriteria() {
		Facility f = facilityDAO.findByStationNumber("442");
		notificationDAO.deleteByCriteria(f.getId(), NotificationType.DONATION);
	}

}
