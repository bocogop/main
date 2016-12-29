package org.bocogop.shared.persistence.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.Table;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.QueryHints;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.core.env.Environment;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.google.common.collect.Iterables;

import org.bocogop.shared.model.core.IdentifiedPersistent;
import org.bocogop.shared.model.core.Persistent;
import org.bocogop.shared.persistence.AppDAO;
import org.bocogop.shared.util.TypeUtil;
import org.bocogop.shared.util.cache.CacheUtil;

public abstract class AbstractAppDAOImpl<T extends IdentifiedPersistent> implements AppDAO<T> {

	public static boolean FLUSH_EVERY_OPERATION = false;

	protected Class<? extends T> type;
	@Value("${maxSupportedSQLParams}")
	protected int maxSupportedSQLParams;

	@PersistenceContext
	protected EntityManager em;
	@Autowired
	private Environment env;
	@Autowired
	protected MessageSource messageSource;

	@SuppressWarnings("unchecked")
	public AbstractAppDAOImpl() {
		type = (Class<? extends T>) TypeUtil.getFirstTypeParameterClass(this);
	}

	protected void flushIfDebug() {
		if (FLUSH_EVERY_OPERATION && TransactionSynchronizationManager.isActualTransactionActive())
			flush();
	}

	protected boolean isUnitTest() {
		return env.acceptsProfiles("default");
	}

	protected boolean dataChangesAllowedOutsideUnitTest() {
		return true;
	}

	protected void appendTypeSchemaAndTable(StringBuilder sb) {
		appendTypeSchemaAndTable(sb, type);
	}

	protected void appendTypeSchemaAndTable(StringBuilder sb, Class<? extends Persistent> t) {
		Table annotation = t.getAnnotation(Table.class);
		final String schema = annotation.schema();
		final String tableName = annotation.name();
		sb.append(" ").append(schema).append(".").append(tableName).append(" ");
	}

