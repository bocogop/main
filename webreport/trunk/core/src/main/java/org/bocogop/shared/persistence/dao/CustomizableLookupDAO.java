package org.bocogop.shared.persistence.dao;

import java.util.List;
import java.util.SortedSet;

import org.bocogop.shared.model.lookup.AbstractLookup;
import org.bocogop.shared.model.lookup.LookupType;
import org.bocogop.shared.persistence.queryCustomization.QueryCustomization;

public interface CustomizableLookupDAO<T extends AbstractLookup<T, ?>> extends CustomizableSortedDAO<T> {

	T findByLookup(LookupType val);

	SortedSet<T> findAllSorted(Boolean active);

	SortedSet<T> findAllSorted(Boolean active, QueryCustomization customization);

	List<T> findAll(Boolean active);

	List<T> findAll(Boolean includeOnlyActive, QueryCustomization customization);

}
