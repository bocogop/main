package org.bocogop.shared.persistence.dao;

import org.bocogop.shared.model.lookup.LookupType;
import org.bocogop.shared.model.lookup.Party;
import org.bocogop.shared.persistence.AppSortedDAO;

public interface PartyDAO extends AppSortedDAO<Party> {

	public Party findByCode(String code);

	public Party findByLookup(LookupType lookup);

}