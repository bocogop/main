package org.bocogop.wr.persistence.impl.facility;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.persistence.Query;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;

import org.bocogop.shared.model.core.AbstractAuditedPersistent;
import org.bocogop.shared.model.lookup.sds.VAFacility;
import org.bocogop.shared.persistence.lookup.sds.VAFacilityDAO;
import org.bocogop.shared.persistence.lookup.sds.VAFacilityDAO.QuickSearchResult;
import org.bocogop.shared.util.PersistenceUtil;
import org.bocogop.wr.model.facility.AbstractLocation;
import org.bocogop.wr.model.facility.AdministrativeUnit;
import org.bocogop.wr.model.facility.Facility;
import org.bocogop.wr.persistence.dao.facility.FacilityDAO;
import org.bocogop.wr.persistence.dao.lookup.AdministrativeUnitDAO;
import org.bocogop.wr.persistence.impl.GenericHibernateSortedDAOImpl;
import org.bocogop.wr.persistence.queryCustomization.QueryCustomization;
import org.bocogop.wr.util.cache.CacheNames;

@Repository
public class FacilityDAOImpl extends GenericHibernateSortedDAOImpl<Facility> implements FacilityDAO {

	@Autowired
	private VAFacilityDAO vaFacilityDAO;
	@Autowired
	private AdministrativeUnitDAO administrativeUnitDAO;

	// @Cacheable(value = CacheNames.QUERIES_FACILITY_DAO)
	public Facility findByStationNumber(String stationNumber) {
		@SuppressWarnings("unchecked")
		List<Facility> results = query("from " + Facility.class.getName() + " where stationNumber = :stationNumber")
				.setParameter("stationNumber", stationNumber).getResultList();
		return results.isEmpty() ? null : results.get(0);
	}

	@Override
	public Map<Long, Facility> findByVAFacilityIds(Collection<Long> vaFacilityIds) {
		Map<Long, Facility> results = new HashMap<>();

		if (CollectionUtils.isEmpty(vaFacilityIds))
			return results;

		List<Long> vaFacilityIdList = new ArrayList<>(vaFacilityIds);

		for (int i = 0; i < vaFacilityIdList.size(); i += 2000) {
			List<Long> batchChunk = vaFacilityIdList.subList(i, Math.min(vaFacilityIdList.size(), i + 2000));

			@SuppressWarnings("unchecked")
			List<Facility> queryResults = query("select i from " + Facility.class.getName()
					+ " i left join fetch i.vaFacility f where f.id in (:vaFacilityIds)")
							.setParameter("vaFacilityIds", batchChunk).getResultList();
			for (Facility r : queryResults)
				results.put(r.getVaFacility().getId(), r);
		}
		return results;
	}

	@Override
	public Map<Long, Facility> findByVAFacilities(Collection<VAFacility> vaFacilities) {
		return findByVAFacilityIds(PersistenceUtil.translateObjectsToIds(vaFacilities));
	}

	@Override
	// @Cacheable(value = CacheNames.QUERIES_FACILITY_DAO)
	public SortedSet<VAFacility> findVAFacilitiesWithLinkToFacility() {
		@SuppressWarnings("unchecked")
		List<VAFacility> results = query(
				"select i.vaFacility from " + Facility.class.getName() + " i where i.vaFacility is not null")
						.getResultList();
		return new TreeSet<>(results);
	}

	@Override
	// @Cacheable(value = CacheNames.QUERIES_FACILITY_DAO)
	public SortedSet<Facility> findWithLinkToVAFacility() {
		@SuppressWarnings("unchecked")
		List<Facility> results = query("select i from " + Facility.class.getName() + " i where vaFacility is not null")
				.getResultList();
		return new TreeSet<>(results);
	}

	@Override
	public Facility findByVAFacility(long vaFacilityId) {
		return findByVAFacilityIds(Arrays.asList(vaFacilityId)).get(vaFacilityId);
	}

	@Override
	@CacheEvict(value = CacheNames.QUERIES_FACILITY_DAO,
			// inefficient but we shouldn't need to save these very often - CPB
			allEntries = true)
	public Facility saveOrUpdate(Facility item) {
		return super.saveOrUpdate(item);
	}

	@Override
	public SortedSet<Facility> findRootFacilities() {
		@SuppressWarnings("unchecked")
		List<Facility> results = query(
				"select i from " + Facility.class.getName() + " i where parent is null or parent = i").getResultList();
		return new TreeSet<>(results);
	}

