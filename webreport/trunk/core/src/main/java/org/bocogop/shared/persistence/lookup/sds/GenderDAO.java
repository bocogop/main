package org.bocogop.shared.persistence.lookup.sds;

import org.bocogop.shared.model.lookup.LookupType;
import org.bocogop.shared.model.lookup.sds.Gender;
import org.bocogop.shared.persistence.AppSortedDAO;

public interface GenderDAO extends AppSortedDAO<Gender> {

	public Gender findByCode(String code);

	public Gender findByLookup(LookupType lookup);

}