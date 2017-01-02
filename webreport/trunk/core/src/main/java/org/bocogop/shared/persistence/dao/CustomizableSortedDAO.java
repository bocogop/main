package org.bocogop.shared.persistence.dao;

import java.util.SortedSet;

import org.bocogop.shared.model.IdentifiedPersistent;
import org.bocogop.shared.persistence.AppSortedDAO;
import org.bocogop.shared.persistence.queryCustomization.QueryCustomization;

@SuppressWarnings("rawtypes")
public interface CustomizableSortedDAO<T extends Comparable & IdentifiedPersistent>
		extends CustomizableAppDAO<T>, AppSortedDAO<T> {

	SortedSet<T> findAllSorted(QueryCustomization customization);

	<U extends T> SortedSet<U> findAllSortedByType(Class<U> subtype, QueryCustomization customization);

}
