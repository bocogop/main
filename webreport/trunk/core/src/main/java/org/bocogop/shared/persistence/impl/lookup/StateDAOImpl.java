package org.bocogop.shared.persistence.impl.lookup;

import java.util.List;

import javax.persistence.NoResultException;

import org.bocogop.shared.model.lookup.LookupType;
import org.bocogop.shared.model.lookup.State;
import org.bocogop.shared.persistence.dao.StateDAO;
import org.bocogop.shared.persistence.impl.AbstractAppSortedDAOImpl;
import org.bocogop.shared.util.cache.CacheNames;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;

@Repository
public class StateDAOImpl extends AbstractAppSortedDAOImpl<State> implements StateDAO {

	@Cacheable(CacheNames.QUERIES_STATE_DAO)
	@Override
	public State findByCode(String code) {
		try {
			return (State) query("from " + State.class.getName() + " where code = :code").setParameter("code", code)
					.getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}

	@Cacheable(CacheNames.QUERIES_STATE_DAO)
	@Override
	public State findByLookup(LookupType lookup) {
		return findRequiredByPrimaryKey(lookup.getId());
	}

	@SuppressWarnings("unchecked")
	@Cacheable(CacheNames.QUERIES_STATE_DAO)
	@Override
	public List<State> findAllSortedByCountry() {
		return (List<State>) query("from " + State.class.getName() + " s order by s.country.id, s.name")
				.getResultList();
	}
}
