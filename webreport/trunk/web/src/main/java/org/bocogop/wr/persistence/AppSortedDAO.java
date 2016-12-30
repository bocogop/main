package org.bocogop.wr.persistence;

import java.util.SortedSet;

import org.bocogop.wr.model.core.IdentifiedPersistent;

@SuppressWarnings("rawtypes")
public interface AppSortedDAO<T extends Comparable & IdentifiedPersistent> extends AppDAO<T> {

	SortedSet<T> findAllSorted();

	<U extends T> SortedSet<U> findAllSortedByType(Class<U> clazz);

}
