package org.bocogop.wr.persistence.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.hibernate.jpa.QueryHints;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.Iterables;

import org.bocogop.shared.model.core.IdentifiedPersistent;
import org.bocogop.shared.persistence.impl.AbstractAppDAOImpl;
import org.bocogop.shared.util.TypeUtil;
import org.bocogop.shared.util.cache.CacheUtil;
import org.bocogop.wr.persistence.dao.CustomizableAppDAO;
import org.bocogop.wr.persistence.queryCustomization.QueryCustomization;
import org.bocogop.wr.util.DateUtil;

public abstract class GenericHibernateDAOImpl<T extends IdentifiedPersistent> extends AbstractAppDAOImpl<T>
		implements CustomizableAppDAO<T> {

	@Autowired
	protected DateUtil dateUtil;

	@SuppressWarnings("unchecked")
	public GenericHibernateDAOImpl() {
		type = (Class<? extends T>) TypeUtil.getFirstTypeParameterClass(this);
		if (Comparable.class.isAssignableFrom(type)
				&& !GenericHibernateSortedDAOImpl.class.isAssignableFrom(getClass()))
			throw new IllegalStateException(
					"The class " + getClass() + " extends " + GenericHibernateDAOImpl.class.getName()
							+ " but should extend " + GenericHibernateSortedDAOImpl.class.getName() + " since the type "
							+ type + " implements " + Comparable.class.getSimpleName());
	}

	@SuppressWarnings("unchecked")
	public List<T> findAll(QueryCustomization customization) {
		return (List<T>) findAllByType(type, customization);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <U extends T> List<U> findAllByType(Class<U> subtype, QueryCustomization customization) {
		StringBuilder sb = new StringBuilder("from ").append(subtype.getName()).append(" o");
		if (customization != null)
			customization.appendRemainingJoins(sb, "o");
		Query q = em.createQuery(sb.toString());
		if (customization != null)
			customization.applyQueryModifications(q);

		if ((customization == null || !customization.hasPrefetchFields()) && CacheUtil.isReadOnly(subtype))
			q.setHint(QueryHints.HINT_CACHEABLE, "true");

		return q.getResultList();
	}

	@Override
	public Map<Long, T> findByPrimaryKeys(Collection<Long> primaryKeys, QueryCustomization customization) {
		Map<Long, T> resultMap = new LinkedHashMap<Long, T>();
		if (primaryKeys.isEmpty()) {
			return resultMap;
		} else if (primaryKeys.size() == 1) {
			Long key = primaryKeys.iterator().next();
			T item = findByPrimaryKey(key, customization);
			resultMap.put(key, item);
		} else {
			StringBuilder sb = new StringBuilder("from ").append(type.getName()).append(" o");
			if (customization != null)
				customization.appendRemainingJoins(sb, "o");
			sb.append(" where o.id in (:ids)");
			if (customization != null)
				customization.appendOrderBy(sb);

			/*
			 * Research using Spliterator for parallel execution - CPB
			 */
			Iterables.partition(primaryKeys, maxSupportedSQLParams).forEach(l -> {
				Query q = em.createQuery(sb.toString());
				if (customization != null)
					customization.applyQueryModifications(q);
				q.setParameter("ids", l);

				@SuppressWarnings("unchecked")
				List<T> resultList = q.getResultList();
				for (T result : resultList)
					resultMap.put(result.getId(), result);
			});
		}
		return resultMap;
	}

	@Override
	public Map<Long, T> findRequiredByPrimaryKeys(Collection<Long> primaryKeys, QueryCustomization customization) {
		Map<Long, T> item = findByPrimaryKeys(primaryKeys, customization);
		if (item.size() != primaryKeys.size()) {
			List<Long> invalidKeys = new ArrayList<>(primaryKeys);
			invalidKeys.removeAll(item.keySet());
			throw new IllegalArgumentException("No " + type.getSimpleName() + " was found for the following IDs: "
					+ StringUtils.join(invalidKeys, ", "));
		}
		return item;
	}

	@SuppressWarnings("unchecked")
	public T findByPrimaryKey(Long id, QueryCustomization customization) {
		if (id == null)
			throw new IllegalArgumentException("ID provided to findByPrimaryKey was null");

		if (customization == null)
			return em.find(type, id);

		StringBuilder sb = new StringBuilder("select distinct o from ").append(type.getName()).append(" o");
		customization.appendRemainingJoins(sb, "o");
		sb.append(" where o.id = :id");
		Query q = em.createQuery(sb.toString());
		customization.applyQueryModifications(q);
		q.setParameter("id", id);

		try {
			return (T) q.getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}

	public T findRequiredByPrimaryKey(Long id, QueryCustomization customization) {
		T item = findByPrimaryKey(id, customization);
		if (item == null)
			throw new IllegalArgumentException("No " + type.getSimpleName() + " with ID " + id + " was found.");
		return item;
	}

	/* -------------------------- Utility methods for subclasses */

	/**
	 * In many cases, our DAOs follow the pattern of dynamically building a List
	 * of where clause items, parameters to set and applying a query
	 * customization. This method abstracts that functionality, but DAOs are
	 * also of course free to build queries as needed. CPB
	 * 
	 * @param em
	 *            TODO
	 * @param sb
	 *            The StringBuilder which already contains the select clause
	 * @param whereClauseItems
	 *            The list of items to use for the "where" section of the JPQL
	 *            query (not including the words "where" or "and")
	 * @param params
	 *            The parameters to set in the JPQL query
	 * @param groupBy
	 *            The "group by" comma-separated list of items (not including
	 *            the words "group by")
	 * @param customizations
	 *            The optional query customization to consider. This method only
	 *            considers those that have the start index / row limitation
	 *            fields set. Only the first QueryCustomization will be
	 *            respected; we are just using the varargs syntax to allow for
	 *            an "optional" parameter in subclasses.
	 * @return
	 */
	public static Query constructQuery(EntityManager em, StringBuilder sb, List<String> whereClauseItems,
			Map<String, Object> params, String groupBy, QueryCustomization... customization) {
		for (ListIterator<String> it = whereClauseItems.listIterator(); it.hasNext();) {
			sb.append(it.hasPrevious() ? " and ( " : " where ( ").append(it.next()).append(" )");
		}

		QueryCustomization c = ArrayUtils.isNotEmpty(customization) ? customization[0] : new QueryCustomization();

		if (StringUtils.isNotEmpty(groupBy))
			sb.append(" group by ").append(groupBy);
		c.appendOrderBy(sb);

		Query q = em.createQuery(sb.toString());

		c.applyQueryModifications(q);

		for (Map.Entry<String, Object> entry : params.entrySet()) {
			q.setParameter(entry.getKey(), entry.getValue());
		}

		return q;
	}

}
