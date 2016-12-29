package org.bocogop.wr.persistence.impl.facility;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.persistence.Query;

import org.springframework.stereotype.Repository;

import com.google.common.collect.Lists;

import org.bocogop.wr.model.facility.Location;
import org.bocogop.wr.model.volunteer.VolunteerAssignment;
import org.bocogop.wr.persistence.dao.facility.LocationDAO;
import org.bocogop.wr.persistence.impl.GenericHibernateSortedDAOImpl;
import org.bocogop.wr.persistence.queryCustomization.QueryCustomization;

@Repository
public class LocationDAOImpl extends GenericHibernateSortedDAOImpl<Location> implements LocationDAO {

	@Override
	public SortedSet<Location> findByCriteria(Long parentFacilityId) {
		if (parentFacilityId == null)
			throw new IllegalArgumentException("No criteria specified");

		StringBuilder sb = new StringBuilder("select v from ").append(Location.class.getName()).append(" v");

		/* Don't bother with this yet - CPB */
		QueryCustomization cust = new QueryCustomization();

		List<String> whereClauseItems = new ArrayList<>();
		Map<String, Object> params = new HashMap<>();

		if (parentFacilityId != null) {
			whereClauseItems.add("v.parent.id = :parentId");
			params.put("parentId", parentFacilityId);
		}

		Query q = constructQuery(em, sb, whereClauseItems, params, null, cust);

		int maxResults = -1;
		if (cust.getRowLimitation() != null)
			maxResults = cust.getRowLimitation();

		if (maxResults > 0)
			q.setMaxResults(maxResults);

		@SuppressWarnings("unchecked")
		List<Location> resultList = q.getResultList();
		return new TreeSet<>(resultList);
	}

	@Override
	public Map<Long, Integer[]> countVolunteersForLocations(Collection<Long> locationIdColl) {
		Map<Long, Integer[]> results = new HashMap<>();

		List<Long> locationIds = new ArrayList<>(locationIdColl);
		for (Long a : locationIds)
			results.put(a, new Integer[] { 0, 0 });

		String q = "select f.id, count(distinct vfa.volunteer)" //
				+ " from " + VolunteerAssignment.class.getName() + " vfa" //
				+ " join vfa.facility f" //
				+ " where f.id in (:ids)" //
				+ " and TYPE(f) = :locationType" //
				+ " group by f.id";

		for (List<Long> batchChunk : Lists.partition(new ArrayList<>(locationIds), 2000)) {
			@SuppressWarnings("unchecked")
			List<Object[]> queryResults = query(q).setParameter("locationType", Location.class)
					.setParameter("ids", batchChunk).getResultList();
			for (Object[] r : queryResults) {
				results.get(((Number) r[0]).longValue())[1] = ((Number) r[1]).intValue();
			}
		}

		q = "select f.id, count(distinct vfa.volunteer)" //
				+ " from " + VolunteerAssignment.class.getName() + " vfa" //
				+ " join vfa.facility f" //
				+ " join vfa.volunteer v" //
				+ " where f.id in (:ids)" //
				+ " and TYPE(f) = :locationType" //
				+ " and vfa.inactive = false" //
				+ " and v.status.volunteerActive = true" //
				+ " group by f.id";

		for (List<Long> batchChunk : Lists.partition(new ArrayList<>(locationIds), 2000)) {
			@SuppressWarnings("unchecked")
			List<Object[]> queryResults = query(q).setParameter("locationType", Location.class)
					.setParameter("ids", batchChunk).getResultList();
			for (Object[] r : queryResults) {
				results.get(((Number) r[0]).longValue())[0] = ((Number) r[1]).intValue();
			}
		}

		return results;
	}

}
