package org.bocogop.shared.persistence;

import java.util.SortedSet;

import org.bocogop.shared.model.core.IdentifiedPersistent;

@SuppressWarnings("rawtypes")
public interface AppSortedDAO<T extends Comparable & IdentifiedPersistent> extends AppDAO<T> {

	SortedSet<T> findAllSorted();

	<U extends T> SortedSet<U> findAllSortedByType(Class<U> clazz);

}
