package org.bocogop.shared.persistence.impl.lookup.sds;

import java.util.SortedSet;
import java.util.TreeSet;

import javax.persistence.NoResultException;

import org.bocogop.shared.model.lookup.sds.State;
import org.bocogop.shared.persistence.impl.AbstractAppSortedDAOImpl;
import org.bocogop.shared.persistence.lookup.sds.StateDAO;
import org.bocogop.shared.util.cache.CacheNames;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;

@Repository
public class StateDAOImpl extends AbstractAppSortedDAOImpl<State> implements StateDAO {

	@SuppressWarnings("unchecked")
	@Override
	public SortedSet<State> findSortedStateByCountry(String countryId, String fipsCode) {
		try {
			return new TreeSet<>(
					query("from " + State.class.getName() + " where country_Id = :countryId and fipsCode < :fipsCode")
							.setParameter("countryId", countryId).setParameter("fipsCode", fipsCode).getResultList());
		} catch (NoResultException e) {
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public SortedSet<State> findSortedStateByCountry(String countryId) {
		try {
			return new TreeSet<>(query("from " + State.class.getName() + " where country_Id = :countryId")
					.setParameter("countryId", countryId).getResultList());
		} catch (NoResultException e) {
			return null;
		}
	}

	@Override
	public State findStateByPostalCode(String postalCode) {
		State matchedState = null;
		try {
			matchedState = (State) (query("from " + State.class.getName() + " where postalName = :postalName")
					.setParameter("postalName", postalCode).getSingleResult());
		} catch (NoResultException e) {
		}

		return matchedState;
	}

	@SuppressWarnings("unchecked")
	@Override
	@Cacheable(CacheNames.QUERIES_STATE_DAO)
	public SortedSet<State> findListOfStatesInUSA() {
		try {
			return new TreeSet<>(
					query("from " + State.class.getName() + " where country_Id = 1006840 and fipsCode < 60")
							.getResultList());
		} catch (NoResultException e) {
			return null;
		}
	}

	@Override
	@Cacheable(CacheNames.QUERIES_STATE_DAO)
	public SortedSet<State> findAllSorted() {
		return super.findAllSorted();
	}

	
	
}
