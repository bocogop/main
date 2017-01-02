package org.bocogop.shared.persistence.impl;

import java.util.SortedSet;
import java.util.TreeSet;

import org.bocogop.shared.model.IdentifiedPersistent;
import org.bocogop.shared.persistence.dao.CustomizableSortedDAO;
import org.bocogop.shared.persistence.queryCustomization.QueryCustomization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
