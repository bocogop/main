package org.bocogop.wr.persistence.impl.volunteer;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import org.bocogop.shared.model.core.AbstractAuditedPersistent;
import org.bocogop.wr.model.benefitingService.BenefitingService;
import org.bocogop.wr.model.facility.AbstractUpdateableLocation;
import org.bocogop.wr.model.facility.Facility;
import org.bocogop.wr.model.facility.Location;
import org.bocogop.wr.model.time.AdjustedHoursEntry;
import org.bocogop.wr.model.time.WorkEntry;
import org.bocogop.wr.model.volunteer.Volunteer;
import org.bocogop.wr.model.volunteer.VolunteerAssignment;
import org.bocogop.wr.model.volunteer.VolunteerStatus.VolunteerStatusType;
import org.bocogop.wr.persistence.dao.TimeSummary;
import org.bocogop.wr.persistence.dao.facility.UpdateableLocationDAO;
import org.bocogop.wr.persistence.dao.lookup.VolunteerStatusDAO;
import org.bocogop.wr.persistence.dao.organization.OrgQuickSearchResult;
import org.bocogop.wr.persistence.dao.organization.OrganizationDAO;
import org.bocogop.wr.persistence.dao.volunteer.VolunteerDAO;
import org.bocogop.wr.persistence.dao.volunteer.VolunteerQuickSearchAssignment;
import org.bocogop.wr.persistence.dao.volunteer.VolunteerQuickSearchOrganization;
import org.bocogop.wr.persistence.dao.volunteer.VolunteerQuickSearchResult;
import org.bocogop.wr.persistence.impl.GenericHibernateSortedDAOImpl;
import org.bocogop.wr.persistence.queryCustomization.QueryCustomization;

@Repository
public class VolunteerDAOImpl extends GenericHibernateSortedDAOImpl<Volunteer> implements VolunteerDAO {
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(VolunteerDAOImpl.class);

	@Value("${volunteerSearch.maxResults}")
	private int maxResults;
	@Autowired
	private OrganizationDAO organizationDAO;
	@Autowired
	private VolunteerStatusDAO volunteerStatusDAO;
	@Autowired
	private UpdateableLocationDAO locationDAO;

