package org.bocogop.shared.persistence.dao;

import org.bocogop.shared.model.lookup.Country;
import org.bocogop.shared.model.lookup.LookupType;
import org.bocogop.shared.persistence.AppSortedDAO;

public interface CountryDAO extends AppSortedDAO<Country> {

	public Country findByCode(String code);

	public Country findByLookup(LookupType lookup);

}