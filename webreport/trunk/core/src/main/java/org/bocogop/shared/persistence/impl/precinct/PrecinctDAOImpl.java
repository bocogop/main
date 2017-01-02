package org.bocogop.shared.persistence.impl.precinct;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.persistence.Query;

import org.bocogop.shared.model.precinct.Precinct;
import org.bocogop.shared.persistence.dao.precinct.PrecinctDAO;
import org.bocogop.shared.persistence.impl.GenericHibernateSortedDAOImpl;
import org.bocogop.shared.persistence.queryCustomization.QueryCustomization;
import org.bocogop.shared.util.cache.CacheNames;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;

@Repository
public class PrecinctDAOImpl extends GenericHibernateSortedDAOImpl<Precinct> implements PrecinctDAO {

	@Override
	@CacheEvict(value = CacheNames.QUERIES_PRECINCT_DAO,
			// inefficient but we shouldn't need to save these very often - CPB
			allEntries = true)
	public Precinct saveOrUpdate(Precinct item) {
		return super.saveOrUpdate(item);
	}

	@Override
	public SortedSet<Precinct> findByCriteria(String code, String name) {
		StringBuilder sb = new StringBuilder("select v from ").append(Precinct.class.getName()).append(" v");

		/* Don't bother with this yet - CPB */
		QueryCustomization cust = new QueryCustomization();

		List<String> whereClauseItems = new ArrayList<>();
		Map<String, Object> params = new HashMap<>();

		if (code != null) {
			whereClauseItems.add("v.code = :code");
			params.put("code", code);
		}

		if (name != null) {
			whereClauseItems.add("v.name = :name");
			params.put("name", name);
		}

		Query q = constructQuery(em, sb, whereClauseItems, params, null, cust);

		int maxResults = -1;
		if (cust.getRowLimitation() != null)
			maxResults = cust.getRowLimitation();

		if (maxResults > 0)
			q.setMaxResults(maxResults);

		@SuppressWarnings("unchecked")
		List<Precinct> resultList = q.getResultList();
		return new TreeSet<>(resultList);
	}

	@Override
	@Cacheable(value = CacheNames.QUERIES_PRECINCT_DAO)
	public SortedSet<Precinct> findAllSorted() {
		SortedSet<Precinct> all = super.findAllSorted();
		return all;
	}

}