	@SuppressWarnings("unchecked")
	@Override
	public List<Volunteer> findByCriteria(String firstName, String middleName, String lastName,
			boolean firstNameOrLastNameMatches, boolean useExactNameMatching, String identifyingCode,
			LocalDate dateOfBirth, String addressStreet, String city, String state, String zip, String phone,
			String email, VolunteerStatusType status, Collection<Long> facilityIds,
			QueryCustomization... customization) {
		boolean hasFacilityIds = CollectionUtils.isNotEmpty(facilityIds);

		StringBuilder sb = new StringBuilder("select v from ").append(Volunteer.class.getName()).append(" v");

		if (hasFacilityIds) {
			sb.append(" left join v.volunteerAssignments fa left join fa.facility faf");
			sb.append(" left join v.primaryFacility pf left join faf.parent faf_parent");
		}

		/* Don't bother with this yet - CPB */
		QueryCustomization cust = new QueryCustomization();

		List<String> whereClauseItems = new ArrayList<>();
		Map<String, Object> params = new HashMap<>();

		boolean hasFirst = StringUtils.isNotBlank(firstName);
		boolean hasLast = StringUtils.isNotBlank(lastName);
		String comparator = useExactNameMatching ? "=" : "like";
		String wildcard = useExactNameMatching ? "" : "%";

		if (firstNameOrLastNameMatches && hasFirst && hasLast) {
			whereClauseItems
					.add("(v.firstName " + comparator + " :firstName or v.lastName " + comparator + " :lastName)");
			params.put("firstName", wildcard + firstName + wildcard);
			params.put("lastName", wildcard + lastName + wildcard);
		} else {
			if (hasFirst) {
				whereClauseItems.add("v.firstName " + comparator + " :firstName");
				params.put("firstName", wildcard + firstName + wildcard);
			}

			if (hasLast) {
				whereClauseItems.add("v.lastName " + comparator + " :lastName");
				params.put("lastName", wildcard + lastName + wildcard);
			}
		}

		if (StringUtils.isNotBlank(identifyingCode)) {
			whereClauseItems.add("v.identifyingCode = :identifyingCode");
			params.put("identifyingCode", identifyingCode);
		}

		if (dateOfBirth != null) {
			whereClauseItems.add("v.dateOfBirth = :dob");
			params.put("dob", dateOfBirth);
		}

		if (hasFacilityIds) {
			whereClauseItems.add("(   (pf.id is not null and pf.id in (:facilityIds))" //
					+ " or (faf is not null and TYPE(faf) = :facilityType and faf.id in (:facilityIds))" //
					+ " or (faf is not null and TYPE(faf) = :locationType and faf_parent.id in (:facilityIds)) )");
			params.put("facilityIds", facilityIds);
			params.put("facilityType", Facility.class);
			params.put("locationType", Location.class);
		}

		if (StringUtils.isNotBlank(addressStreet)) {
			whereClauseItems.add("v.addressLine1 like :addressStreet" + " or v.addressLine2 like :addressStreet");
			params.put("addressStreet", "%" + addressStreet + "%");
		}

		if (StringUtils.isNotBlank(city)) {
			whereClauseItems.add("v.city like :city");
			params.put("city", "%" + city + "%");
		}

		if (status != null) {
			whereClauseItems.add("v.status.id = :statusId");
			params.put("statusId", status.getId());
		}

		/*
		 * Match on State abbreviation if specified value has only two letters;
		 * otherwise, wildcard match on state name
		 */
		if (StringUtils.isNotBlank(state)) {
			state = state.trim();
			if (state.trim().length() == 2) {
				whereClauseItems.add("v.state.abbreviation = :state");
				params.put("state", state);
			} else {
				whereClauseItems.add("v.state.name like :state");
				params.put("state", "%" + state + "%");
			}
		}

		/* Match only on the first five digits */
		if (StringUtils.isNotBlank(zip)) {
			whereClauseItems.add("SUBSTRING(v.zip, 1, 5) = :zip");
			if (zip.length() > 5)
				zip = zip.substring(0, 5);
			params.put("zip", zip);
		}

		if (StringUtils.isNotBlank(phone)) {
			StringBuilder sb2 = new StringBuilder();
			for (String phoneField : new String[] { "phone", "phoneAlt", "phoneAlt2" })
				sb2.append(sb2.length() > 0 ? " OR " : "") //
						.append("CONCAT(SUBSTRING(v.").append(phoneField).append(",1,3), ") //
						.append("SUBSTRING(v.").append(phoneField).append(", 5,3), ") //
						.append("SUBSTRING(v.").append(phoneField).append(", 9,4)) like :phone");
			whereClauseItems.add(sb2.toString());
			params.put("phone", "%" + phone.replaceAll("\\D", "") + "%");
		}

		if (StringUtils.isNotBlank(email)) {
			whereClauseItems.add("v.email like :email");
			params.put("email", "%" + email + "%");
		}

		Query q = constructQuery(em, sb, whereClauseItems, params, null, cust);

		int maxResults = this.maxResults;
		if (cust.getRowLimitation() != null)
			maxResults = cust.getRowLimitation();

		List<Volunteer> resultList = q.setMaxResults(maxResults).getResultList();
		return resultList;
	}

	@Override
	public List<String> findZipCodesAtFacilities(List<Long> facilityIds, Boolean volunteerActive,
			Boolean volunteerAssignmentActive) {
		String query = "select distinct SUBSTRING(v.zip, 1, 5) from " + Volunteer.class.getName()
				+ " v join v.volunteerAssignments vfa" + " where vfa.facility.id in (:facilityIds)";
		if (volunteerActive != null)
			query += " and v.status.volunteerActive = " + volunteerActive;
		if (volunteerAssignmentActive != null)
			query += " and vfa.inactive = " + !volunteerAssignmentActive;
		query += " order by SUBSTRING(v.zip, 1, 5)";

		@SuppressWarnings("unchecked")
		List<String> results = query(query).setParameter("facilityIds", facilityIds).getResultList();
		return results;
	}

	@SuppressWarnings("unchecked")
	@Override
	public SortedSet<Facility> findFacilitiesForVolunteer(long volunteerId) {
		return new TreeSet<>(query(
				"select vfa.facility from " + VolunteerAssignment.class.getName() + " vfa where vfa.volunteer.id = :id")
						.setParameter("id", volunteerId).getResultList());
	}

