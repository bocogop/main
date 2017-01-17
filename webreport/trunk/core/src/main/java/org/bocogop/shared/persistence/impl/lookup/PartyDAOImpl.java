package org.bocogop.shared.persistence.impl.lookup;

import javax.persistence.NoResultException;

import org.bocogop.shared.model.lookup.Gender;
import org.bocogop.shared.model.lookup.LookupType;
import org.bocogop.shared.model.lookup.Party;
import org.bocogop.shared.persistence.dao.PartyDAO;
import org.bocogop.shared.persistence.impl.AbstractAppSortedDAOImpl;
import org.bocogop.shared.util.cache.CacheNames;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;

@Repository
public class PartyDAOImpl extends AbstractAppSortedDAOImpl<Party> implements PartyDAO {

	@Cacheable(CacheNames.QUERIES_PARTY_DAO)
	@Override
	public Party findByCode(String code) {
		try {
			return (Party) query("from " + Party.class.getName() + " where code = :code").setParameter("code", code)
					.getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}

	@Cacheable(CacheNames.QUERIES_PARTY_DAO)
	@Override
	public Party findByLookup(LookupType lookup) {
		return findRequiredByPrimaryKey(lookup.getId());
	}
}
