package org.bocogop.shared.persistence.impl;

import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;

import javax.persistence.Query;

import org.springframework.stereotype.Repository;

import com.google.common.collect.Iterables;

import org.bocogop.shared.model.AppUserFacility;
import org.bocogop.shared.model.AppUserFacilityRole;
import org.bocogop.shared.model.Role;
import org.bocogop.shared.model.core.AbstractAuditedPersistent;
import org.bocogop.shared.persistence.AppUserFacilityRoleDAO;

@Repository
public class AppUserFacilityRoleDAOImpl extends AbstractAppDAOImpl<AppUserFacilityRole>
		implements AppUserFacilityRoleDAO {

	@Override
	public void bulkAdd(long appUserId, Collection<Long> roleIDs, Collection<Long> vaFacilityIDs) {
		if (vaFacilityIDs.isEmpty())
			return;

		ZonedDateTime now = ZonedDateTime.now();

		StringBuilder sb = new StringBuilder("insert into");
		appendTypeSchemaAndTable(sb);
		sb.append("(APP_USER_FACILITY_ID, ROLE_ID,");
		sb.append(" CREATED_BY, CREATED_DATE, MODIFIED_BY, MODIFIED_DATE)");
		sb.append(" select auf.APP_USER_FACILITY_ID, r.ROLE_ID, :createdBy, :createdDate, :modifiedBy, :modifiedDate");
		sb.append(" from ");
		appendTypeSchemaAndTable(sb, AppUserFacility.class);
		sb.append(" auf, ");
		appendTypeSchemaAndTable(sb, Role.class);
		sb.append(" r where auf.APP_USER_ID = :userId and auf.FACILITY_ID in (:facilityIds)");
		sb.append(" and r.ROLE_ID in (:roleIds)");

		Iterables.partition(vaFacilityIDs, maxSupportedSQLParams).forEach(l -> {
			Query q = em.createNativeQuery(sb.toString());
			q.setParameter("userId", appUserId);
			q.setParameter("createdBy", AbstractAuditedPersistent.getCurrentUserIdForAudit());
			q.setParameter("createdDate", now);
			q.setParameter("modifiedBy", AbstractAuditedPersistent.getCurrentUserIdForAudit());
			q.setParameter("modifiedDate", now);
			q.setParameter("facilityIds", l);
			q.setParameter("roleIds", roleIDs);
			q.executeUpdate();
		});
	}

	@Override
	public int deleteByVAFacilityIDs(long appUserId, Collection<Long> vaFacilityIDs) {
		if (vaFacilityIDs.isEmpty())
			return 0;

		StringBuilder sb = new StringBuilder("delete from ").append(AppUserFacilityRole.class.getName())
				.append(" where id in (select id from ").append(AppUserFacilityRole.class.getName())
				.append(" where appUserFacility.appUser.id = :appUserId and appUserFacility.facility.id in (:ids))");

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

		StringBuilder sb = new StringBuilder("delete from ").append(AppUserFacilityRole.class.getName())
				.append(" where id in (select id from ").append(AppUserFacilityRole.class.getName())
				.append(" where appUserFacility.appUser.id in (:ids))");

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
