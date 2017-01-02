package org.bocogop.shared.persistence.impl;

import java.time.ZonedDateTime;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.bocogop.shared.persistence.queryCustomization.QueryCustomization;
import org.bocogop.shared.util.cache.CacheUtil;
import org.hibernate.jpa.QueryHints;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

/* Helper class that allows the main GenericHibernateLookupDAO to always access these
 * methods through a proxy - CPB */
@Repository
public class GenericHibernateLookupDAOHelper {
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(GenericHibernateLookupDAOHelper.class);

	@PersistenceContext
	protected EntityManager em;

	@SuppressWarnings("unchecked")
	public <T> List<T> findAll(Class<T> type, Boolean active, QueryCustomization customization) {
		StringBuilder sb = new StringBuilder("from ").append(type.getName()).append(" o");

		if (customization != null)
			customization.appendRemainingJoins(sb, "o");

		if (active != null) {
			if (active) {
				sb.append(" where COALESCE(o.effectiveDate,'1900-01-01') <= :now");
				sb.append(" and COALESCE(o.expirationDate,'2199-01-01') > :now");
			} else {
				sb.append(" where COALESCE(o.effectiveDate,'1900-01-01') > :now");
				sb.append(" or COALESCE(o.expirationDate,'2199-01-01') < :now");
			}
		}

		Query q = em.createQuery(sb.toString());

		if (active != null) {
			q.setParameter("now", ZonedDateTime.now());
		}

		if (customization != null)
			customization.applyQueryModifications(q);

		if (CacheUtil.isReadOnly(type))
			q.setHint(QueryHints.HINT_CACHEABLE, "true");

		return q.getResultList();
	}

	@SuppressWarnings("unchecked")
	public <T> T findById(Class<T> type, long id) {
		Query query = em.createQuery("from " + type.getName() + " where id = :id").setParameter("id", id);

		if (CacheUtil.isReadOnly(type))
			query.setHint(QueryHints.HINT_CACHEABLE, "true");
		try {
			return (T) query.getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}

}