	@SuppressWarnings("unchecked")
	public List<T> findAll() {
		return (List<T>) findAllByType(type);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <U extends T> List<U> findAllByType(Class<U> subtype) {
		StringBuilder sb = new StringBuilder("from ").append(subtype.getName()).append(" o");
		Query q = em.createQuery(sb.toString());

		if (CacheUtil.isReadOnly(subtype))
			q.setHint(QueryHints.CACHEABLE, "true");

		return q.getResultList();
	}
	
	public List<T> findAtMost(int num) {
		@SuppressWarnings("unchecked")
		List<T> results = (List<T>) findAtMostByType(type, num);
		return results;
	}

	public List<T> findSome(int num) {
		@SuppressWarnings("unchecked")
		List<T> results = (List<T>) findSomeByType(type, num);
		if (results.size() != num)
			throw new RuntimeException("Couldn't find " + num + " items of type " + type.getSimpleName());
		return results;
	}

	@Override
	public <U extends T> List<U> findSomeByType(Class<U> subtype, int num) {
		List<U> results = findAtMostByType(subtype, num);
		if (results.size() != num)
			throw new RuntimeException("Couldn't find " + num + " items of type " + subtype.getSimpleName());
		return results;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <U extends T> List<U> findAtMostByType(Class<U> subtype, int num) {
		StringBuilder sb = new StringBuilder("from ").append(subtype.getName()).append(" o");
		Query q = em.createQuery(sb.toString());
		q.setMaxResults(num);
		if (CacheUtil.isReadOnly(subtype))
			q.setHint(QueryHints.CACHEABLE, "true");

		List<U> results = q.getResultList();
		return results;
	}

	public SortedSet<T> findAllSorted() {
		return new TreeSet<>(findAll());
	}

	public int getTotalNumber() {
		Query query = em.createQuery("select count(*) from " + type.getName());
		return ((Long) query.getSingleResult()).intValue();
	}

	public int[] getTotalAndFilteredNumber(String subsetCriteria, Map<String, Object> paramMap) {
		if (StringUtils.isBlank(subsetCriteria))
			subsetCriteria = "1=1";

		Query query = em.createQuery("select count(*), sum(case when " + subsetCriteria + " then 1 else 0 end) from "
				+ type.getName() + " o");
		if (paramMap != null)
			for (Entry<String, Object> entry : paramMap.entrySet())
				query.setParameter(entry.getKey(), entry.getValue());
		Object[] r = (Object[]) query.getSingleResult();
		int totalCount = ((Number) r[0]).intValue();
		Number subsetCount = ((Number) r[1]);
		return new int[] { totalCount, subsetCount == null ? 0 : subsetCount.intValue() };
	}

	@Override
	public Map<Long, T> findByPrimaryKeys(Collection<Long> primaryKeys) {
		Map<Long, T> resultMap = new LinkedHashMap<Long, T>();
		if (primaryKeys == null || primaryKeys.isEmpty()) {
			return resultMap;
		} else if (primaryKeys.size() == 1) {
			Long key = primaryKeys.iterator().next();
			T item = findByPrimaryKey(key);
			resultMap.put(key, item);
		} else {
			StringBuilder sb = new StringBuilder("from ").append(type.getName()).append(" o");
			sb.append(" where o.id in (:ids)");

			/*
			 * Consider using Spliterator for parallel execution if needed - CPB
			 */
			Iterables.partition(primaryKeys, maxSupportedSQLParams).forEach(l -> {
				Query q = em.createQuery(sb.toString());
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
	public Map<Long, T> findRequiredByPrimaryKeys(Collection<Long> primaryKeys) {
		Map<Long, T> item = findByPrimaryKeys(primaryKeys);
		if (item.size() != primaryKeys.size()) {
			List<Long> invalidKeys = new ArrayList<>(primaryKeys);
			invalidKeys.removeAll(item.keySet());
			throw new IllegalArgumentException("No " + type.getSimpleName() + " was found for the following IDs: "
					+ StringUtils.join(invalidKeys, ", "));
		}
		return item;
	}

	public T findByPrimaryKey(Long id) {
		if (id == null)
			throw new IllegalArgumentException("A null ID was passed in to findByPrimaryKey()");
		return em.find(type, id);
	}

	public T findRequiredByPrimaryKey(Long id) {
		T item = findByPrimaryKey(id);
		if (item == null)
			throw new IllegalArgumentException("No " + type.getSimpleName() + " with ID " + id + " was found.");
		return item;
	}

	public T saveOrUpdate(T item) {
		checkDataMods();
		item = em.merge(item);
		flushIfDebug();
		return item;
	}

	private void checkDataMods() {
		if (!dataChangesAllowedOutsideUnitTest() && !isUnitTest())
			throw new IllegalStateException("Can't make changes to a " + type.getSimpleName() + " outside a unit test");
	}

	public void flush() {
		em.flush();
	}

	public void flushAndRefresh(T item) {
		flush();
		refresh(item);
	}

	public void detach(T item) {
		em.detach(item);
	}

	public void refresh(T item) {
		em.refresh(item);
	}

	public void delete(T item) {
		checkDataMods();
		em.remove(item);
		flushIfDebug();
	}

	public void delete(long id) {
		delete(findRequiredByPrimaryKey(id));
	}

	@Override
	public int deleteAll() {
		checkDataMods();
		int rowsModified = query("delete from " + type.getName()).executeUpdate();
		flushIfDebug();
		return rowsModified;
	}

	public int deleteByPrimaryKeys(Collection<Long> primaryKeys) {
		if (primaryKeys.isEmpty())
			return 0;

		StringBuilder sb = new StringBuilder("delete from ").append(type.getName()).append(" where id in (:ids)");

		/*
		 * Consider using Spliterator for parallel execution if needed - CPB
		 */
		AtomicInteger ai = new AtomicInteger(0);
		Iterables.partition(primaryKeys, maxSupportedSQLParams).forEach(l -> {
			Query q = em.createQuery(sb.toString());
			q.setParameter("ids", l);
			ai.addAndGet(q.executeUpdate());
		});

		return ai.get();
	}

	public void deleteAll(Collection<? extends Persistent> entities) {
		checkDataMods();
		for (Object obj : entities)
			em.remove(obj);
		flushIfDebug();
	}

	/* -------------------------- Utility methods for subclasses */

	protected Query query(String jpql) {
		return em.createQuery(jpql);
	}

	public void lock(T entity, LockModeType lockMode) {
		em.lock(entity, lockMode);
	}

	/**
	 * In many cases, our DAOs follow the pattern of dynamically building a List
	 * of where clause items, parameters to set and applying a query
	 * customization. This method abstracts that functionality, but DAOs are
	 * also of course free to build queries as needed. CPB
	 * 
	 * @param em
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
	 * @return
	 */
	public static Query constructQuery(EntityManager em, StringBuilder sb, List<String> whereClauseItems,
			Map<String, Object> params, String orderBy, String groupBy) {
		for (ListIterator<String> it = whereClauseItems.listIterator(); it.hasNext();) {
			sb.append(it.hasPrevious() ? " and " : " where ").append(it.next());
		}

		if (StringUtils.isNotEmpty(groupBy))
			sb.append(" group by ").append(groupBy);

		Query q = em.createQuery(sb.toString());

		for (Map.Entry<String, Object> entry : params.entrySet()) {
			q.setParameter(entry.getKey(), entry.getValue());
		}

		return q;
	}

}
