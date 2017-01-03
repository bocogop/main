package org.bocogop.shared.persistence.impl.lookup;

import javax.persistence.NoResultException;

import org.bocogop.shared.model.lookup.Country;
import org.bocogop.shared.model.lookup.LookupType;
import org.bocogop.shared.persistence.dao.CountryDAO;
import org.bocogop.shared.persistence.impl.AbstractAppSortedDAOImpl;
import org.bocogop.shared.util.cache.CacheNames;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;

@Repository
public class CountryDAOImpl extends AbstractAppSortedDAOImpl<Country> implements CountryDAO {

	@Cacheable(CacheNames.QUERIES_COUNTRY_DAO)
	@Override
	public Country findByCode(String code) {
		try {
			return (Country) query("from " + Country.class.getName() + " where code = :code").setParameter("code", code)
					.getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}

	@Cacheable(CacheNames.QUERIES_COUNTRY_DAO)
	@Override
	public Country findByLookup(LookupType lookup) {
		return findRequiredByPrimaryKey(lookup.getId());
	}
}
