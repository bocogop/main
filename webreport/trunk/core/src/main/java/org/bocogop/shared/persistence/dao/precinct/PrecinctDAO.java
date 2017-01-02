package org.bocogop.shared.persistence.dao.precinct;

import java.util.SortedSet;

import org.bocogop.shared.model.precinct.Precinct;
import org.bocogop.shared.persistence.dao.CustomizableSortedDAO;

/**
 *
 */
public interface PrecinctDAO extends CustomizableSortedDAO<Precinct> {

	SortedSet<Precinct> findByCriteria(String code, String name);

}
