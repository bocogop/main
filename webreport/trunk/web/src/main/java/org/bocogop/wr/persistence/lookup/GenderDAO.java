package org.bocogop.wr.persistence.lookup;

import org.bocogop.wr.model.lookup.Gender;
import org.bocogop.wr.model.lookup.LookupType;
import org.bocogop.wr.persistence.AppSortedDAO;

public interface GenderDAO extends AppSortedDAO<Gender> {

	public Gender findByCode(String code);

	public Gender findByLookup(LookupType lookup);

}