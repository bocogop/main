package org.bocogop.shared.persistence.impl.lookup.sds;

import javax.persistence.NoResultException;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;

import org.bocogop.shared.model.lookup.LookupType;
import org.bocogop.shared.model.lookup.sds.Gender;
import org.bocogop.shared.persistence.impl.AbstractAppSortedDAOImpl;
import org.bocogop.shared.persistence.lookup.sds.GenderDAO;
import org.bocogop.shared.util.cache.CacheNames;

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
