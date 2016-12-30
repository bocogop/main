package org.bocogop.wr.persistence.impl.lookup;

import javax.persistence.NoResultException;

import org.bocogop.wr.model.lookup.Gender;
import org.bocogop.wr.model.lookup.LookupType;
import org.bocogop.wr.persistence.impl.AbstractAppSortedDAOImpl;
import org.bocogop.wr.persistence.lookup.GenderDAO;
import org.bocogop.wr.util.cache.CacheNames;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;

@Repository
public class GenderDAOImpl extends AbstractAppSortedDAOImpl<Gender> implements GenderDAO {

	@Cacheable(CacheNames.QUERIES_GENDER_DAO)
	@Override
	public Gender findByCode(String code) {
		try {
			return (Gender) query("from " + Gender.class.getName() + " where code = :code").setParameter("code", code)
					.getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}

	@Cacheable(CacheNames.QUERIES_GENDER_DAO)
	@Override
	public Gender findByLookup(LookupType lookup) {
		return findRequiredByPrimaryKey(lookup.getId());
	}
}
