package org.bocogop.wr.persistence.dao.volunteer;

import java.util.List;

import org.bocogop.wr.model.notification.Notification;
import org.bocogop.wr.model.voluntaryService.VoluntaryServiceStaff;
import org.bocogop.wr.persistence.dao.CustomizableAppDAO;

public interface VoluntaryServiceStaffDAO extends CustomizableAppDAO<VoluntaryServiceStaff> {

	/**
	 * 
	 * @param appUserName
	 *            TODO
	 * @param stationNum
	 *            TODO
	 * @return The list of Voluntary Service Staff matching the above criteria
	 */
	List<VoluntaryServiceStaff> findByCriteria(Long facilityId, String appUserName);

	/**
	 * 
	 * @param staffTitleId
	 * @return The list of Voluntary Service Staff with the given staff title
	 */
	List<VoluntaryServiceStaff> findByStaffTitle(long staffTitleId);

	List<VoluntaryServiceStaff> findLinkedToNotification(Notification notification);

}
