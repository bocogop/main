package org.bocogop.wr.persistence.impl;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import org.bocogop.wr.model.award.Award;
import org.bocogop.wr.model.award.AwardResult;
import org.bocogop.wr.persistence.dao.AwardDAO;

@Repository
public class AwardDAOImpl extends GenericHibernateSortedDAOImpl<Award> implements AwardDAO {

	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(AwardDAOImpl.class);

	private String getHoursCTEs(boolean includeLastAwardDateRestrictions) {
		StringBuilder sb = new StringBuilder("WITH main_hours" //
				+ "       AS (SELECT VolunteerId = v.id," //
				+ "             TotalHours = sum(h.HoursWorked)" //
				+ "           FROM wr.Volunteers v" //
				+ "             JOIN wr.WR_STD_VolunteerStatus s ON v.WR_STD_VolunteerStatusFK = s.id" //
				+ "             JOIN wr.VolunteerAssignments va ON va.WrVolunteersFK = v.id" //
				+ "             JOIN wr.Hours h ON h.WrVolunteerAssignmentsFK = va.id" //
				+ "           WHERE 1=1" //
				+ "             AND v.PrimaryFacilityFK = :facilityId" //
				+ "             AND (:activeStatus IS NULL OR s.Active = :activeStatus)" //
				+ "             AND (:showOnlyAdult = 0 OR v.isYouth = 0)" //
				+ "             AND (:showOnlyYouth = 0 OR v.isYouth = 1)");

		if (includeLastAwardDateRestrictions) {
			sb //
					.append("  AND (:lastAwardOnOrAfter IS NULL OR v.DateLastAward >= :lastAwardOnOrAfter)") //
					.append("  AND (:lastAwardOnOrBefore IS NULL OR v.DateLastAward <= :lastAwardOnOrBefore)");
		}

		sb.append("           GROUP BY v.id" //
				+ "			)," //
				+ "     adjusted_hours" //
				+ "       AS (SELECT VolunteerId = WrVolunteersFK," //
				+ "             TotalHours = sum(AdjustmentHours)" //
				+ "           FROM wr.VolunteerAdjustments" //
				+ "           GROUP BY WrVolunteersFK" //
				+ "			)," //
				+ "     total_hours" //
				+ "       AS (SELECT VolunteerId = v.id," //
				+ "             HoursLastAward = v.HoursLastAward," //
				+ "             TotalHours = ISNULL(mh.TotalHours, 0) + ISNULL(ah.TotalHours, 0)," //
				+ "             v.WR_STD_VolunteerStatusFK," //
				+ "             volAge = case when v.isYouth = 0 then 'A' else 'Y' end," //
				+ "             v.AwardCodesFK" //
				+ "           FROM wr.Volunteers v" //
				+ "             JOIN wr.WR_STD_VolunteerStatus s ON v.WR_STD_VolunteerStatusFK = s.id" //
				+ "             LEFT JOIN main_hours mh ON v.id = mh.VolunteerId" //
				+ "             LEFT JOIN adjusted_hours ah ON v.id = ah.VolunteerId" //
				+ "           WHERE 1=1" //
				+ "             AND v.PrimaryFacilityFK = :facilityId" //
				+ "             AND (:activeStatus IS NULL OR s.Active = :activeStatus)" //
				+ "             AND (:showOnlyAdult = 0 OR v.isYouth = 0)" //
				+ "             AND (:showOnlyYouth = 0 OR v.isYouth = 1)");

		if (includeLastAwardDateRestrictions) {
			sb //
					.append("  AND (:lastAwardOnOrAfter IS NULL OR v.DateLastAward >= :lastAwardOnOrAfter)") //
					.append("  AND (:lastAwardOnOrBefore IS NULL OR v.DateLastAward <= :lastAwardOnOrBefore)");
		}

		sb.append("		)");
		return sb.toString();
	}

	public Query appendCTEParams(Long facilityId, boolean includeAdult, boolean includeYouth, boolean includeOther,
			boolean includeActive, boolean includeSeparated, Query query) {
		Integer activeStatus = null;
		if (includeActive && includeSeparated) {
			activeStatus = null;
		} else {
			activeStatus = includeActive ? 1 : 0;
		}

		return query //
				.setParameter("facilityId", facilityId) //
				.setParameter("activeStatus", activeStatus) //
				.setParameter("showOnlyAdult", includeAdult && !includeYouth && !includeOther)
				.setParameter("showOnlyYouth", includeYouth && !includeAdult && !includeOther);
	}

