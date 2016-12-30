package org.bocogop.shared.persistence.impl;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.bocogop.shared.util.cache.CacheUtil;
import org.hibernate.annotations.QueryHints;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

@Repository
public class AppLookupDAOHelper {
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(AppLookupDAOHelper.class);

	@PersistenceContext
	protected EntityManager em;

	@SuppressWarnings("unchecked")
	public <T> List<T> findAll(Class<T> type, Boolean active) {
		StringBuilder sb = new StringBuilder("from ").append(type.getName()).append(" o");

		if (active != null) {
			sb.append(" where o.effectiveDate <= :now");
		}

		Query q = em.createQuery(sb.toString());

		if (active != null) {
			q.setParameter("now", ZonedDateTime.now(ZoneId.of("Z")));
		}

		if (CacheUtil.isReadOnly(type) && active == null)
			q.setHint(QueryHints.CACHEABLE, "true");

		return q.getResultList();
	}

	@SuppressWarnings("unchecked")
	public <T> T findById(Class<T> type, long id) {
		Query query = em.createQuery("from " + type.getName() + " where id = :id").setParameter("id", id);

		if (CacheUtil.isReadOnly(type))
			query.setHint(QueryHints.CACHEABLE, "true");
		try {
			return (T) query.getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}

}
