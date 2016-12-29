package org.bocogop.wr.persistence.impl;

import java.util.SortedSet;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.bocogop.shared.model.core.IdentifiedPersistent;
import org.bocogop.wr.persistence.dao.CustomizableSortedDAO;
import org.bocogop.wr.persistence.queryCustomization.QueryCustomization;

@SuppressWarnings("rawtypes")
public abstract class GenericHibernateSortedDAOImpl<T extends Comparable & IdentifiedPersistent>
		extends GenericHibernateDAOImpl<T> implements CustomizableSortedDAO<T> {
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(GenericHibernateSortedDAOImpl.class);

	public SortedSet<T> findAllSorted() {
		return findAllSorted(null);
	}

	public <U extends T> SortedSet<U> findAllSortedByType(Class<U> clazz) {
		return findAllSortedByType(clazz, null);
	}

	public SortedSet<T> findAllSorted(QueryCustomization customization) {
		return new TreeSet<>(findAll(customization));
	}

	public <U extends T> SortedSet<U> findAllSortedByType(Class<U> subtype, QueryCustomization customization) {
		return new TreeSet<U>(findAllByType(subtype, customization));
	}

}