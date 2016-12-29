package org.bocogop.shared.persistence.impl;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.persistence.Query;

import org.springframework.stereotype.Repository;

import com.google.common.collect.Iterables;

import org.bocogop.shared.model.AppUser;
import org.bocogop.shared.model.AppUserFacility;
import org.bocogop.shared.model.core.AbstractAuditedPersistent;
import org.bocogop.shared.model.lookup.sds.VAFacility;
import org.bocogop.shared.persistence.AppUserFacilityDAO;
import org.bocogop.shared.util.SecurityUtil;

@Repository
public class AppUserFacilityDAOImpl extends AbstractAppDAOImpl<AppUserFacility> implements AppUserFacilityDAO {

	@Override
	public void bulkAdd(final long userId, Collection<Long> facilityIdsToAdd, boolean rolesCustomizedForFacilities) {
		if (facilityIdsToAdd.isEmpty())
			return;

		ZonedDateTime now = ZonedDateTime.now();

		StringBuilder sb = new StringBuilder("insert into");
		appendTypeSchemaAndTable(sb);
		sb.append("(APP_USER_ID, FACILITY_ID, ROLES_CUSTOMIZED_IND, PRIMARY_FACILITY_IND,");
		sb.append(" CREATED_BY, CREATED_DATE, MODIFIED_BY, MODIFIED_DATE)");
		sb.append(" select :userId, i.id, ").append(rolesCustomizedForFacilities ? "'Y'" : "'N'")
				.append(", 'N', :createdBy, :createdDate, :modifiedBy, :modifiedDate");
		sb.append(" from ");
		appendTypeSchemaAndTable(sb, VAFacility.class);
		sb.append(" i where i.id in (:facilityIds)");

		Iterables.partition(facilityIdsToAdd, maxSupportedSQLParams).forEach(l -> {
			Query q = em.createNativeQuery(sb.toString());
			q.setParameter("userId", userId);
			q.setParameter("createdBy", AbstractAuditedPersistent.getCurrentUserIdForAudit());
			q.setParameter("createdDate", now);
			q.setParameter("modifiedBy", AbstractAuditedPersistent.getCurrentUserIdForAudit());
			q.setParameter("modifiedDate", now);
			q.setParameter("facilityIds", l);
			q.executeUpdate();
		});
	}

	@Override
	public List<AppUserFacility> findByUserSorted(long userId) {
		Query q = query("select auf from " + AppUserFacility.class.getName() + " auf left join fetch auf.facility f"
				+ " where auf.appUser.id = :userId and :currentUser in (select username from " + AppUser.class.getName()
				+ ") order by f.name").setParameter("currentUser", SecurityUtil.getCurrentUserName())
						.setParameter("userId", userId);
		@SuppressWarnings("unchecked")
		List<AppUserFacility> results = q.getResultList();
		return results;
	}

	@Override
	public VAFacility findPrimaryFacilityForUser(long userId) {
		Query q = query("select auf.facility from " + AppUserFacility.class.getName() + " auf"
				+ " where auf.appUser.id = :userId and auf.primaryFacility = true").setParameter("userId", userId);
		@SuppressWarnings("unchecked")
		List<VAFacility> results = q.getResultList();
		return results.isEmpty() ? null : results.get(0);
	}

	@Override
	public void savePrimaryFacilityForUser(long userId, long primaryFacilityId) {
		Query q = query("update " + AppUserFacility.class.getName() //
				+ " set primaryFacility = case when facility.id = :facilityId then 'Y' else 'N' end" //
				+ ", modifiedBy = :myUser, modifiedDate = :nowUTC" //
				+ " where appUser.id = :userId") //
						.setParameter("userId", userId) //
						.setParameter("facilityId", primaryFacilityId) //
						.setParameter("myUser", AbstractAuditedPersistent.getCurrentUserIdForAudit()) //
						.setParameter("nowUTC", ZonedDateTime.now(ZoneId.of("Z")));
		q.executeUpdate();
	}

	@Override
	public int deleteByVAFacilityIDs(long appUserId, Collection<Long> vaFacilityIDs) {
		if (vaFacilityIDs.isEmpty())
			return 0;

		StringBuilder sb = new StringBuilder("delete from ").append(AppUserFacility.class.getName())
				.append(" where appUser.id = :appUserId and facility.id in (:ids)");

		/*
		 * Consider using Spliterator for parallel execution if needed - CPB
		 */
		AtomicInteger ai = new AtomicInteger(0);
		Iterables.partition(vaFacilityIDs, maxSupportedSQLParams).forEach(l -> {
			Query q = em.createQuery(sb.toString());
			q.setParameter("ids", l);
			q.setParameter("appUserId", appUserId);
			ai.addAndGet(q.executeUpdate());
		});

		return ai.get();
	}

	@Override
	public int deleteByUsers(Collection<Long> userIDs) {
		if (userIDs.isEmpty())
			return 0;

		StringBuilder sb = new StringBuilder("delete from ").append(AppUserFacility.class.getName())
				.append(" where appUser.id in (:ids)");

		/*
		 * Consider using Spliterator for parallel execution if needed - CPB
		 */
		AtomicInteger ai = new AtomicInteger(0);
		Iterables.partition(userIDs, maxSupportedSQLParams).forEach(l -> {
			Query q = em.createQuery(sb.toString());
			q.setParameter("ids", l);
			ai.addAndGet(q.executeUpdate());
		});

		return ai.get();
	}

}
