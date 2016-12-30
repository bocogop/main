package org.bocogop.shared.persistence.impl;

import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.persistence.Query;

import org.bocogop.shared.model.AppUserGlobalRole;
import org.bocogop.shared.model.Role;
import org.bocogop.shared.model.core.AbstractAuditedPersistent;
import org.bocogop.shared.persistence.AppUserGlobalRoleDAO;
import org.springframework.stereotype.Repository;

import com.google.common.collect.Iterables;

@Repository
public class AppUserGlobalRoleDAOImpl extends AbstractAppDAOImpl<AppUserGlobalRole> implements AppUserGlobalRoleDAO {

	@Override
	public void bulkAdd(final long userId, Collection<Long> roleIdsToAdd) {
		if (roleIdsToAdd.isEmpty())
			return;

		ZonedDateTime now = ZonedDateTime.now();

		StringBuilder sb = new StringBuilder("insert into");
		appendTypeSchemaAndTable(sb);
		sb.append("(APP_USER_ID, ROLE_ID, CREATED_BY, CREATED_DATE, MODIFIED_BY, MODIFIED_DATE)");
		sb.append(" select :userId, r.ROLE_ID, :createdBy, :createdDate, :modifiedBy, :modifiedDate");
		sb.append(" from ");
		appendTypeSchemaAndTable(sb, Role.class);
		sb.append(" r where r.ROLE_ID in (:roleIds)");

		Iterables.partition(roleIdsToAdd, maxSupportedSQLParams).forEach(l -> {
			Query q = em.createNativeQuery(sb.toString());
			q.setParameter("userId", userId);
			q.setParameter("createdBy", AbstractAuditedPersistent.getCurrentUserIdForAudit());
			q.setParameter("createdDate", now);
			q.setParameter("modifiedBy", AbstractAuditedPersistent.getCurrentUserIdForAudit());
			q.setParameter("modifiedDate", now);
			q.setParameter("roleIds", l);
			q.executeUpdate();
		});
	}

	@Override
	public int deleteByRoleIds(long userId, List<Long> roleIds) {
		if (roleIds.isEmpty())
			return 0;

		StringBuilder sb = new StringBuilder("delete from ").append(AppUserGlobalRole.class.getName())
				.append(" where appUser.id = :appUserId and role.id in (:ids)");

		/*
		 * Consider using Spliterator for parallel execution if needed - CPB
		 */
		AtomicInteger ai = new AtomicInteger(0);
		Iterables.partition(roleIds, maxSupportedSQLParams).forEach(l -> {
			Query q = em.createQuery(sb.toString());
			q.setParameter("ids", l);
			q.setParameter("appUserId", userId);
			ai.addAndGet(q.executeUpdate());
		});

		return ai.get();
	}
	
	@Override
	public int deleteByUsers(List<Long> userIds) {
		if (userIds.isEmpty())
			return 0;

		StringBuilder sb = new StringBuilder("delete from ").append(AppUserGlobalRole.class.getName())
				.append(" where appUser.id in (:ids)");

		/*
		 * Consider using Spliterator for parallel execution if needed - CPB
		 */
		AtomicInteger ai = new AtomicInteger(0);
		Iterables.partition(userIds, maxSupportedSQLParams).forEach(l -> {
			Query q = em.createQuery(sb.toString());
			q.setParameter("ids", l);
			ai.addAndGet(q.executeUpdate());
		});

		return ai.get();
	}

}
