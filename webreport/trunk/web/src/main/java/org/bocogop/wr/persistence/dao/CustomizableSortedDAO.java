package org.bocogop.wr.persistence.dao;

import java.util.SortedSet;

import org.bocogop.shared.model.core.IdentifiedPersistent;
import org.bocogop.shared.persistence.AppSortedDAO;
import org.bocogop.wr.persistence.queryCustomization.QueryCustomization;

@SuppressWarnings("rawtypes")
public interface CustomizableSortedDAO<T extends Comparable & IdentifiedPersistent>
		extends CustomizableAppDAO<T>, AppSortedDAO<T> {

	SortedSet<T> findAllSorted(QueryCustomization customization);

	<U extends T> SortedSet<U> findAllSortedByType(Class<U> subtype, QueryCustomization customization);

}
