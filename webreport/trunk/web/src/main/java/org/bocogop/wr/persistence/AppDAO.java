package org.bocogop.wr.persistence;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.persistence.LockModeType;

import org.bocogop.wr.model.core.Persistent;

public interface AppDAO<T> {

	T findByPrimaryKey(Long id);

	T findRequiredByPrimaryKey(Long id);

	Map<Long, T> findByPrimaryKeys(Collection<Long> primaryKeys);

	Map<Long, T> findRequiredByPrimaryKeys(Collection<Long> primaryKeys);

	List<T> findAll();
	
	<U extends T> List<U> findAllByType(Class<U> subtype);

	List<T> findSome(int num);
	
	List<T> findAtMost(int num);

	<U extends T> List<U> findSomeByType(Class<U> subtype, int num);
	
	<U extends T> List<U> findAtMostByType(Class<U> subtype, int num);
	
	T saveOrUpdate(T entity);

	/** Requests the EntityManger to flush any pending changes. */
	void flush();

	void detach(T item);

	/**
	 * Flushes the EntityManager and then refreshes the object state from the
	 * database. This method is necessary when we are refreshing the object to
	 * pick up a field set by a database trigger, but still want to push any
	 * queued changes to the database. If we simply called refresh(), any queued
	 * changes would be discarded. CPB
	 */
	void flushAndRefresh(T entity);

	void refresh(T entity);

	void delete(long entityId);

	void delete(T item);
	
	/**
	 * Performs a bulk "delete where ID in (...)" statement on the root table
	 * entity only. Assumes that callers will have bulk-deleted any children
	 * objects already, or will have defined a DB-level cascade trigger on the
	 * foreign key. CPB
	 */
	int deleteByPrimaryKeys(Collection<Long> primaryKeys);

	int deleteAll();

	/**
	 * Deletes a collection of entities from a database
	 * 
	 * @param entities
	 *            A collection to be deleted @ thrown if failed to delete from a
	 *            database
	 */
	public void deleteAll(Collection<? extends Persistent> entities);

	void lock(T entity, LockModeType type);

	int getTotalNumber();

	int[] getTotalAndFilteredNumber(String filterString, Map<String, Object> paramMap);


}
