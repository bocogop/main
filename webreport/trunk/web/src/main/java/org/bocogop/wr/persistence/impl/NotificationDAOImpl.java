package org.bocogop.wr.persistence.impl;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import org.bocogop.wr.model.notification.Notification;
import org.bocogop.wr.model.notification.NotificationSeverityType;
import org.bocogop.wr.model.notification.NotificationType;
import org.bocogop.wr.model.views.UserFacilityPermission;
import org.bocogop.wr.model.views.UserFacilityRole;
import org.bocogop.wr.persistence.dao.NotificationDAO;
import org.bocogop.wr.persistence.queryCustomization.QueryCustomization;

@Repository
public class NotificationDAOImpl extends GenericHibernateSortedDAOImpl<Notification> implements NotificationDAO {
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(NotificationDAOImpl.class);

	@Override
	public List<Notification> findByUserAndFacility(LocalDate activeOnDate, long appUserId, long facilityId) {
		StringBuilder sb = new StringBuilder("select n from ").append(Notification.class.getName()).append(" n");
		sb.append(" left join n.targetRole r");
		sb.append(" left join n.targetPermission p");
		sb.append(" left join n.targetFacility f");
		sb.append(" left join n.targetUser u");

		QueryCustomization cust = new QueryCustomization();

		List<String> whereClauseItems = new ArrayList<>();
		Map<String, Object> params = new HashMap<>();

		whereClauseItems.add("n.beginDate <= :activeOnDate");
		whereClauseItems.add("COALESCE(n.expirationDate, '2199-01-01') >= :activeOnDate");
		params.put("activeOnDate", activeOnDate);

		whereClauseItems.add("(r.id is null or exists (" //
				+ "select ufr from " + UserFacilityRole.class.getName() + " ufr" //
				+ " where ufr.user.id = :appUserId" //
				+ " and ufr.facility.id = :facilityId" //
				+ " and ufr.role.id = r.id))");
		whereClauseItems.add("(p.id is null or exists (" //
				+ "select ufp from " + UserFacilityPermission.class.getName() + " ufp" //
				+ " where ufp.user.id = :appUserId" //
				+ " and ufp.facility.id = :facilityId" //
				+ " and ufp.permission.id = p.id))");
		whereClauseItems.add("(f.id is null or f.id = :facilityId)");
		whereClauseItems.add("(u.id is null or u.id = :appUserId)");

		params.put("appUserId", appUserId);
		params.put("facilityId", facilityId);

		Query q = constructQuery(em, sb, whereClauseItems, params, null, cust);

		@SuppressWarnings("unchecked")
		List<Notification> resultList = q.getResultList();
		return resultList;
	}

	@Override
	public List<Notification> findByCriteria(NotificationSeverityType severity, NotificationType type,
			LocalDate activeOnDate, //
			boolean matchTargetRoleId, Long targetRoleId, //
			boolean matchTargetFacilityId, Long targetFacilityId, //
			boolean matchTargetUserId, Long targetUserId, //
			boolean matchReferenceVolunteerId, Long referenceVolunteerId) {
		StringBuilder sb = new StringBuilder("select n from ").append(Notification.class.getName()).append(" n");

		QueryCustomization cust = new QueryCustomization();
		List<String> whereClauseItems = new ArrayList<>();
		Map<String, Object> params = new HashMap<>();

		if (severity != null) {
			whereClauseItems.add("n.severity = :severity");
			params.put("severity", severity);
		}

		if (type != null) {
			whereClauseItems.add("n.type = :type");
			params.put("type", type);
		}

		if (activeOnDate != null) {
			whereClauseItems.add("n.beginDate <= :activeOnDate");
			whereClauseItems.add("(n.expirationDate is null or n.expirationDate >= :activeOnDate)");
			params.put("activeOnDate", activeOnDate);
		}

		if (matchTargetRoleId) {
			if (targetRoleId == null) {
				whereClauseItems.add("n.targetRole is null");
			} else {
				whereClauseItems.add("n.targetRole.id = :targetRoleId");
				params.put("targetRoleId", targetRoleId);
			}
		}

		if (matchTargetFacilityId) {
			if (targetFacilityId == null) {
				whereClauseItems.add("n.targetFacility is null");
			} else {
				whereClauseItems.add("n.targetFacility.id = :targetFacilityId");
				params.put("targetFacilityId", targetFacilityId);
			}
		}

		if (matchTargetUserId) {
			if (targetUserId == null) {
				whereClauseItems.add("n.targetUser is null");
			} else {
				whereClauseItems.add("n.targetUser.id = :targetUserId");
				params.put("targetUserId", targetUserId);
			}
		}

		if (matchReferenceVolunteerId) {
			if (referenceVolunteerId == null) {
				whereClauseItems.add("n.referenceVolunteer is null");
			} else {
				whereClauseItems.add("n.referenceVolunteer.id = :referenceVolunteerId");
				params.put("referenceVolunteerId", referenceVolunteerId);
			}
		}

		Query q = constructQuery(em, sb, whereClauseItems, params, null, cust);

		@SuppressWarnings("unchecked")
		List<Notification> resultList = q.getResultList();
		return resultList;
	}

	@Override
	public int purgeExpiredNotifications() {
		int numPurged = em.createQuery("delete from " + Notification.class.getName() + " where expirationDate < :now")
				.setParameter("now", LocalDate.now()).executeUpdate();
		return numPurged;
	}

	@Override
	public int deleteByCriteria(Long facilityId, NotificationType notificationType) {
		// todo accept optional params - CPB
		int numDeleted = em
				.createQuery("delete from " + Notification.class.getName() + " where id in (" //
						+ "select n.id from " + Notification.class.getName() + " n" //
						+ " where n.targetFacility.id = :facilityId" //
						+ " and n.type = :notificationType)") //
				.setParameter("facilityId", facilityId) //
				.setParameter("notificationType", notificationType) //
				.executeUpdate();
		return numDeleted;
	}

}
