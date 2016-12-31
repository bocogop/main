package org.bocogop.wr.persistence.impl.voter.demographics;

import static org.bocogop.wr.persistence.dao.voter.demographics.VolDemoColumn.ACTIVE_ASSIGNMENTS;
import static org.bocogop.wr.persistence.dao.voter.demographics.VolDemoColumn.PARKING_STICKERS;
import static org.bocogop.wr.persistence.dao.voter.demographics.VolDemoColumn.SUPERVISORS;
import static org.bocogop.wr.persistence.dao.voter.demographics.VolDemoColumn.TOTAL_DONATIONS;
import static org.bocogop.wr.persistence.dao.voter.demographics.VolDemoColumn.UNIFORMS;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.persistence.Query;

import org.apache.commons.lang3.StringUtils;
import org.bocogop.wr.model.voter.VoterDemographics;
import org.bocogop.wr.persistence.dao.voter.demographics.VolDemoColumn;
import org.bocogop.wr.persistence.dao.voter.demographics.VolDemoDAO;
import org.bocogop.wr.persistence.dao.voter.demographics.VolDemoSearchParams;
import org.bocogop.wr.persistence.impl.AbstractAppDAOImpl;
import org.bocogop.wr.util.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class VolDemoDAOImpl extends AbstractAppDAOImpl<VoterDemographics> implements VolDemoDAO {
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(VolDemoDAOImpl.class);

	static class SearchContext {
		VolDemoSearchParams searchParams;
		Map<String, Object> params = new HashMap<>();
		StringBuilder sb = new StringBuilder();

		public SearchContext(VolDemoSearchParams searchParams) {
			this.searchParams = searchParams;
		}

		SearchContext append(Object s) {
			sb.append(s);
			return this;
		}

		SearchContext append(String s) {
			sb.append(s);
			return this;
		}

		void addParam(String name, Object val) {
			params.put(name, val);
		}
	}

	@Override
	public List<VoterDemographics> findDemographics(VolDemoSearchParams searchParams, int start, int length) {
		if (!searchParams.isIncludeActive() && !searchParams.isIncludeInactive() && !searchParams.isIncludeTerminated()
				&& !searchParams.isIncludeTerminatedByCause())
			return new ArrayList<>();

		SearchContext sc = new SearchContext(searchParams);

		boolean includeAssignments = searchParams.displayCols.contains(ACTIVE_ASSIGNMENTS)
				|| searchParams.displayCols.contains(SUPERVISORS);
		boolean includeParkingStickers = searchParams.displayCols.contains(PARKING_STICKERS);
		boolean includeUniforms = searchParams.displayCols.contains(UNIFORMS);
		boolean includeDonations = searchParams.displayCols.contains(TOTAL_DONATIONS);
		sc.append("WITH");

		if (searchParams.isLocal()) {
			sc.append("			vols_at_precinct AS (") //
					.append("		SELECT DISTINCT TOP 2000000000 va.WrVotersFK") //
					.append("		FROM wr.VoterAssignments va") //
					.append("		WHERE va.RootPrecinctFK = :precinctId");
			if (searchParams.onlyActive())
				sc.append(" 			AND va.IsInactive = 0");
			sc.append("			), ");
			sc.addParam("precinctId", searchParams.precinctId);
		}

		sc.append("				all_assigned_vols AS (") //
				.append("			SELECT *") //
				.append("			FROM wr.Voters v");
		if (searchParams.isLocal())
			sc.append("					LEFT JOIN vols_at_precinct vaf ON v.id = vaf.WrVotersFK");
		sc.append("					WHERE 1=1"); //
		appendWhereClauseItemsForVoterRestrictions(sc);
		sc.append("				)");

		if (includeAssignments)
			appendCTEForAssignments(sc);
		if (includeParkingStickers)
			appendCTEForParkingStickers(sc);
		if (includeUniforms)
			includeCTEForUniforms(sc);

		sc.append("	SELECT v.id,") //
				.append("		v.IdentifyingCode,") //
				.append("		v.LastName, v.FirstName, v.MiddleName, v.NameSuffix, v.Nickname,") //
				.append("		v.DateOfBirth, v.Age, v.IsYouth,") //
				.append("		Gender = g.Name,") //
				.append("		StatusName = vs.name, v.StatusDate,") //
				.append("		v.StreetAddress1, v.StreetAddress2, v.City, State = st.Name, StateId = st.Id, v.Zipcode,") //
				.append(includeParkingStickers ? " vps.combined_parking_stickers," : " combined_parking_stickers = '',") //
				.append(includeUniforms ? " vu.combined_uniforms," : " combined_uniforms = '',") //
				.append("		v.Telephone, v.AlternateTelephone, v.AlternateTelephone2,") //
				.append("		v.EmailAddress,") //
				.append("		v.EmergencyContactName, v.EmergencyContactRelationship, v.EmergencyContactTelephone, v.EmergencyContactAlternateTelephone,") //
				.append("		v.PrimaryPrecinctFK, PrimaryPrecinctName = ci.nameofinstitution + ' (' + ci.StationNumber + ')',") //
				.append("		v.EntryDate,") //
				.append(includeAssignments ? "va.combined_assignments," : " combined_assignments = '',") //
				.append("		svh.LastVoteredDate,") //
				.append("		svh.CurrentYearHours,") //
				.append("		svh.PriorYearHours,") //
				.append("		svh.TotalAdjustedHours,") //
				.append("		svh.TotalHours,") //
				.append(includeDonations //
						? "		TotalDonations = (select ISNULL(sum(dd.DonationValue), 0)" //
								+ "	from wr.DonationDetail dd" //
								+ "	join wr.DonationSummary ds on dd.DonationSummaryFK = ds.id" //
								+ "	join wr.Donor d on ds.DonorFK = d.id" //
								+ "	where d.WrVotersFK = v.id),"
						: "	TotalDonations = 0,") //
				.append("		v.HoursLastAward,") //
				.append("		v.DateLastAward,") //
				.append("		PrimaryOrganization = o.OrganizationName") //
		;

		sc.append("	FROM all_assigned_vols v") //
				.append("		JOIN sdsadm.std_gender g ON v.std_genderfk = g.id") //
				.append("		JOIN wr.WR_STD_VoterStatus vs ON v.WR_STD_VoterStatusFK = vs.id") //
				.append("		LEFT JOIN sdsadm.std_state st ON v.std_statefk = st.id") //
				.append("		LEFT JOIN dbo.FinalOrganizationName o ON v.primaryorganizationfk = o.id") //
				.append("		LEFT JOIN dbo.combinedinstitutions ci ON v.primaryprecinctfk = ci.id") //
				.append("		LEFT JOIN wr.SUMM_Voter_Hours svh on v.id = svh.VoterId"); //
		if (includeAssignments)
			sc.append("			LEFT JOIN vol_assignments va on v.id = va.VoterId");
		if (includeParkingStickers)
			sc.append("		LEFT JOIN vol_parking_stickers vps on v.id = vps.VoterId");
		if (includeUniforms)
			sc.append("		LEFT JOIN vol_uniforms vu on v.id = vu.VoterId");

		sc.append("	WHERE 1=1");
		appendFilterWhereClauseItems(sc);
		appendWhereClauseItemsForAdvancedRestrictions(sc);

		String[] orderByCols = getOrderByCols(searchParams.sortAscending);
		String[] ascendingOrderByCols = getOrderByCols(true);
		sc.append("	ORDER BY ").append(orderByCols[searchParams.sortColIndex]);
		if (searchParams.sortColIndex != 1)
			sc.append(", ").append(ascendingOrderByCols[1]);

		sc.append(" OFFSET " + start + " ROWS");
		if (length != -1)
			sc.append(" FETCH NEXT " + length + " ROWS ONLY");

		Query query = em.createNativeQuery(sc.sb.toString());

		for (Entry<String, Object> entry : sc.params.entrySet())
			query.setParameter(entry.getKey(), entry.getValue());

		@SuppressWarnings("unchecked")
		List<Object[]> queryResults = query.getResultList();

		List<VoterDemographics> results = new ArrayList<>(queryResults.size());
		for (Object[] row : queryResults) {
			VoterDemographics vd = buildFromRow(row);
			results.add(vd);
		}
		return results;
	}

	@Override
	public int[] findDemographicsTotalAndFilteredNumber(VolDemoSearchParams searchParams) {
		SearchContext sc = new SearchContext(searchParams);

		boolean includeAssignments = searchParams.displayCols.contains(ACTIVE_ASSIGNMENTS)
				|| searchParams.displayCols.contains(SUPERVISORS);

		sc.append("WITH");

		if (searchParams.isLocal()) {
			sc.append("			vols_at_precinct AS (") //
					.append("		SELECT DISTINCT TOP 2000000000 va.WrVotersFK") //
					.append("		FROM wr.VoterAssignments va") //
					.append("		WHERE va.RootPrecinctFK = :precinctId");

			if (searchParams.onlyActive())
				sc.append(" 			AND va.IsInactive = 0");
			sc //
					.append("	), ");
			sc.addParam("precinctId", searchParams.precinctId);
		}

		sc.append("				all_assigned_vols AS (") //
				.append("			SELECT v.*") //
				.append("			FROM wr.Voters v");
		if (searchParams.isLocal())
			sc.append("					LEFT JOIN vols_at_precinct vaf ON v.id = vaf.WrVotersFK");
		sc.append("					WHERE 1=1"); //
		appendWhereClauseItemsForVoterRestrictions(sc);
		sc.append("				)"); //

		if (includeAssignments)
			appendCTEForAssignments(sc);

		sc.append(" SELECT allCount = count(*), filteredCount = sum(case when (1=1");
		appendFilterWhereClauseItems(sc);
		sc.append(") then 1 else 0 end)") //
				.append("	FROM all_assigned_vols v") //
				.append("		LEFT JOIN sdsadm.std_state st ON v.std_statefk = st.id") //
				.append("		LEFT JOIN dbo.FinalOrganizationName o ON v.primaryorganizationfk = o.id") //
				.append("		LEFT JOIN dbo.combinedinstitutions ci ON v.primaryprecinctfk = ci.id") //
				.append("		LEFT JOIN wr.SUMM_Voter_Hours svh on v.id = svh.VoterId"); //
		if (includeAssignments)
			sc.append("			LEFT JOIN vol_assignments va on v.id = va.VoterId");
		sc.append("					WHERE 1=1"); //
		appendWhereClauseItemsForAdvancedRestrictions(sc);

		Query query = em.createNativeQuery(sc.sb.toString());

		for (Entry<String, Object> entry : sc.params.entrySet())
			query.setParameter(entry.getKey(), entry.getValue());

		Object[] r = (Object[]) query.getSingleResult();
		int totalCount = ((Number) r[0]).intValue();
		Number subsetCount = ((Number) r[1]);
		return new int[] { totalCount, subsetCount == null ? 0 : subsetCount.intValue() };
	}

	private void appendCTEForAssignments(SearchContext sb) {
		sb.append("				,vol_assignments AS (") //
				.append("			SELECT VoterId = v.Id") //
				.append("				,combined_assignments = STUFF((") //
				.append("					SELECT ';;' + CONCAT(bs.ServiceName, case when ISNULL(LTRIM(RTRIM(bs.Subdivision)), '') <> '' then ' - ' + bs.Subdivision else '' end") //
				.append("						, ' - ', bsr.Name, '|', bsr.contactName, '|', bsr.contactEmail, '|', bsr.contactPhone)") //
				.append("					FROM wr.VoterAssignments va") //
				.append("						JOIN wr.BenefitingServices bs on va.WrBenefitingServicesFK = bs.Id") //
				.append("						JOIN wr.BenefitingServiceRoles bsr on va.WrBenefitingServiceRolesFK = bsr.Id") //
				.append("					WHERE va.WrVotersFK = v.Id") //
				.append("						AND va.IsInactive = 0") //
				.append("					ORDER BY bs.ServiceName, bs.Subdivision, bsr.Name") //
				.append("				FOR XML PATH('') ,TYPE).value('.', 'varchar(max)'), 1, 2, '')") //
				.append("			FROM all_assigned_vols v") //
				.append("			GROUP BY v.Id") //
				.append("		)");
	}

	public void includeCTEForUniforms(SearchContext sb) {
		sb.append("				,vol_uniforms AS (") //
				.append("			SELECT VoterId = v.Id") //
				.append("				,combined_uniforms = STUFF((") //
				.append("					SELECT CONCAT(';', ss.SizeName, '|', u.NumberOfShirts)") //
				.append("					FROM wr.Uniforms u") //
				.append("					JOIN wr.ShirtSizes ss ON u.ShirtSizesFK = ss.id") //
				.append("					WHERE u.WrVotersFK = v.Id"); //
		if (sb.searchParams.isLocal())
			sb.append("					AND u.PrecinctFK = :precinctId"); //
		sb.append("					ORDER BY ss.SizeOrder, u.NumberOfShirts") //
				.append("				FOR XML PATH('') ,TYPE).value('.', 'varchar(max)'), 1, 1, '')") //
				.append("			FROM all_assigned_vols v") //
				.append("			GROUP BY v.Id") //
				.append("		)");
	}

	public void appendCTEForParkingStickers(SearchContext sb) {
		sb.append("				,vol_parking_stickers AS (") //
				.append("			SELECT VoterId = v.Id") //
				.append("				,combined_parking_stickers = STUFF((") //
				.append("					SELECT ';' + ps.stickerNumber + '|' + st.name + '|' + ps.licensePlate") //
				.append("					FROM wr.ParkingSticker ps") //
				.append("					LEFT JOIN sdsadm.std_state st ON ps.STD_StateFK = st.id") //
				.append("					WHERE ps.WrVotersFK = v.Id"); //
		if (sb.searchParams.isLocal())
			sb.append("					AND ps.PrecinctFK = :precinctId"); //
		sb.append("					ORDER BY ps.stickerNumber, st.name, ps.licensePlate") //
				.append("				FOR XML PATH('') ,TYPE).value('.', 'varchar(max)'), 1, 1, '')") //
				.append("			FROM all_assigned_vols v") //
				.append("			GROUP BY v.Id") //
				.append("		)");
	}

	private void appendWhereClauseItemsForVoterRestrictions(SearchContext sb) {
		VolDemoSearchParams searchParams = sb.searchParams;

		if (sb.searchParams.isLocal()) {
			sb.append("				AND (") //
					.append("			ISNULL(v.PrimaryPrecinctFK, v.OriginalPrecinctCreatedFK) = :precinctId") //
					.append("			OR vaf.WrVotersFK IS NOT NULL)"); //
		}

		/*
		 * If they want to include all statuses, no need to add any restrictions
		 * - CPB
		 */
		if (searchParams.isIncludeActive() && searchParams.isIncludeInactive() && searchParams.isIncludeTerminated()
				&& searchParams.isIncludeTerminatedByCause())
			return;
	}

	private void appendWhereClauseItemsForAdvancedRestrictions(SearchContext sb) {
		VolDemoSearchParams searchParams = sb.searchParams;

		// ------------ Last Votered Options

		processAdvancedOption(sb, "lastVolOptions=have, haveLastVolOption=haveLastVolLast30",
				"DATEDIFF(day, svh.LastVoteredDate, SYSDATETIME()) <= 30");
		processAdvancedOption(sb, "lastVolOptions=have, haveLastVolOption=haveLastVolLast60",
				"DATEDIFF(day, svh.LastVoteredDate, SYSDATETIME()) <= 60");
		processAdvancedOption(sb, "lastVolOptions=have, haveLastVolOption=haveLastVolLast90",
				"DATEDIFF(day, svh.LastVoteredDate, SYSDATETIME()) <= 90");
		String haveLastVolAfter = searchParams.restrictions.get("haveLastVolAfter");
		if (StringUtils.isNotBlank(haveLastVolAfter))
			processAdvancedOption(sb, "lastVolOptions=have, haveLastVolOption=haveLastVolAfter",
					"svh.LastVoteredDate >= :haveLastVolAfter", "haveLastVolAfter",
					LocalDate.parse(haveLastVolAfter, DateUtil.TWO_DIGIT_DATE_ONLY_FORMAT));

		processAdvancedOption(sb, "lastVolOptions=havent, haventLastVolOption=haventLastVolIn30",
				"svh.LastVoteredDate is null or DATEDIFF(day, svh.LastVoteredDate, SYSDATETIME()) >= 30");
		processAdvancedOption(sb, "lastVolOptions=havent, haventLastVolOption=haventLastVolIn60",
				"svh.LastVoteredDate is null or DATEDIFF(day, svh.LastVoteredDate, SYSDATETIME()) >= 60");
		processAdvancedOption(sb, "lastVolOptions=havent, haventLastVolOption=haventLastVolIn90",
				"svh.LastVoteredDate is null or DATEDIFF(day, svh.LastVoteredDate, SYSDATETIME()) >= 90");
		processAdvancedOption(sb, "lastVolOptions=havent, haventLastVolOption=haventLastVolEver",
				"svh.LastVoteredDate is null");
		String haventlastVolSince = searchParams.restrictions.get("haventLastVolSince");
		if (StringUtils.isNotBlank(haventlastVolSince))
			processAdvancedOption(sb, "lastVolOptions=havent, haventLastVolOption=haventLastVolSince",
					"svh.LastVoteredDate is null or svh.LastVoteredDate < :haventlastVolSince", "haventlastVolSince",
					LocalDate.parse(haventlastVolSince, DateUtil.TWO_DIGIT_DATE_ONLY_FORMAT));

		// ------------ Status Date options

		String statusDateBefore = searchParams.restrictions.get("statusDateBefore");
		if (StringUtils.isNotBlank(statusDateBefore))
			processAdvancedOption(sb, "statusDateOptions=before", "v.StatusDate <= :statusDateBefore",
					"statusDateBefore", LocalDate.parse(statusDateBefore, DateUtil.TWO_DIGIT_DATE_ONLY_FORMAT));

		String statusDateAfter = searchParams.restrictions.get("statusDateAfter");
		if (StringUtils.isNotBlank(statusDateAfter))
			processAdvancedOption(sb, "statusDateOptions=after", "v.StatusDate >= :statusDateAfter", "statusDateAfter",
					LocalDate.parse(statusDateAfter, DateUtil.TWO_DIGIT_DATE_ONLY_FORMAT));

		String statusDateBetweenStart = searchParams.restrictions.get("statusDateBetweenStart");
		String statusDateBetweenEnd = searchParams.restrictions.get("statusDateBetweenEnd");
		if (StringUtils.isNotBlank(statusDateBetweenStart) && StringUtils.isNotBlank(statusDateBetweenEnd)) {
			Map<String, Object> newParams = new HashMap<>();
			newParams.put("statusDateAfter",
					LocalDate.parse(statusDateBetweenStart, DateUtil.TWO_DIGIT_DATE_ONLY_FORMAT));
			newParams.put("statusDateBefore",
					LocalDate.parse(statusDateBetweenEnd, DateUtil.TWO_DIGIT_DATE_ONLY_FORMAT));
			processAdvancedOption(sb, "statusDateOptions=between",
					"v.StatusDate >= :statusDateAfter and v.StatusDate <= :statusDateBefore", newParams);
		}

	}

	private void processAdvancedOption(SearchContext sb, String nameValCSVs, String whereClauseItem) {
		processAdvancedOption(sb, nameValCSVs, whereClauseItem, null);
	}

	private void processAdvancedOption(SearchContext sb, String nameValCSVs, String whereClauseItem, String paramName,
			Object paramValue) {
		Map<String, Object> paramsToAdd = new HashMap<>();
		if (paramName != null)
			paramsToAdd.put(paramName, paramValue);
		processAdvancedOption(sb, nameValCSVs, whereClauseItem, paramsToAdd);
	}

	private void processAdvancedOption(SearchContext sb, String nameValCSVs, String whereClauseItem,
			Map<String, Object> newParams) {
		Map<String, String> rMap = sb.searchParams.restrictions;

		String[] tokens = nameValCSVs.split(",");
		for (String t : tokens) {
			String[] nameVal = t.split("=");
			if (!nameVal[1].trim().equals(rMap.get(nameVal[0].trim())))
				return;
		}

		sb.append(" AND (").append(whereClauseItem).append(")");
		if (newParams != null)
			sb.params.putAll(newParams);
	}

	private void appendFilterWhereClauseItems(SearchContext sb) {
		VolDemoSearchParams searchParams = sb.searchParams;

		boolean includeAssignments = searchParams.displayCols.contains(ACTIVE_ASSIGNMENTS)
				|| searchParams.displayCols.contains(SUPERVISORS);

		List<String> whereClauseItems = new ArrayList<>();
		String searchValue = searchParams.searchValue;

		if (StringUtils.isNotBlank(searchValue)) {
			searchValue = searchValue.replaceAll("[^\\p{Print}]", "");
			String[] tokens = searchValue.split("\\s");
			for (int i = 0; i < tokens.length; i++) {
				if (StringUtils.isBlank(tokens[i]))
					continue;
				whereClauseItems.add("(" //
						+ "    v.lastName like :search" + i //
						+ " or v.firstName like :search" + i //
						+ " or v.middleName like :search" + i //
						+ " or v.nameSuffix like :search" + i //
						+ " or v.StreetAddress1 like :search" + i //
						+ " or v.StreetAddress2 like :search" + i //
						+ " or v.City like :search" + i //
						+ " or st.Name like :search" + i //
						+ " or v.ZipCode like :search" + i //
						+ " or v.EmailAddress like :search" + i //
						+ " or v.EmergencyContactName like :search" + i //
						+ " or ci.nameofinstitution like :search" + i //
						+ " or ci.stationnumber like :search" + i //
						+ " or o.OrganizationName like :search" + i //
						+ (includeAssignments ? " or va.combined_assignments like :search" + i : "") //
						+ ")");
				sb.addParam("search" + i, "%" + tokens[i] + "%");
			}
		}

		for (Entry<VolDemoColumn, String> entry : searchParams.filters.entrySet()) {
			VolDemoColumn colIndex = entry.getKey();
			String filterText = entry.getValue();
			/*
			 * all the where clause table references below have to exist in both
			 * the main query and the totalAndFilteredNumber query - CPB
			 */
			if (colIndex == VolDemoColumn.DOB) {
				// Birth month
				whereClauseItems.add("DATEPART(month, v.DateOfBirth) = :monthIndex");
				sb.addParam("monthIndex", filterText);
			} else if (colIndex == VolDemoColumn.AGE_GROUP) {
				// Age Group
				whereClauseItems.add("v.IsYouth = :isYouth");
				sb.addParam("isYouth", "Adult".equalsIgnoreCase(filterText) ? "0" : "1");
			} else if (colIndex == VolDemoColumn.GENDER) {
				// Gender
				whereClauseItems.add("v.std_genderfk = :genderId");
				sb.addParam("genderId", filterText);
			} else if (colIndex == VolDemoColumn.ENTRY_DATE) {
				String[] tokens = filterText.split("/", -1);
				if (!"".equals(tokens[0])) {
					whereClauseItems.add("DATEPART(month, v.EntryDate) = :entryDateMonthIndex");
					sb.addParam("entryDateMonthIndex", tokens[0]);
				}
				if (!"".equals(tokens[1])) {
					whereClauseItems.add("DATEPART(year, v.EntryDate) = :entryDateYearIndex");
					sb.addParam("entryDateYearIndex", tokens[1]);
				}
			} else if (colIndex == VolDemoColumn.STATE) {
				// State
				whereClauseItems.add("st.id = :stateId");
				sb.addParam("stateId", filterText);
			} else if (colIndex == VolDemoColumn.STATUS_DATE) {
				String[] tokens = filterText.split("/", -1);
				if (!"".equals(tokens[0])) {
					whereClauseItems.add("DATEPART(month, v.StatusDate) = :statusDateMonthIndex");
					sb.addParam("statusDateMonthIndex", tokens[0]);
				}
				if (!"".equals(tokens[1])) {
					whereClauseItems.add("DATEPART(year, v.StatusDate) = :statusDateYearIndex");
					sb.addParam("statusDateYearIndex", tokens[1]);
				}
			} else if (colIndex == VolDemoColumn.PRIMARY_PRECINCT) {
				// Primary Precinct - mine vs others
				if ("mine".equals(filterText)) {
					whereClauseItems.add("ci.id = :workingPrecinctId");
					sb.addParam("workingPrecinctId", searchParams.workingPrecinctId);
				} else if ("others".equals(filterText)) {
					whereClauseItems.add("(ci.id is null or ci.id <> :workingPrecinctId)");
					sb.addParam("workingPrecinctId", searchParams.workingPrecinctId);
				}
			} else if (colIndex == VolDemoColumn.LAST_VOTERED_DATE) {
				String[] tokens = filterText.split("/", -1);
				if (!"".equals(tokens[0])) {
					whereClauseItems.add("DATEPART(month, svh.LastVoteredDate) = :lastVoteredDateMonthIndex");
					sb.addParam("lastVoteredDateMonthIndex", tokens[0]);
				}
				if (!"".equals(tokens[1])) {
					whereClauseItems.add("DATEPART(year, svh.LastVoteredDate) = :lastVoteredDateYearIndex");
					sb.addParam("lastVoteredDateYearIndex", tokens[1]);
				}
			} else if (colIndex == VolDemoColumn.DATE_LAST_AWARD) {
				String[] tokens = filterText.split("/", -1);
				if (!"".equals(tokens[0])) {
					whereClauseItems.add("DATEPART(month, v.DateLastAward) = :dateLastAwardMonthIndex");
					sb.addParam("dateLastAwardMonthIndex", tokens[0]);
				}
				if (!"".equals(tokens[1])) {
					whereClauseItems.add("DATEPART(year, v.DateLastAward) = :dateLastAwardYearIndex");
					sb.addParam("dateLastAwardYearIndex", tokens[1]);
				}
			}
		}

		if (!whereClauseItems.isEmpty())
			// requires that multi-expression criteria be surrounded earlier
			// with parens - CPB
			sb.append(" AND ").append(StringUtils.join(whereClauseItems, " AND "));
	}

	private String[] getOrderByCols(boolean asc) {
		String orderDir = asc ? "" : " desc";
		String[] orderByCols = { "v.LastName" + orderDir, // dummy value for
															// checkbox column
				"v.LastName" + orderDir + ", v.FirstName" + orderDir + ", v.MiddleName" + orderDir + ", v.NameSuffix"
						+ orderDir, //
				"v.DateOfBirth" + orderDir, //
				"v.Age" + orderDir, //
				"v.IsYouth" + orderDir, //
				"g.Name" + orderDir, //
				"v.IdentifyingCode" + orderDir, //
				"v.EntryDate" + orderDir, //
				"ISNULL(v.StreetAddress1, '')" + orderDir + ", ISNULL(v.StreetAddress2, '')" + orderDir
						+ ", ISNULL(v.City, '')" + orderDir, //
				"ISNULL(v.StreetAddress1, '')" + orderDir + ", ISNULL(v.StreetAddress2, '')" + orderDir, //
				"ISNULL(v.City, '')" + orderDir, //
				"ISNULL(v.StateName, '')" + orderDir, //
				"ISNULL(v.ZipCode, '')" + orderDir, //
				"vps.combined_parking_stickers" + orderDir, //
				"vu.combined_uniforms" + orderDir, //
				"ISNULL(v.Telephone, '')" + orderDir + ", ISNULL(v.AlternateTelephone, '')" + orderDir
						+ ", ISNULL(v.AlternateTelephone2, '')" + orderDir, //
				"ISNULL(v.Telephone, '')" + orderDir, //
				"ISNULL(v.AlternateTelephone, '')" + orderDir, //
				"ISNULL(v.AlternateTelephone2, '')" + orderDir, //
				"ISNULL(v.EmailAddress, '')" + orderDir, //
				"ISNULL(v.EmergencyContactName, '')" + orderDir + ", ISNULL(v.EmergencyContactRelationship, '')"
						+ orderDir, //
				"vs.Name" + orderDir, //
				"v.StatusDate" + orderDir, //
				"ci.nameofinstitution" + orderDir, //
				"va.combined_assignments" + orderDir, //
				"STUFF(va.combined_assignments, 1, CHARINDEX('|', va.combined_assignments), '')" + orderDir, //
				"svh.LastVoteredDate" + orderDir, //
				"o.OrganizationName" + orderDir, //
				"svh.CurrentYearHours" + orderDir, //
				"svh.PriorYearHours" + orderDir, //
				"svh.TotalAdjustedHours" + orderDir, //
				"svh.TotalHours" + orderDir, //
				"TotalDonations" + orderDir, //
				"v.HoursLastAward" + orderDir, //
				"v.DateLastAward" + orderDir, //
		};
		return orderByCols;
	}

	private VoterDemographics buildFromRow(Object[] row) {
		int index = 0;
		long id = ((Number) row[index++]).longValue();
		String identifyingCode = (String) row[index++];
		String lastName = (String) row[index++];
		String firstName = (String) row[index++];
		String middleName = (String) row[index++];
		String nameSuffix = (String) row[index++];
		String nickname = (String) row[index++];
		LocalDate birthDate = ((Timestamp) row[index++]).toLocalDateTime().toLocalDate();
		int age = ((Number) row[index++]).intValue();
		boolean youth = 1 == ((Integer) row[index++]);
		String gender = (String) row[index++];
		String status = (String) row[index++];
		LocalDate statusDate = DateUtil.asLocalDate((Date) row[index++]);
		String streetAddress1 = (String) row[index++];
		String streetAddress2 = (String) row[index++];
		String city = (String) row[index++];
		String state = (String) row[index++];
		// possibly null so we don't convert until below
		Number stateId = (Number) row[index++];
		String zip = (String) row[index++];
		String combinedParkingStickers = (String) row[index++];
		String combinedUniforms = (String) row[index++];
		String phone = (String) row[index++];
		String altPhone = (String) row[index++];
		String altPhone2 = (String) row[index++];
		String email = (String) row[index++];
		String emerContactName = (String) row[index++];
		String emerContactRelationship = (String) row[index++];
		String emerContactPhone = (String) row[index++];
		String emerContactAltPhone = (String) row[index++];
		// possibly null so we don't convert until below
		Number primaryPrecinctId = (Number) row[index++];
		String primaryPrecinctName = (String) row[index++];
		LocalDate entryDate = DateUtil.asLocalDate((Date) row[index++]);
		String combinedAssignments = (String) row[index++];
		// possibly null so we don't convert until below
		LocalDate lastVoteredDate = DateUtil.asLocalDate((Date) row[index++]);
		// possibly null so we don't convert until below
		Number currentYearHours = (Number) row[index++];
		// possibly null so we don't convert until below
		Number priorHours = (Number) row[index++];
		// possibly null so we don't convert until below
		Number adjustedHours = (Number) row[index++];
		// possibly null so we don't convert until below
		Number totalHours = (Number) row[index++];
		BigDecimal totalDonations = new BigDecimal(((Number) row[index++]).toString());
		// possibly null so we don't convert until below
		Number hoursLastAward = (Number) row[index++];
		// possibly null so we don't convert until below
		LocalDate dateLastAward = DateUtil.asLocalDate((Date) row[index++]);
		String primaryOrganization = (String) row[index++];

		VoterDemographics vd = new VoterDemographics();
		// TODO BOCOGOP
		return vd;
	}

}