	@Override
	public void updateFieldsWithoutVersionIncrement(long volunteerId, boolean setPrimaryOrganization,
			Long primaryOrganizationId, boolean setPrimaryFacility, Long primaryFacilityId, VolunteerStatusType status,
			LocalDate statusDate, Long preferredLanguageId) {
		if (!setPrimaryOrganization && !setPrimaryFacility && status == null && statusDate == null
				&& preferredLanguageId == null)
			throw new IllegalArgumentException("No update parameter was specified");

		/*
		 * Necessary in case we made changes prior to this that haven't been
		 * flushed yet - CPB
		 */
		em.flush();

		List<String> updates = new ArrayList<>();
		Map<String, Object> params = new HashMap<>();

		if (setPrimaryOrganization) {
			updates.add("PrimaryOrganizationFK = :primaryOrganizationId");
			params.put("primaryOrganizationId", primaryOrganizationId);
		}

		if (setPrimaryFacility) {
			if (primaryFacilityId != null) {
				AbstractUpdateableLocation<?> l = locationDAO.findRequiredByPrimaryKey(primaryFacilityId);
				if ("Location".equals(l.getScale()))
					throw new IllegalArgumentException("Cannot set the primary facility to a location");
			}

			updates.add("PrimaryFacilityFK = :primaryFacilityId");
			params.put("primaryFacilityId", primaryFacilityId);
		}

		if (status != null) {
			updates.add("WR_STD_VolunteerStatusFK = :newStatusId");
			params.put("newStatusId", status.getId());
		}

		if (statusDate != null) {
			updates.add("statusDate = :statusDate");
			params.put("statusDate", statusDate);
		}

		if (preferredLanguageId != null) {
			updates.add("PreferredLanguageForLoginFK = :langId");
			params.put("langId", preferredLanguageId);
		}

		updates.add("MODIFIED_BY = :myUser");
		params.put("myUser", AbstractAuditedPersistent.getCurrentUserIdForAudit());
		updates.add("MODIFIED_DATE = :nowUTC");
		params.put("nowUTC", ZonedDateTime.now(ZoneId.of("Z")));

		StringBuilder sb = new StringBuilder("update wr.Volunteers set ");
		sb.append(StringUtils.join(updates, ", "));
		sb.append(" where id = :volunteerId");
		params.put("volunteerId", volunteerId);

		Query q = em.createNativeQuery(sb.toString());
		for (Entry<String, Object> paramEntry : params.entrySet())
			q.setParameter(paramEntry.getKey(), paramEntry.getValue());
		int numUpdated = q.executeUpdate();

		if (numUpdated == 0)
			throw new IllegalStateException("No volunteer ID " + volunteerId + " found.");
	}

	@Override
	public TimeSummary getTimeSummary(long volunteerId, ZoneId facilityTimeZone) {
		LocalDate currentFYStart = dateUtil.getCurrentFiscalYearStartDate(facilityTimeZone);
		Object[] result = (Object[]) query("select max(we.dateWorked), " //
				+ "COALESCE(sum(case when we.dateWorked < :currentFYStart then we.hoursWorked else 0 end), 0), " //
				+ "COALESCE(sum(case when we.dateWorked >= :currentFYStart then we.hoursWorked else 0 end), 0) " //
				+ " from " + WorkEntry.class.getName() //
				+ " we where we.volunteerAssignment.volunteer.id = :volunteerId")
						.setParameter("currentFYStart", currentFYStart).setParameter("volunteerId", volunteerId)
						.getSingleResult();
		LocalDate mostRecentDateWorked = (LocalDate) result[0];
		double priorYearHours = ((Number) result[1]).doubleValue();
		double currentYearHours = ((Number) result[2]).doubleValue();

		Object result2 = query("select COALESCE(sum(ta.hours), 0) from " + AdjustedHoursEntry.class.getName()
				+ " ta where ta.volunteer.id = :volunteerId").setParameter("volunteerId", volunteerId)
						.getSingleResult();
		double adjustedHours = ((Number) result2).doubleValue();

		TimeSummary ts = new TimeSummary(mostRecentDateWorked, priorYearHours, currentYearHours,
				priorYearHours + currentYearHours + adjustedHours, adjustedHours);
		return ts;
	}