	public List<AwardResult> findProcessedAwards(Long facilityId, boolean includeAdult, boolean includeYouth,
			boolean includeOther, boolean includeActive, boolean includeSeparated, LocalDate lastAwardOnOrAfter,
			LocalDate lastAwardOnOrBefore) {
		if (!includeAdult && !includeYouth && !includeOther)
			return new ArrayList<>();
		if (!includeActive && !includeSeparated)
			return new ArrayList<>();

		List<String> validAwardTypes = new ArrayList<>();
		if (includeAdult)
			validAwardTypes.add("A");
		if (includeYouth)
			validAwardTypes.add("Y");
		if (includeOther)
			validAwardTypes.add("O");

		String queryStr = getHoursCTEs(true) //
				+ " SELECT" //
				+ "  VolunteerId = th.VolunteerId," //
				+ "  AwardId = v.AwardCodesFK," //
				+ "  VolLastName = v.LastName," //
				+ "  VolFirstName = v.FirstName," //
				+ "  DateLastAward = v.DateLastAward," //
				+ "  HoursLastAward = v.HoursLastAward," //
				+ "  CurrentAwardName = va.AwardName," //
				+ "  CurrentAwardHours = va.AwardHours," //
				+ "  ActualHours = th.TotalHours," //
				+ "  YearsWorked = vyw.YearsWorked," //
				+ "  AgeStatus = CASE WHEN v.isYouth = 0 THEN 'A' ELSE 'Y' END," //
				+ "  VolStatus = s.Name," //
				+ "  DateLastVolunteered = vyw.DateLastVolunteered," //
				+ "  AwardType = va.AwardType " //
				+ " FROM total_hours th" //
				+ "  JOIN wr.Volunteers v ON th.VolunteerId = v.id" //
				+ "  JOIN wr.WR_STD_VolunteerStatus s ON v.WR_STD_VolunteerStatusFK = s.id" //
				+ "  JOIN wr.VolunteerAwards va ON v.AwardCodesFK = va.id" //
				+ "  JOIN VolunteerYearsWorked vyw ON vyw.VolunteerId = th.VolunteerId" //
				+ " WHERE va.AwardType in (:validAwardTypes)" //
				+ " ORDER BY CurrentAwardName, VolLastName, VolFirstName ASC";

		Query query = em.createNativeQuery(queryStr);
		appendCTEParams(facilityId, includeAdult, includeYouth, includeOther, includeActive, includeSeparated, query);
		query.setParameter("lastAwardOnOrAfter", lastAwardOnOrAfter) //
				.setParameter("lastAwardOnOrBefore", lastAwardOnOrBefore) //
				.setParameter("validAwardTypes", validAwardTypes);

		@SuppressWarnings("unchecked")
		List<Object[]> results = query.getResultList();

		List<AwardResult> r = new ArrayList<>(results.size());
		for (Object[] row : results) {
			AwardResult ar = new AwardResult();
			ar.setVolunteerId(((Number) row[0]).longValue());
			ar.setDeservedAwardId(row[1] != null ? ((Number) row[1]).longValue() : 0);
			ar.setVolLastName((String) row[2]);
			ar.setVolFirstName((String) row[3]);
			ar.setDateLastAward(((Timestamp) row[4]).toLocalDateTime().toLocalDate());
			ar.setHoursLastAward(row[5] != null ? ((Number) row[5]).longValue() : 0);
			ar.setCurrentAwardName((String) row[6]);
			ar.setCurrentAwardHours(row[7] != null ? ((Number) row[7]).longValue() : 0);
			ar.setActualHours(row[8] != null ? ((Number) row[8]).longValue() : 0);
			ar.setYearsWorked(row[9] != null ? ((Number) row[9]).longValue() : 0);
			ar.setAgeStatus((String) row[10]);
			ar.setVolStatus((String) row[11]);
			ar.setDateLastVolunteered(row[12] != null ? ((Date) row[12]).toLocalDate() : null);
			ar.setAwardType((String) row[13]);
		
			r.add(ar);
		}
		return r;
	}

