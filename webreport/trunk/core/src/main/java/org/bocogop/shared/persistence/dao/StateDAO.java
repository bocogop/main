package org.bocogop.shared.persistence.dao;

import java.util.List;

import org.bocogop.shared.model.lookup.LookupType;
import org.bocogop.shared.model.lookup.State;
import org.bocogop.shared.persistence.AppSortedDAO;

public interface StateDAO extends AppSortedDAO<State> {

	public State findByCode(String code);

	public State findByLookup(LookupType lookup);

	public List<State> findAllSortedByCountry();

}