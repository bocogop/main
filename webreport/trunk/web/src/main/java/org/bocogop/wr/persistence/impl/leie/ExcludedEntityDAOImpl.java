package org.bocogop.wr.persistence.impl.leie;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.persistence.Query;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;

import org.bocogop.wr.model.facility.Facility;
import org.bocogop.wr.model.facility.Location;
import org.bocogop.wr.model.leie.ExcludedEntity;
import org.bocogop.wr.model.leie.ExclusionType;
import org.bocogop.wr.model.volunteer.Volunteer;
import org.bocogop.wr.model.volunteer.VolunteerAssignment;
import org.bocogop.wr.persistence.dao.leie.ExcludedEntityDAO;
import org.bocogop.wr.persistence.dao.leie.ExcludedEntityMatch;
import org.bocogop.wr.persistence.dao.leie.ExclusionTypeDAO;
import org.bocogop.wr.persistence.impl.GenericHibernateSortedDAOImpl;
import org.bocogop.wr.persistence.queryCustomization.QueryCustomization;
import org.bocogop.wr.util.cache.CacheNames;

@Repository
public class ExcludedEntityDAOImpl extends GenericHibernateSortedDAOImpl<ExcludedEntity> implements ExcludedEntityDAO {
	private static final Logger log = LoggerFactory.getLogger(ExcludedEntityDAOImpl.class);

	@Value("${excludedEntityURL}")
	private String url;

	@Autowired
	private ExclusionTypeDAO exclusionTypeDAO;

