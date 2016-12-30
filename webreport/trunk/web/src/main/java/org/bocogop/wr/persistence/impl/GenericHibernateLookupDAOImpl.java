package org.bocogop.wr.persistence.impl;

import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.bocogop.shared.model.lookup.AbstractLookup;
import org.bocogop.shared.model.lookup.LookupType;
import org.bocogop.wr.persistence.dao.CustomizableLookupDAO;
import org.bocogop.wr.persistence.queryCustomization.QueryCustomization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class GenericHibernateLookupDAOImpl<T extends AbstractLookup<T, ?>> extends GenericHibernateSortedDAOImpl<T>
		implements CustomizableLookupDAO<T> {
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(GenericHibernateLookupDAOImpl.class);

	/*
	 * We use a helper so that we can ensure we're always going through the
	 * proxy (and hitting the @Cacheable functionality) - self-proxy trick using
	 * 
	 * @Resource won't work here since we're a superclass and don't know the
	 * bean name. CPB
	 */
	@Autowired
	private GenericHibernateLookupDAOHelper helper;

	// ------------------------------- Methods in GenericDAO

	@Override
	public List<T> findAll() {
		return findAll(null, null);
	}

	@Override
	public List<T> findAll(QueryCustomization customization) {
		return findAll(null, customization);
	}

	// ------------------------------- Methods in GenericSortedDAO

	@Override
	public SortedSet<T> findAllSorted() {
		return findAllSorted(null, null);
	}

	@Override
	public SortedSet<T> findAllSorted(QueryCustomization customization) {
		return findAllSorted(null, customization);
	}

	// ------------------------------- Methods in GenericLookupDAO

	public SortedSet<T> findAllSorted(Boolean active) {
		return findAllSorted(active, null);
	}

	public SortedSet<T> findAllSorted(Boolean active, QueryCustomization customization) {
		return new TreeSet<>(findAll(active, customization));
	}

	public List<T> findAll(Boolean active) {
		return findAll(active, null);
	}

	public T findByLookup(LookupType val) {
		return findById(val.getId());
	}

	// ------------------- Methods that delegate to the helper

	public List<T> findAll(Boolean active, QueryCustomization customization) {
		@SuppressWarnings("unchecked")
		List<T> results = (List<T>) helper.findAll(type, active, customization);
		return results;
	}

	public T findById(long id) {
		T item = helper.findById(type, id);
		return item;
	}

}
