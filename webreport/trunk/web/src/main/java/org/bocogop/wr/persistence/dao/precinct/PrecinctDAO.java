package org.bocogop.wr.persistence.dao.precinct;

import java.util.SortedSet;

import org.bocogop.wr.model.precinct.Precinct;
import org.bocogop.wr.persistence.dao.CustomizableSortedDAO;

/**
 *
 */
public interface PrecinctDAO extends CustomizableSortedDAO<Precinct> {

	SortedSet<Precinct> findByCriteria(String code, String name);

}