	@Cacheable(CacheNames.QUERIES_EXCLUDED_ENTITY_DAO_TOTAL_AND_FILTERED)
	@Override
	public int[] getTotalAndFilteredNumber(String searchValue) {
		Map<String, Object> params = new HashMap<>();
		String criteria = null;
		if (StringUtils.isNotBlank(searchValue)) {
			List<String> whereClauseItems = getWhereClauseItems(searchValue, params);
			criteria = "(" + StringUtils.join(whereClauseItems, ") and (") + ")";
		}
		return super.getTotalAndFilteredNumber(criteria, params);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<ExcludedEntity> findByCriteria(String searchValue, int start, int length, String orderBy) {
		StringBuilder sb = new StringBuilder("select o from ").append(ExcludedEntity.class.getName())
				.append(" o left join o.exclusionType et");

		Map<String, Object> params = new HashMap<>();
		List<String> whereClauseItems = getWhereClauseItems(searchValue, params);

		Query q = constructQuery(em, sb, whereClauseItems, params, null, new QueryCustomization().setOrderBy(orderBy));

		q.setFirstResult(start);
		q.setMaxResults(length);

		return q.getResultList();
	}

	private List<String> getWhereClauseItems(String searchValue, Map<String, Object> params) {
		List<String> whereClauseItems = new ArrayList<>();

		if (StringUtils.isNotBlank(searchValue)) {
			searchValue = searchValue.replaceAll("[^\\p{Print}]", "");
			String[] tokens = searchValue.split("\\s");
			for (int i = 0; i < tokens.length; i++) {
				if (StringUtils.isBlank(tokens[i]))
					continue;
				whereClauseItems.add("o.lastName like :search" + i + " or o.firstName like :search" + i
						+ " or o.middleName like :search" + i + " or o.businessName like :search" + i);
				params.put("search" + i, "%" + tokens[i] + "%");
			}
		}
		return whereClauseItems;
	}

	@Override
	public int importData(ImportDataCallback callback) throws IOException {
		HttpGet httpget = new HttpGet(url);

		try (CloseableHttpClient httpclient = HttpClients.createDefault();
				CloseableHttpResponse response = httpclient.execute(httpget)) {
			HttpEntity entity = response.getEntity();
			if (entity == null)
				throw new IOException("No file found at the URL " + url);

			try (BufferedReader isr = new BufferedReader(new InputStreamReader(entity.getContent()));
					CSVParser parser = new CSVParser(isr, CSVFormat.RFC4180.withHeader());) {
				int i = 0;
				for (Iterator<CSVRecord> it = parser.iterator(); it.hasNext(); i++) {
					if (i % 100 == 0)
						log.info("LEIE process adding record " + i);

					CSVRecord r = it.next();
					ExcludedEntity ee = new ExcludedEntity(r.toMap());
					ExclusionType exclusionType = exclusionTypeDAO.findBySSA(ee.getExclusionTypeCode());
					ee.setExclusionType(exclusionType);

					callback.processRecord(ee);
				}

				return i;
			}
		}
	}

	@Override
	public List<ExcludedEntityMatch> findExcludedEntitiesForFacilities(Collection<Long> vaFacilityIds) {
		return findExcludedEntities(vaFacilityIds, null, null);
	}

	@Override
	public List<ExcludedEntityMatch> findExcludedEntitiesForVolunteer(long volunteerId,
			LocalDate exclusionDateNewerThan) {
		return findExcludedEntities(null, volunteerId, exclusionDateNewerThan);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<ExcludedEntity> findExcludedEntitiesForVolunteerInfo(String lastName, String firstName,
			LocalDate dateOfBirth, LocalDate exclusionDateGreaterThan) {
		String q = "select ee from " + ExcludedEntity.class.getName()
				+ " ee where ee.lastName = :lastName and ee.firstName = :firstName and ee.dob = :dateOfBirth";
		if (exclusionDateGreaterThan != null)
			q += " and ee.exclusionDate > :exclusionDateGreaterThan";

		Query query = query(q);

		query.setParameter("lastName", lastName) //
				.setParameter("firstName", firstName) //
				.setParameter("dateOfBirth", dateOfBirth);
		if (exclusionDateGreaterThan != null)
			query.setParameter("exclusionDateGreaterThan", exclusionDateGreaterThan);

		return query.getResultList();
	}

	private List<ExcludedEntityMatch> findExcludedEntities(Collection<Long> vaFacilityIds, Long volunteerId,
			LocalDate exclusionDateNewerThan) {
		boolean specifiedFacilities = CollectionUtils.isNotEmpty(vaFacilityIds);

		Map<String, Object> params = new HashMap<>();
		List<String> whereClauseItems = new ArrayList<>();

		StringBuilder sb = new StringBuilder("select v, ee from ").append(ExcludedEntity.class.getName())
				.append(" ee, ").append(Volunteer.class.getName()).append(" v");

		whereClauseItems.add("ee.lastName = v.lastName");
		whereClauseItems.add("ee.firstName = v.firstName");
		whereClauseItems.add("ee.dob = v.dateOfBirth");

		if (specifiedFacilities) {
			whereClauseItems.add("(v.primaryFacility.id in (:facilityIds) or exists (" //
					+ "	select a from " + VolunteerAssignment.class.getName() + " a" //
					+ " join a.facility f" //
					+ " left join f.parent fp" //
					+ " where a.volunteer = v" //
					+ " and ((TYPE(f) = :facilityClass and f.id in (:facilityIds)) or (TYPE(f) = :locationClass and fp.id in (:facilityIds)))))");
			params.put("facilityIds", vaFacilityIds);
			params.put("facilityClass", Facility.class);
			params.put("locationClass", Location.class);
		}
		
		if (volunteerId != null) {
			whereClauseItems.add("v.id = :volunteerId");
			params.put("volunteerId", volunteerId);
		} else {
			whereClauseItems.add("v.status.volunteerActive = true");
		}

		if (exclusionDateNewerThan != null) {
			whereClauseItems.add("ee.exclusionDate > :exclusionDateNewerThan");
			params.put("exclusionDateNewerThan", exclusionDateNewerThan);
		}

		Query query = constructQuery(em, sb, whereClauseItems, params, null, new QueryCustomization().setOrderBy("ee.exclusionDate desc"));

		List<ExcludedEntityMatch> results = new ArrayList<>();

		@SuppressWarnings("unchecked")
		List<Object[]> rows = (List<Object[]>) query.getResultList();
		for (Object[] row : rows) {
			results.add(new ExcludedEntityMatch((Volunteer) row[0], (ExcludedEntity) row[1]));
		}
		return results;
	}

	@Override
	public List<ExcludedEntityMatch> findNewVolunteerMatches() {
		String q = "select v, ee from " + ExcludedEntity.class.getName() + " ee, " + Volunteer.class.getName() //
				+ " v where ee.lastName = v.lastName" //
				+ " and ee.firstName = v.firstName" //
				+ " and ee.dob = v.dateOfBirth" //
				+ " and (v.leieExclusionDate is null " //
				+ "		or v.leieApprovalOverride = false" //
				+ "		or ee.exclusionDate > v.leieExclusionDate)" //
				+ " and v.status.volunteerTerminated = false" //
				+ " order by v.id, ee.exclusionDate";

		Query query = query(q);

		Map<Volunteer, ExcludedEntity> resultMap = new LinkedHashMap<>();

		@SuppressWarnings("unchecked")
		List<Object[]> rows = (List<Object[]>) query.getResultList();
		for (Object[] row : rows) {
			resultMap.put((Volunteer) row[0], (ExcludedEntity) row[1]);
		}

		return resultMap.entrySet().stream().map(p -> new ExcludedEntityMatch(p.getKey(), p.getValue()))
				.collect(Collectors.toList());
	}

}