	@Override
	public SortedSet<VolunteerQuickSearchResult> quickSearch(String searchValue, Long volunteerId, long facilityId,
			boolean includeAssignments, boolean includeOrganizations, boolean onlyActiveAssignmentsAndOrgs) {
		StringBuilder sb = new StringBuilder();
		sb.append("select v.id, v.identifyingCode, v.firstName, v.middleName, v.lastName, v.suffix, v.dateOfBirth");
		if (includeAssignments)
			sb.append(", va.id, bs.name, bs.subdivision, bsr.name, f.id, f.name, fp.id, fp.name, va.inactive");
		sb.append(" from ");

		sb.append(VolunteerAssignment.class.getName())
				.append(" va join va.volunteer v join va.facility f left join f.parent fp");
		if (includeAssignments)
			sb.append(" join va.benefitingService bs join va.benefitingServiceRole bsr");

		Map<String, Object> params = new HashMap<>();

		sb.append(
				" where ((TYPE(f) = :facilityClass and f.id = :facilityId) or (TYPE(f) = :locationClass and fp.id = :facilityId))");
		params.put("facilityId", facilityId);
		params.put("facilityClass", Facility.class);
		params.put("locationClass", Location.class);

		sb.append(" and v.status.volunteerActive = true");

		if (onlyActiveAssignmentsAndOrgs)
			sb.append(" and va.inactive = false");

		if (StringUtils.isNotBlank(searchValue)) {
			String[] tokens = searchValue.split("\\W");
			for (int i = 0; i < tokens.length; i++) {
				String token = tokens[i];
				if (StringUtils.isBlank(token))
					continue;

				sb.append(" and (v.firstName like :text").append(i);
				sb.append(" or v.middleName like :text").append(i);
				sb.append(" or v.lastName like :text").append(i);
				sb.append(" or v.suffix like :text").append(i);
				sb.append(" or v.identifyingCode like :text").append(i);
				sb.append(")");
				params.put("text" + i, "%" + token.toLowerCase() + "%");
			}
		}

		if (volunteerId != null) {
			sb.append(" and v.id = :volunteerId");
			params.put("volunteerId", volunteerId);
		}

		Query q = query(sb.toString());
		for (Entry<String, Object> entry : params.entrySet())
			q.setParameter(entry.getKey(), entry.getValue());

		@SuppressWarnings("unchecked")
		List<Object[]> results = q.getResultList();
		if (results.isEmpty())
			return new TreeSet<>();

		/* key = volunteer ID */
		Map<Long, VolunteerQuickSearchResult> m = new HashMap<>();
		for (Object[] result : results) {
			long id = ((Number) result[0]).longValue();
			VolunteerQuickSearchResult r = m.computeIfAbsent(id,
					k -> new VolunteerQuickSearchResult(
							id, String.valueOf(result[1]), Volunteer.getDisplayName((String) result[2],
									(String) result[3], (String) result[4], (String) result[5]),
							(LocalDate) result[6]));

			if (includeAssignments) {
				long vaId = ((Number) result[7]).longValue();
				String bsName = (String) result[8];
				String bsSubdivision = (String) result[9];
				String bsrName = (String) result[10];
				long facId = ((Number) result[11]).longValue();
				String facName = (String) result[12];
				boolean inactive = (Boolean) result[15];

				String locationName = (facId == facilityId) ? "Main Facility" : facName;
				r.getAssignments()
						.add(new VolunteerQuickSearchAssignment(vaId, bsrName + " - "
								+ BenefitingService.getDisplayName(bsName, bsSubdivision) + " @" + locationName,
								!inactive));
			}
		}

		if (includeOrganizations) {
			Map<Long, SortedSet<OrgQuickSearchResult>> orgMap = organizationDAO.quickSearchForTimePosting(m.keySet(),
					onlyActiveAssignmentsAndOrgs, facilityId);
			for (Entry<Long, SortedSet<OrgQuickSearchResult>> entry : orgMap.entrySet()) {
				VolunteerQuickSearchResult r = m.get(entry.getKey());
				entry.getValue().forEach(p -> r.getOrganizations()
						.add(new VolunteerQuickSearchOrganization(p.getId(), p.getName(), p.getVolunteerOrgActive())));
			}
		}

		SortedSet<VolunteerQuickSearchResult> returnResults = new TreeSet<>(m.values());
		return returnResults;
	}

	@Override
	public int inactivateStaleVolunteers(LocalDate entryDateBefore, ZoneId zoneForStatusDate) {
		flush();

		String jpql = "update " + Volunteer.class.getName() //
				+ " set status = :inactiveStatus, statusDate = :today, modifiedBy = :myUser, modifiedDate = :nowUTC, version = version + 1" // "
				+ " where id in (" //
				+ "		select v.id from " + Volunteer.class.getName() + " v" //
				+ " 	where v.status.id = :activeId" //
				+ " 	and v.entryDate < :entryCutoffDate" //
				+ " 	and not exists (" //
				+ "			select a from " + VolunteerAssignment.class.getName()
				+ "			a where a.inactive = false and a.volunteer = v))";

		Query q = query(jpql);

		q.setParameter("inactiveStatus", volunteerStatusDAO.findByLookup(VolunteerStatusType.INACTIVE)) //
				.setParameter("activeId", VolunteerStatusType.ACTIVE.getId()) //
				.setParameter("entryCutoffDate", entryDateBefore) //
				.setParameter("today", LocalDate.now(zoneForStatusDate)) //
				.setParameter("myUser", AbstractAuditedPersistent.getCurrentUserIdForAudit()) //
				.setParameter("nowUTC", ZonedDateTime.now(ZoneId.of("Z")));

		int recordsUpdated = q.executeUpdate();
		return recordsUpdated;
	}

	public List<Volunteer> findByAwardCode(long awardCodeId) {
		@SuppressWarnings("unchecked")
		List<Volunteer> results = query("from " + Volunteer.class.getName() + " where lastAward.id = :awardCodeId")
				.setParameter("awardCodeId", awardCodeId).getResultList();
		return results;
	}

}
