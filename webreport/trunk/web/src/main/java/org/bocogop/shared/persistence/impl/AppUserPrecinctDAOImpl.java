package org.bocogop.shared.persistence.impl;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.persistence.Query;

import org.bocogop.shared.model.AppUser;
import org.bocogop.shared.model.AppUserPrecinct;
import org.bocogop.shared.model.core.AbstractAuditedPersistent;
import org.bocogop.shared.persistence.AppUserPrecinctDAO;
import org.bocogop.shared.util.SecurityUtil;
import org.bocogop.wr.model.precinct.Precinct;
import org.springframework.stereotype.Repository;

import com.google.common.collect.Iterables;

@Repository
public class AppUserPrecinctDAOImpl extends AbstractAppDAOImpl<AppUserPrecinct> implements AppUserPrecinctDAO {

	@Override
	public void bulkAdd(final long userId, Collection<Long> precinctIdsToAdd) {
		if (precinctIdsToAdd.isEmpty())
			return;

		ZonedDateTime now = ZonedDateTime.now();

		StringBuilder sb = new StringBuilder("insert into");
		appendTypeSchemaAndTable(sb);
		sb.append("(APP_USER_ID, PRECINCT_ID, PRIMARY_PRECINCT_IND,");
		sb.append(" CREATED_BY, CREATED_DATE, MODIFIED_BY, MODIFIED_DATE)");
		sb.append(" select :userId, i.id, 'N', :createdBy, :createdDate, :modifiedBy, :modifiedDate");
		sb.append(" from ");
		appendTypeSchemaAndTable(sb, Precinct.class);
		sb.append(" i where i.id in (:precinctIds)");

		Iterables.partition(precinctIdsToAdd, maxSupportedSQLParams).forEach(l -> {
			Query q = em.createNativeQuery(sb.toString());
			q.setParameter("userId", userId);
			q.setParameter("createdBy", AbstractAuditedPersistent.getCurrentUserIdForAudit());
			q.setParameter("createdDate", now);
			q.setParameter("modifiedBy", AbstractAuditedPersistent.getCurrentUserIdForAudit());
			q.setParameter("modifiedDate", now);
			q.setParameter("precinctIds", l);
			q.executeUpdate();
		});
	}

	@Override
	public List<AppUserPrecinct> findByUserSorted(long userId) {
		Query q = query("select auf from " + AppUserPrecinct.class.getName() + " auf left join fetch auf.precinct f"
				+ " where auf.appUser.id = :userId and :currentUser in (select username from " + AppUser.class.getName()
				+ ") order by f.name").setParameter("currentUser", SecurityUtil.getCurrentUserName())
						.setParameter("userId", userId);
		@SuppressWarnings("unchecked")
		List<AppUserPrecinct> results = q.getResultList();
		return results;
	}

	@Override
	public Precinct findPrimaryPrecinctForUser(long userId) {
		Query q = query("select auf.precinct from " + AppUserPrecinct.class.getName() + " auf"
				+ " where auf.appUser.id = :userId and auf.primaryPrecinct = true").setParameter("userId", userId);
		@SuppressWarnings("unchecked")
		List<Precinct> results = q.getResultList();
		return results.isEmpty() ? null : results.get(0);
	}

	@Override
	public void savePrimaryPrecinctForUser(long userId, long primaryPrecinctId) {
		Query q = query("update " + AppUserPrecinct.class.getName() //
				+ " set primaryPrecinct = case when precinct.id = :precinctId then 'Y' else 'N' end" //
				+ ", modifiedBy = :myUser, modifiedDate = :nowUTC" //
				+ " where appUser.id = :userId") //
						.setParameter("userId", userId) //
						.setParameter("precinctId", primaryPrecinctId) //
						.setParameter("myUser", AbstractAuditedPersistent.getCurrentUserIdForAudit()) //
						.setParameter("nowUTC", ZonedDateTime.now(ZoneId.of("Z")));
		q.executeUpdate();
	}

	@Override
	public int deleteByPrecinctIDs(long appUserId, Collection<Long> precinctIDs) {
		if (precinctIDs.isEmpty())
			return 0;

		StringBuilder sb = new StringBuilder("delete from ").append(AppUserPrecinct.class.getName())
				.append(" where appUser.id = :appUserId and precinct.id in (:ids)");

		/*
		 * Consider using Spliterator for parallel execution if needed - CPB
		 */
		AtomicInteger ai = new AtomicInteger(0);
		Iterables.partition(precinctIDs, maxSupportedSQLParams).forEach(l -> {
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

		StringBuilder sb = new StringBuilder("delete from ").append(AppUserPrecinct.class.getName())
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