	@SuppressWarnings("unchecked")
	public List<AwardResult> findPotentialAwards(Long facilityId, boolean includeAdult, boolean includeYouth,
			boolean includeActive, boolean includeSeparated) {
		if (!includeAdult && !includeYouth)
			return new ArrayList<>();
		if (!includeActive && !includeSeparated)
			return new ArrayList<>();

		List<String> validAgeStatuses = new ArrayList<>();
		if (includeAdult)
			validAgeStatuses.add("A");
		if (includeYouth)
			validAgeStatuses.add("Y");
		// other not supported for potentials - CPB

		String queryStr = getHoursCTEs(false) //
				+ " , ordered_awards AS (" //
				+ "		SELECT" //
				+ "         th.VolunteerId," //
				+ "         th.TotalHours," //
				+ "         th.HoursLastAward," //
				+ "         th.WR_STD_VolunteerStatusFK," //
				+ "         AwardId = va.Id," //
				+ "         DeservedAwardName = va.AwardName," //
				+ "         DeservedAwardHours = va.AwardHours," //
				+ "         DeservedAwrdType = va.AwardType," //
				+ "         row_num = row_number() OVER(" //
				+ "               PARTITION BY th.VolunteerId" //
				+ "               ORDER BY va.RequiredHours DESC)" //
				+ "       FROM total_hours th" //
				+ "         JOIN wr.VolunteerAwards va ON va.AwardType in (:validAgeStatuses)" //
				+ "				AND th.volAge = va.AwardType" //
				+ "				AND va.RequiredHours <= th.TotalHours" //
				+ "				AND va.RequiredHours > 0" //
				+ "				AND ISNULL(th.HoursLastAward, -1) < va.RequiredHours" //
				+ "				AND ISNULL(th.AwardCodesFK, -1) <> va.id" //
				+ "	)" //
				+ " SELECT" //
				+ "  VolunteerId = oa.VolunteerId," //
				+ "  AwardId = oa.AwardId," //
				+ "  VolLastName = v.LastName," //
				+ "  VolFirstName = v.FirstName," //
				+ "  DateLastAward = v.DateLastAward," //
				+ "  HoursLastAward = v.HoursLastAward," //
				+ "  CurrentAwardName = va.AwardName," //
				+ "  CurrentAwardHours = va.AwardHours," //
				+ "  DeservedAwardName = oa.DeservedAwardName," //
				+ "  DeservedAwardHours = oa.DeservedAwardHours," //
				+ "  ActualHours = oa.TotalHours," //
				+ "  YearsWorked = vyw.YearsWorked," //
				+ "  AgeStatus = CASE WHEN v.isYouth = 0 THEN 'A' ELSE 'Y' END," //
				+ "  VolStatus = s.Name," //
				+ "  DateLastVolunteered = vyw.DateLastVolunteered," //
				+ "  AwardType = oa.DeservedAwrdType"
				+ " " //
				+ " FROM ordered_awards oa" //
				+ "  JOIN VolunteerYearsWorked vyw ON vyw.VolunteerId = oa.VolunteerId" //
				+ "  JOIN wr.WR_STD_VolunteerStatus s ON oa.WR_STD_VolunteerStatusFK = s.id" //
				+ "  JOIN wr.Volunteers v ON oa.VolunteerId = v.id" //
				+ "  LEFT JOIN wr.VolunteerAwards va ON v.AwardCodesFK = va.id" //
				+ " WHERE row_num = 1" + "  AND ISNULL(va.AwardHours, -1) < oa.DeservedAwardHours"
				+ "  ORDER BY DeservedAwardName, VolLastName, VolFirstName ASC";

		Query query = em.createNativeQuery(queryStr);
		appendCTEParams(facilityId, includeAdult, includeYouth, false, includeActive, includeSeparated, query);

		List<Object[]> results = query //
				.setParameter("validAgeStatuses", validAgeStatuses) //
				.getResultList();

		List<AwardResult> r = new ArrayList<>(results.size());
		for (Object[] row : results) {
			AwardResult ar = new AwardResult();
			ar.setVolunteerId(((Number) row[0]).longValue());
			ar.setDeservedAwardId(row[1] != null ? ((Number) row[1]).longValue() : 0);
			ar.setVolLastName((String) row[2]);
			ar.setVolFirstName((String) row[3]);
			ar.setDateLastAward(row[4] != null ? ((Timestamp) row[4]).toLocalDateTime().toLocalDate() : null);
			ar.setHoursLastAward(row[5] != null ? ((Number) row[5]).longValue() : 0);
			ar.setCurrentAwardName((String) row[6]);
			ar.setCurrentAwardHours(row[7] != null ? ((Number) row[7]).longValue() : 0);
			ar.setDeservedAwardName((String) row[8]);
			ar.setDeservedAwardHours(row[9] != null ? ((Number) row[9]).longValue() : 0);
			ar.setActualHours(row[10] != null ? ((Number) row[10]).longValue() : 0);
			ar.setYearsWorked(row[11] != null ? ((Number) row[11]).longValue() : 0);
			ar.setAgeStatus((String) row[12]);
			ar.setVolStatus((String) row[13]);
			if (row[14] != null)
				ar.setDateLastVolunteered(((Date) row[14]).toLocalDate());
			// ar.setDateLastVolunteered(row[14] != null ? ((Date)
			// row[14]).toLocalDate() : null);
			ar.setAwardType((String) row[15]);
		
			r.add(ar);
		}
		return r;
	}

}