	@Override
	public void updateFieldsWithoutVersionIncrement(long facilityId, boolean setVAFacilityId, Long vaFacilityId,
			boolean setAdministrativeUnitId, Long administrativeUnitId, boolean setStationNumber,
			String stationNumber) {
		if (!setVAFacilityId)
			throw new IllegalArgumentException("No update parameter was specified");

		/*
		 * Necessary in case we made changes prior to this that haven't been
		 * flushed yet - CPB
		 */
		em.flush();

		List<String> updates = new ArrayList<>();
		Map<String, Object> params = new HashMap<>();

		if (setVAFacilityId) {
			VAFacility f = vaFacilityId != null ? vaFacilityDAO.findRequiredByPrimaryKey(vaFacilityId) : null;
			updates.add("vaFacility = :vaFacility");
			params.put("vaFacility", f);
		}

		if (setAdministrativeUnitId) {
			AdministrativeUnit ap = administrativeUnitDAO.findRequiredByPrimaryKey(administrativeUnitId);
			updates.add("administrativeUnit = :administrativeUnit");
			params.put("administrativeUnit", ap);
		}

		if (setStationNumber) {
			updates.add("stationNumber = :stationNumber");
			params.put("stationNumber", stationNumber);
		}
		
		updates.add("modifiedBy = :myUser");
		params.put("myUser", AbstractAuditedPersistent.getCurrentUserIdForAudit());
		updates.add("modifiedDate = :nowUTC");
		params.put("nowUTC", ZonedDateTime.now(ZoneId.of("Z")));

		StringBuilder sb = new StringBuilder("update " + Facility.class.getName() + " set ");
		sb.append(StringUtils.join(updates, ", "));
		sb.append(" where id = :facilityId");
		params.put("facilityId", facilityId);

		Query q = em.createQuery(sb.toString());
		for (Entry<String, Object> paramEntry : params.entrySet())
			q.setParameter(paramEntry.getKey(), paramEntry.getValue());
		int numUpdated = q.executeUpdate();

		if (numUpdated == 0)
			throw new IllegalStateException("No facility with ID " + facilityId + " found.");
	}

	@Override
	public <T extends AbstractLocation> SortedSet<T> findByCriteria(Long facilityOrLocationId, Long parentFacilityId,
			Class<T> requiredClassType, Boolean activeStatus) {
		if (parentFacilityId == null && facilityOrLocationId == null)
			throw new IllegalArgumentException("No criteria specified");

		StringBuilder sb = new StringBuilder("select v from ").append(requiredClassType.getName()).append(" v");

		/* Don't bother with this yet - CPB */
		QueryCustomization cust = new QueryCustomization();

		List<String> whereClauseItems = new ArrayList<>();
		Map<String, Object> params = new HashMap<>();

		if (facilityOrLocationId != null) {
			whereClauseItems.add("v.id = :facilityOrLocationId");
			params.put("facilityOrLocationId", facilityOrLocationId);
		}

		if (parentFacilityId != null) {
			whereClauseItems.add("v.parent.id = :parentId");
			params.put("parentId", parentFacilityId);
		}

		if (activeStatus != null) {
			whereClauseItems.add("v.inactive = :inactive");
			params.put("inactive", !activeStatus);
		}

		Query q = constructQuery(em, sb, whereClauseItems, params, null, cust);

		int maxResults = -1;
		if (cust.getRowLimitation() != null)
			maxResults = cust.getRowLimitation();

		if (maxResults > 0)
			q.setMaxResults(maxResults);

		@SuppressWarnings("unchecked")
		List<T> resultList = q.getResultList();
		return new TreeSet<>(resultList);
	}

	@Override
	public List<QuickSearchResult> findUnlinkedMatchingVAFacilities(String text, int maxResults) {
		if (StringUtils.isBlank(text))
			return new ArrayList<>();

		String[] tokens = text.split("\\W");

		StringBuilder sb = new StringBuilder();
		sb.append("select t.id, t.name, t.stationNumber from ");
		sb.append(VAFacility.class.getName()).append(" t");
		sb.append(" where t not in (select f.vaFacility from ").append(Facility.class.getName()).append(" f)");

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

	@Override
	@Cacheable(value = CacheNames.QUERIES_FACILITY_DAO)
	public SortedSet<Facility> findAllSorted() {
		SortedSet<Facility> all = super.findAllSorted();
		for (Facility f : all)
			f.initializeFacility();
		return all;
	}

}
