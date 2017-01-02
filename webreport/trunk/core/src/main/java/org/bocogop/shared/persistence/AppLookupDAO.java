package org.bocogop.shared.persistence;

import java.util.List;
import java.util.SortedSet;

import org.bocogop.shared.model.lookup.AbstractLookup;
import org.bocogop.shared.model.lookup.LookupType;

public interface AppLookupDAO<T extends AbstractLookup<T, ?>> extends AppSortedDAO<T> {

	T findByLookup(LookupType val);

	SortedSet<T> findAllSorted(Boolean active);

	List<T> findAll(Boolean active);

}
