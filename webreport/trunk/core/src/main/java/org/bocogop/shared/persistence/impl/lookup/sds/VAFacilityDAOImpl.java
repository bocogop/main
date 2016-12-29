package org.bocogop.shared.persistence.impl.lookup.sds;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;

import org.bocogop.shared.model.lookup.sds.VAFacility;
import org.bocogop.shared.persistence.impl.AbstractAppSortedDAOImpl;
import org.bocogop.shared.persistence.lookup.sds.VAFacilityDAO;
import org.bocogop.shared.util.StationsUtil;
import org.bocogop.shared.util.cache.CacheNames;

/* We use Spring method-level caching a lot in this class, which could just as well have been
 * implemented using Hibernate-level cache hints. But it's an example of how to do it, plus 
 * it saves a small amount of time having to construct TreeSets around some query results. CPB */
@Repository
public class VAFacilityDAOImpl extends AbstractAppSortedDAOImpl<VAFacility> implements VAFacilityDAO {

	protected boolean dataChangesAllowedOutsideUnitTest() {
		return false;
	}

	@Override
	@Cacheable(value = CacheNames.QUERIES_VA_FACILITY_DAO)
	public VAFacility findByStationNumber(String stationNumber) {
		try {
			return (VAFacility) query("from " + VAFacility.class.getName() + " where stationNumber = :stationNumber")
					.setParameter("stationNumber", stationNumber).getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}

	private String getActiveCriterion() {
		return "(i.deactivationDate is null or coalesce(i.activationDate, '1900-01-01') > deactivationDate)";
	}

	@Override
	@SuppressWarnings("unchecked")
	@Cacheable(value = CacheNames.QUERIES_VA_FACILITY_DAO)
	public SortedSet<VAFacility> findAllSorted() {
		return new TreeSet<>(
				query("from " + VAFacility.class.getName() + " i where " + getActiveCriterion()).getResultList());
	}

	@Override
	@SuppressWarnings("unchecked")
	@Cacheable(value = CacheNames.QUERIES_VA_FACILITY_DAO)
	public SortedSet<VAFacility> findAllStationsInVisn(long visnId) {
		Query query = query(
				"from " + VAFacility.class.getName() + " i where " + getActiveCriterion() + " and i.visn.id = :visnId");
		query.setParameter("visnId", visnId);
		return new TreeSet<>(query.getResultList());
	}

	@Override
	@SuppressWarnings("unchecked")
	@Cacheable(value = CacheNames.QUERIES_VA_FACILITY_DAO)
	public SortedSet<VAFacility> findAllChildrenSorted(long parentId) {
		Query query = query(
				"from " + VAFacility.class.getName() + " i where " + getActiveCriterion() + " and i.parent.id = :pid");
		query.setParameter("pid", parentId);
		return new TreeSet<>(query.getResultList());
	}

	@Override
	@Cacheable(value = CacheNames.QUERIES_VA_FACILITY_DAO)
	public SortedSet<VAFacility> findAllThreeDigitStationsSorted() {
		SortedSet<VAFacility> allSorted = new TreeSet<>(findAllSorted());
		for (Iterator<VAFacility> it = allSorted.iterator(); it.hasNext();) {
			String stationNumber = it.next().getStationNumber();
			if (StringUtils.isBlank(stationNumber) || !StationsUtil.isThreeDigitStation(stationNumber))
				it.remove();
		}
		return allSorted;
	}

	@Override
	@SuppressWarnings("unchecked")
	@Cacheable(value = CacheNames.QUERIES_VA_FACILITY_DAO)
	public SortedSet<VAFacility> findAllVISNsSorted() {
		return new TreeSet<>(query("from " + VAFacility.class.getName() + " i where " + getActiveCriterion()
				+ " and i.facilityType.code =  'VISN' and i.stationNumber is null").getResultList());
	}

	@Override
	@SuppressWarnings("unchecked")
	@Cacheable(value = CacheNames.QUERIES_VA_FACILITY_DAO)
	public SortedSet<VAFacility> findAllActiveTreatingFacilities() {
		return new TreeSet<>(query("from " + VAFacility.class.getName() + " i where " + getActiveCriterion()
				+ " and i.facilityType.code in ('CBOC', 'Dent', 'Dom', 'M&ROC', 'NHC', 'OC', 'OPC', 'ORC', 'VAMC', 'RO-OC')"
				+ " and i.facilityType.medicalTreating = true").getResultList());
	}

	@Override
	public List<QuickSearchResult> findByCriteria(String text, int maxResults) {
		if (StringUtils.isBlank(text))
			return new ArrayList<>();

		String[] tokens = text.split("\\W");

		StringBuilder sb = new StringBuilder();
		sb.append("select t.id, t.name, t.stationNumber from ");
		sb.append(VAFacility.class.getName());
		sb.append(" t where 1=1");

		Map<String, String> params = new HashMap<>();

		for (int i = 0; i < tokens.length; i++) {
			String token = tokens[i];
			if (StringUtils.isBlank(token))
				continue;

			sb.append(" and (lower(t.name) like :text").append(i);
			sb.append(" or lower(t.stationNumber) like :text").append(i).append(")");
			params.put("text" + i, "%" + token.toLowerCase() + "%");
		}
		Query q = query(sb.toString());
		for (Entry<String, String> entry : params.entrySet())
			q.setParameter(entry.getKey(), entry.getValue());

		if (maxResults > 0)
			q.setMaxResults(maxResults);

		@SuppressWarnings("unchecked")
		List<Object[]> results = q.getResultList();

		List<QuickSearchResult> returnResults = new ArrayList<>(results.size());
		for (Object[] result : results) {
			returnResults.add(new QuickSearchResult(((Number) result[0]).longValue(),
					VAFacility.getDisplayName((String) result[1], (String) result[2])));
		}
		return returnResults;
	}

}
