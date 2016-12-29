package org.bocogop.wr.persistence.dao;

import java.time.LocalDate;
import java.util.List;

import org.bocogop.wr.model.notification.Notification;
import org.bocogop.wr.model.notification.NotificationSeverityType;
import org.bocogop.wr.model.notification.NotificationType;

public interface NotificationDAO extends CustomizableSortedDAO<Notification> {

	List<Notification> findByUserAndFacility(LocalDate activeOnDate, long appUserId, long facilityId);

	List<Notification> findByCriteria(NotificationSeverityType severity, NotificationType type, LocalDate activeOnDate, //
			boolean matchTargetRoleId, Long targetRoleId, //
			boolean matchTargetFacilityId, Long targetFacilityId, //
			boolean matchTargetUserId, Long targetUserId, //
			boolean matchReferenceVolunteerId, Long referenceVolunteerId);

	int purgeExpiredNotifications();

	int deleteByCriteria(Long facilityId, NotificationType notificationType);

}
