package org.bocogop.shared.persistence.impl;

import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.bocogop.shared.model.lookup.AbstractLookup;
import org.bocogop.shared.model.lookup.LookupType;
import org.bocogop.shared.persistence.AppLookupDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class AbstractAppLookupDAOImpl<T extends AbstractLookup<T, ?>> extends AbstractAppSortedDAOImpl<T>
		implements AppLookupDAO<T> {
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(AbstractAppLookupDAOImpl.class);

	/*
	 * We use a helper so that we can ensure we're always going through the
	 * proxy (and hitting the @Cacheable functionality) - self-proxy trick using
	 * 
	 * @Resource won't work here since we're a superclass and don't know the
	 * bean name. CPB
	 */
	@Autowired
	private AppLookupDAOHelper helper;

	// ------------------------------- Methods in GenericDAO

	@Override
	public List<T> findAll() {
		return findAll(null);
	}

	// ------------------------------- Methods in GenericSortedDAO

	@Override
	public SortedSet<T> findAllSorted() {
		return findAllSorted(null);
	}

	// ------------------------------- Methods in GenericLookupDAO

	public SortedSet<T> findAllSorted(Boolean active) {
		return new TreeSet<>(findAll(active));
	}

	public T findByLookup(LookupType val) {
		T t = findById(val.getId());
		if (t == null)
			throw new IllegalArgumentException(
					"Missing database entry for lookup value " + val.getClass().getName() + " with ID " + val.getId());
		return t;
	}

	// ------------------- Methods that delegate to the helper

	public List<T> findAll(Boolean active) {
		@SuppressWarnings("unchecked")
		List<T> results = (List<T>) helper.findAll(type, active);
		return results;
	}

	public T findById(long id) {
		T item = helper.findById(type, id);
		return item;
	}

}
