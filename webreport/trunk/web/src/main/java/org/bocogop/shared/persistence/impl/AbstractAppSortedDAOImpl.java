package org.bocogop.shared.persistence.impl;

import java.util.SortedSet;
import java.util.TreeSet;

import org.bocogop.shared.model.core.IdentifiedPersistent;
import org.bocogop.shared.persistence.AppSortedDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("rawtypes")
public abstract class AbstractAppSortedDAOImpl<T extends Comparable & IdentifiedPersistent>
		extends AbstractAppDAOImpl<T> implements AppSortedDAO<T> {
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(AbstractAppSortedDAOImpl.class);

	public SortedSet<T> findAllSorted() {
		return new TreeSet<>(findAll());
	}

	public <U extends T> SortedSet<U> findAllSortedByType(Class<U> subtype) {
		return new TreeSet<U>(findAllByType(subtype));
	}

}
