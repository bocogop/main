package org.bocogop.wr.persistence.impl.volunteer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.persistence.Query;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import org.bocogop.shared.model.AppUser;
import org.bocogop.shared.model.Role;
import org.bocogop.wr.model.facility.Facility;
import org.bocogop.wr.model.notification.Notification;
import org.bocogop.wr.model.views.UserFacilityRole;
import org.bocogop.wr.model.voluntaryService.VoluntaryServiceStaff;
import org.bocogop.wr.model.volunteer.Volunteer;
import org.bocogop.wr.persistence.dao.volunteer.VoluntaryServiceStaffDAO;
import org.bocogop.wr.persistence.impl.GenericHibernateDAOImpl;
import org.bocogop.wr.persistence.queryCustomization.QueryCustomization;

@Repository
public class VoluntaryServiceStaffDAOImpl extends GenericHibernateDAOImpl<VoluntaryServiceStaff>
		implements VoluntaryServiceStaffDAO {
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(VoluntaryServiceStaffDAOImpl.class);

	@SuppressWarnings("unchecked")
	@Override
	public List<VoluntaryServiceStaff> findByCriteria(Long facilityId, String appUserName) {
		StringBuilder sb = new StringBuilder("select s from ").append(VoluntaryServiceStaff.class.getName())
				.append(" s");
		sb.append(" left join fetch s.appUser a");
		sb.append(" left join fetch s.facility v");

		QueryCustomization cust = new QueryCustomization();

		if (cust.getOrderBy() == null)
			cust.setOrderBy("a.lastName, a.firstName");

		List<String> whereClauseItems = new ArrayList<>();
		Map<String, Object> params = new HashMap<>();

		if (facilityId != null) {
			whereClauseItems.add("v.id = :facilityId");
			params.put("facilityId", facilityId);
		}

		if (StringUtils.isNotBlank(appUserName)) {
			whereClauseItems.add("a.username = :username");
			params.put("username", appUserName);
		}

		// Show all staff no matter
		/*
		 * if (activeAsOfDate != null) { whereClauseItems.add(
		 * "COALESCE(s.vavsStartDate, '1900-01-01') <= :activeAsOfDate");
		 * whereClauseItems.add(
		 * "COALESCE(s.vavsEndDate, '2199-01-01') > :activeAsOfDate");
		 * params.put("activeAsOfDate", activeAsOfDate); }
		 */

		Query q = constructQuery(em, sb, whereClauseItems, params, null, cust);

		return q.getResultList();
	}

	public List<VoluntaryServiceStaff> findByStaffTitle(long staffTitleId) {
		@SuppressWarnings("unchecked")
		List<VoluntaryServiceStaff> results = query(
				"from " + VoluntaryServiceStaff.class.getName() + " where staffTitle.id = :staffTitleId")
						.setParameter("staffTitleId", staffTitleId).getResultList();
		return results;
	}

	@Override
	public List<VoluntaryServiceStaff> findLinkedToNotification(Notification n) {
		StringBuilder sb = new StringBuilder("select s1") //
				.append(" from ").append(VoluntaryServiceStaff.class.getName()).append(" s1") //
				.append(" left join fetch s1.appUser a") //
				.append(" left join fetch s1.facility v") //
				.append(" where s1.id in (") //
				.append(" 	select s.id") //
				.append("	from ").append(VoluntaryServiceStaff.class.getName()).append(" s") //
				.append("	where ");

		Volunteer v = n.getTargetVolunteer();
		if (v != null)
			return new ArrayList<>();

		Facility f = n.getTargetFacility();
		if (f != null && f.getId() == null)
			throw new IllegalArgumentException("The notification was attached to an unsaved Facility");

		Role r = n.getTargetRole();
		if (r != null && r.getId() == null)
			throw new IllegalArgumentException("The notification was attached to an unsaved Role");

		AppUser au = n.getTargetUser();
		if (au != null && au.getId() == null)
			throw new IllegalArgumentException(
					"The notification was attached to an unsaved AppUser; please persist the AppUser first");

		List<String> whereClauseItems = new ArrayList<>();
		Map<String, Object> params = new HashMap<>();

		if (au != null) {
			whereClauseItems.add("s.appUser.id = :appUserId");
			params.put("appUserId", au.getId());
		} else {
			// target facility
			if (f != null) {
				whereClauseItems.add("s.facility.id = :facilityId");
				params.put("facilityId", f.getId());
			}

			// target role
			if (r != null) {
				if (f != null) {
					whereClauseItems.add("exists (select ufr from " + UserFacilityRole.class.getName()
							+ " ufr where ufr.user = s.appUser and ufr.role.id = :roleId and ufr.facility.id = :facilityId)");
					params.put("facilityId", f.getId());
				} else {
					whereClauseItems.add("exists (select ufr from " + UserFacilityRole.class.getName()
							+ " ufr where ufr.user = s.appUser and ufr.role.id = :roleId)");
				}
				params.put("roleId", r.getId());
			}
		}

		sb.append(StringUtils.join(whereClauseItems, " and "));
		sb.append(")");

		Query q = query(sb.toString());

		for (Entry<String, Object> param : params.entrySet()) {
			q.setParameter(param.getKey(), param.getValue());
		}

		@SuppressWarnings("unchecked")
		List<VoluntaryServiceStaff> resultList = q.getResultList();
		return resultList;
	}

}
