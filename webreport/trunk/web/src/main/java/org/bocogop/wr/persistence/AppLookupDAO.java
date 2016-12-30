package org.bocogop.wr.persistence;

import java.util.List;
import java.util.SortedSet;

import org.bocogop.wr.model.lookup.AbstractLookup;
import org.bocogop.wr.model.lookup.LookupType;

public interface AppLookupDAO<T extends AbstractLookup<T, ?>> extends AppSortedDAO<T> {

	T findByLookup(LookupType val);

	SortedSet<T> findAllSorted(Boolean active);

	List<T> findAll(Boolean active);

}
