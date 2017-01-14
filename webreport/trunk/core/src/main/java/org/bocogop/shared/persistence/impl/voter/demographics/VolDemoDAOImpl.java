package org.bocogop.shared.persistence.impl.voter.demographics;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.persistence.Query;

import org.apache.commons.lang3.StringUtils;
import org.bocogop.shared.model.voter.VoterDemographics;
import org.bocogop.shared.persistence.dao.voter.demographics.VolDemoColumn;
import org.bocogop.shared.persistence.dao.voter.demographics.VolDemoDAO;
import org.bocogop.shared.persistence.dao.voter.demographics.VolDemoSearchParams;
import org.bocogop.shared.persistence.impl.AbstractAppDAOImpl;
import org.bocogop.shared.util.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
	public List<VoterDemographics> findDemographics(VolDemoSearchParams searchParams, int start, int length,
			long appUserId) {
		SearchContext sc = new SearchContext(searchParams);

		boolean includeParticipations = false; // searchParams.displayCols.contains(PARTICIPATIONS);
		boolean includeIssues = false; // searchParams.displayCols.contains(ISSUES);

		sc.append("WITH");

		sc.append("	all_voters AS (") //
				.append("	SELECT v.*") //
				.append("	FROM Voter v") //
				.append(")");

		if (includeParticipations)
			appendCTEForParticipations(sc);
		if (includeIssues)
			appendCTEForIssues(sc);

		sc.append("	SELECT v.id,") //
				.append("	v.LastName, v.FirstName, v.MiddleName, v.NameSuffix, v.Nickname,") //
				.append("	v.VoterId,") //
				.append("	PrecinctName = p.Name,") // precinct
				.append("	PartyName = pa.Name,") // party
				.append("	v.AffiliatedDate,")

				.append("	v.RegistrationDate,") //
				.append("	v.EffectiveDate,") //
				.append("	v.VoterStatusActive,") //
				.append("	v.VoterStatusReason,") //

				.append("	v.ResidentialAddress, v.ResidentialCity, v.ResidentialState, v.ResidentialZip, v.ResidentialZipPlus,") //

				.append("	GenderName = g.Name,") // Gender
				.append("	v.BirthYear,") //
				.append("	v.AgeApprox,") //

				.append("	v.MailingAddress1, v.MailingAddress2, v.MailingAddress3, v.MailingCity, v.MailingState, v.MailingZip, v.MailingZipPlus, v.MailingCountry,") //
				.append("	v.BallotAddress1, v.BallotAddress2, v.BallotAddress3, v.BallotCity, v.BallotState, v.BallotZip, v.BallotZipPlus, v.BallotCountry,") //

				.append("	v.Phone,") //
				.append("	v.PhoneUserProvided,") //
				.append("	v.Fax,") //
				.append("	v.Email,") //
				.append("	v.EmailUserProvided,") //

				.append(includeParticipations ? " vp.combined_participations," : " combined_participations = '',") //
				.append(includeIssues ? " vi.combined_issues" : " combined_issues = ''") //
		;

		sc.append("	FROM all_voters v") //
				.append("	JOIN Gender g ON v.GenderFK = g.id") //
				.append("	JOIN Precinct p ON v.PrecinctFK = p.id") //
				.append("	JOIN Party pa ON v.PartyFK = pa.id"); //
		if (includeParticipations)
			sc.append("		LEFT JOIN voter_participations vp on v.id = vp.VoterFK");
		if (includeIssues)
			sc.append("		LEFT JOIN voter_issues vi on v.id = vi.VoterFK");

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
	public int[] findDemographicsTotalAndFilteredNumber(VolDemoSearchParams searchParams, long appUserId) {
		SearchContext sc = new SearchContext(searchParams);

		boolean includeParticipations = false; // searchParams.displayCols.contains(PARTICIPATIONS);
		boolean includeIssues = false; // searchParams.displayCols.contains(ISSUES);

		sc.append("WITH");
		sc.append("		all_assigned_voters AS (") //
				.append("	SELECT v.*") //
				.append("	FROM Voter v") //
				.append("		JOIN Precinct pr on v.PrecinctFK = pr.id");

		sc.append("	)"); //

		if (includeParticipations)
			appendCTEForParticipations(sc);

		sc.append(" SELECT allCount = count(*), filteredCount = sum(case when (1=1");
		appendFilterWhereClauseItems(sc);
		sc.append(") then 1 else 0 end)") //
				.append("	FROM all_assigned_voters v"); //
		if (includeParticipations)
			sc.append("			LEFT JOIN voter_participations vp on v.id = vp.VoterFK");
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

	private void appendCTEForParticipations(SearchContext sb) {
		sb.append("				,vol_assignments AS (") //
				.append("			SELECT VoterId = v.Id") //
				.append("				,combined_assignments = STUFF((") //
				.append("					SELECT ';;' + CONCAT(bs.ServiceName, case when ISNULL(LTRIM(RTRIM(bs.Subdivision)), '') <> '' then ' - ' + bs.Subdivision else '' end") //
				.append("						, ' - ', bsr.Name, '|', bsr.contactName, '|', bsr.contactEmail, '|', bsr.contactPhone)") //
				.append("					FROM wr.VoterAssignments va") //
				.append("						JOIN wr.BenefitingServices bs on va.WrBenefitingServicesFK = bs.Id") //
				.append("						JOIN wr.BenefitingServiceRoles bsr on va.WrBenefitingServiceRolesFK = bsr.Id") //
				.append("					WHERE va.VoterFK = v.Id") //
				.append("						AND va.IsInactive = 0") //
				.append("					ORDER BY bs.ServiceName, bs.Subdivision, bsr.Name") //
				.append("				FOR XML PATH('') ,TYPE).value('.', 'varchar(max)'), 1, 2, '')") //
				.append("			FROM all_assigned_vols v") //
				.append("			GROUP BY v.Id") //
				.append("		)");
	}

	public void appendCTEForIssues(SearchContext sb) {
		sb.append("				,vol_parking_stickers AS (") //
				.append("			SELECT VoterId = v.Id") //
				.append("				,combined_parking_stickers = STUFF((") //
				.append("					SELECT ';' + ps.stickerNumber + '|' + st.name + '|' + ps.licensePlate") //
				.append("					FROM wr.ParkingSticker ps") //
				.append("					LEFT JOIN sdsadm.std_state st ON ps.STD_StateFK = st.id") //
				.append("					WHERE ps.VoterFK = v.Id") //
				.append("					ORDER BY ps.stickerNumber, st.name, ps.licensePlate") //
				.append("				FOR XML PATH('') ,TYPE).value('.', 'varchar(max)'), 1, 1, '')") //
				.append("			FROM all_assigned_vols v") //
				.append("			GROUP BY v.Id") //
				.append("		)");
	}

	private void appendWhereClauseItemsForAdvancedRestrictions(SearchContext sb) {
		VolDemoSearchParams searchParams = sb.searchParams;

		// ------------ Last Votered Options

		// processAdvancedOption(sb, "lastVolOptions=have,
		// haveLastVolOption=haveLastVolLast30",
		// "DATEDIFF(day, svh.LastVoteredDate, SYSDATETIME()) <= 30");
		// processAdvancedOption(sb, "lastVolOptions=have,
		// haveLastVolOption=haveLastVolLast60",
		// "DATEDIFF(day, svh.LastVoteredDate, SYSDATETIME()) <= 60");
		// processAdvancedOption(sb, "lastVolOptions=have,
		// haveLastVolOption=haveLastVolLast90",
		// "DATEDIFF(day, svh.LastVoteredDate, SYSDATETIME()) <= 90");
		// String haveLastVolAfter =
		// searchParams.restrictions.get("haveLastVolAfter");
		// if (StringUtils.isNotBlank(haveLastVolAfter))
		// processAdvancedOption(sb, "lastVolOptions=have,
		// haveLastVolOption=haveLastVolAfter",
		// "svh.LastVoteredDate >= :haveLastVolAfter", "haveLastVolAfter",
		// LocalDate.parse(haveLastVolAfter,
		// DateUtil.TWO_DIGIT_DATE_ONLY_FORMAT));
		//
		// // ------------ Status Date options
		//
		// String statusDateBefore =
		// searchParams.restrictions.get("statusDateBefore");
		// if (StringUtils.isNotBlank(statusDateBefore))
		// processAdvancedOption(sb, "statusDateOptions=before", "v.StatusDate
		// <= :statusDateBefore",
		// "statusDateBefore", LocalDate.parse(statusDateBefore,
		// DateUtil.TWO_DIGIT_DATE_ONLY_FORMAT));
		//
		// String statusDateAfter =
		// searchParams.restrictions.get("statusDateAfter");
		// if (StringUtils.isNotBlank(statusDateAfter))
		// processAdvancedOption(sb, "statusDateOptions=after", "v.StatusDate >=
		// :statusDateAfter", "statusDateAfter",
		// LocalDate.parse(statusDateAfter,
		// DateUtil.TWO_DIGIT_DATE_ONLY_FORMAT));
		//
		// String statusDateBetweenStart =
		// searchParams.restrictions.get("statusDateBetweenStart");
		// String statusDateBetweenEnd =
		// searchParams.restrictions.get("statusDateBetweenEnd");
		// if (StringUtils.isNotBlank(statusDateBetweenStart) &&
		// StringUtils.isNotBlank(statusDateBetweenEnd)) {
		// Map<String, Object> newParams = new HashMap<>();
		// newParams.put("statusDateAfter",
		// LocalDate.parse(statusDateBetweenStart,
		// DateUtil.TWO_DIGIT_DATE_ONLY_FORMAT));
		// newParams.put("statusDateBefore",
		// LocalDate.parse(statusDateBetweenEnd,
		// DateUtil.TWO_DIGIT_DATE_ONLY_FORMAT));
		// processAdvancedOption(sb, "statusDateOptions=between",
		// "v.StatusDate >= :statusDateAfter and v.StatusDate <=
		// :statusDateBefore", newParams);
		// }

	}

	// private void processAdvancedOption(SearchContext sb, String nameValCSVs,
	// String whereClauseItem) {
	// processAdvancedOption(sb, nameValCSVs, whereClauseItem, null);
	// }
	//
	// private void processAdvancedOption(SearchContext sb, String nameValCSVs,
	// String whereClauseItem, String paramName,
	// Object paramValue) {
	// Map<String, Object> paramsToAdd = new HashMap<>();
	// if (paramName != null)
	// paramsToAdd.put(paramName, paramValue);
	// processAdvancedOption(sb, nameValCSVs, whereClauseItem, paramsToAdd);
	// }

	// private void processAdvancedOption(SearchContext sb, String nameValCSVs,
	// String whereClauseItem,
	// Map<String, Object> newParams) {
	// Map<String, String> rMap = sb.searchParams.restrictions;
	//
	// String[] tokens = nameValCSVs.split(",");
	// for (String t : tokens) {
	// String[] nameVal = t.split("=");
	// if (!nameVal[1].trim().equals(rMap.get(nameVal[0].trim())))
	// return;
	// }
	//
	// sb.append(" AND (").append(whereClauseItem).append(")");
	// if (newParams != null)
	// sb.params.putAll(newParams);
	// }

	private void appendFilterWhereClauseItems(SearchContext sb) {
		VolDemoSearchParams searchParams = sb.searchParams;

		boolean includeParticipations = false; // searchParams.displayCols.contains(PARTICIPATIONS);
		boolean includeIssues = false; // searchParams.displayCols.contains(ISSUES);

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
						+ " or v.ResidentialAddress like :search" + i //
						+ " or v.ResidentialCity like :search" + i //
						+ " or v.ResidentialState like :search" + i //
						+ " or v.ResidentialZip like :search" + i //
						+ " or (case when v.EmailUserProvided is not null then v.EmailUserProvided else v.Email end) like :search"
						+ i //
						+ " or (case when v.PhoneUserProvided is not null then v.PhoneUserProvided else v.Phone end) like :search"
						+ i //
						+ (includeParticipations ? " or vp.combined_participations like :search" + i : "") //
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
			// if (colIndex == VolDemoColumn.DOB) {
			// // Birth month
			// whereClauseItems.add("DATEPART(month, v.DateOfBirth) =
			// :monthIndex");
			// sb.addParam("monthIndex", filterText);
			// } else if (colIndex == VolDemoColumn.AGE_GROUP) {
			// // Age Group
			// whereClauseItems.add("v.IsYouth = :isYouth");
			// sb.addParam("isYouth", "Adult".equalsIgnoreCase(filterText) ? "0"
			// : "1");
			// } else if (colIndex == VolDemoColumn.GENDER) {
			// // Gender
			// whereClauseItems.add("v.std_genderfk = :genderId");
			// sb.addParam("genderId", filterText);
			// } else if (colIndex == VolDemoColumn.ENTRY_DATE) {
			// String[] tokens = filterText.split("/", -1);
			// if (!"".equals(tokens[0])) {
			// whereClauseItems.add("DATEPART(month, v.EntryDate) =
			// :entryDateMonthIndex");
			// sb.addParam("entryDateMonthIndex", tokens[0]);
			// }
			// if (!"".equals(tokens[1])) {
			// whereClauseItems.add("DATEPART(year, v.EntryDate) =
			// :entryDateYearIndex");
			// sb.addParam("entryDateYearIndex", tokens[1]);
			// }
			// } else if (colIndex == VolDemoColumn.STATE) {
			// // State
			// whereClauseItems.add("st.id = :stateId");
			// sb.addParam("stateId", filterText);
			// } else if (colIndex == VolDemoColumn.STATUS_DATE) {
			// String[] tokens = filterText.split("/", -1);
			// if (!"".equals(tokens[0])) {
			// whereClauseItems.add("DATEPART(month, v.StatusDate) =
			// :statusDateMonthIndex");
			// sb.addParam("statusDateMonthIndex", tokens[0]);
			// }
			// if (!"".equals(tokens[1])) {
			// whereClauseItems.add("DATEPART(year, v.StatusDate) =
			// :statusDateYearIndex");
			// sb.addParam("statusDateYearIndex", tokens[1]);
			// }
			// } else if (colIndex == VolDemoColumn.PRIMARY_PRECINCT) {
			// if ("mine".equals(filterText)) {
			// whereClauseItems.add("ci.id = :workingPrecinctId");
			// sb.addParam("workingPrecinctId", searchParams.workingPrecinctId);
			// } else if ("others".equals(filterText)) {
			// whereClauseItems.add("(ci.id is null or ci.id <>
			// :workingPrecinctId)");
			// sb.addParam("workingPrecinctId", searchParams.workingPrecinctId);
			// }
			// }
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
				"v.VoterId" + orderDir, //
				"p.Name" + orderDir, //
				"pa.Name" + orderDir, //
				"v.AffiliatedDate" + orderDir, //
				"v.RegistrationDate" + orderDir, //
				"v.EffectiveDate" + orderDir, //
				"v.VoterStatusActive" + orderDir, //
				"v.VoterStatusReason" + orderDir, //
				"ISNULL(v.ResidentialAddress, '')" + orderDir + ", ISNULL(v.ResidentialCity, '')" + orderDir, //
				"ISNULL(v.ResidentialAddress, '')" + orderDir, //
				"ISNULL(v.ResidentialCity, '')" + orderDir, //
				"ISNULL(v.ResidentialState, '')" + orderDir, //
				"ISNULL(v.ResidentialZip, '')" + orderDir + ", ISNULL(v.ResidentialZipPlus, '')", //
				"g.Name" + orderDir, //
				"v.BirthYear" + orderDir, //
				"v.AgeApprox" + orderDir, //
				"v.MailingAddress1" + orderDir + ", v.MailingAddress2" + orderDir + ", v.MailingAddress3" + orderDir
						+ ", v.MailingCity" + orderDir + ", st.MailingState" + orderDir + ", v.MailingZip" + orderDir
						+ ", v.MailingZipPlus" + orderDir + ", v.MailingCountry" + orderDir, //
				"v.BallotAddress1" + orderDir + ", v.BallotAddress2" + orderDir + ", v.BallotAddress3" + orderDir
						+ ", v.BallotCity" + orderDir + ", st.BallotState" + orderDir + ", v.BallotZip" + orderDir
						+ ", v.BallotZipPlus" + orderDir + ", v.BallotCountry" + orderDir, //
				"ISNULL(v.PhoneUserProvided, v.Phone), v.Fax, ISNULL(v.EmailUserProvided, v.Email)", //
				"ISNULL(v.PhoneUserProvided, v.Phone)", //
				"v.Fax", //
				"ISNULL(v.EmailUserProvided, v.Email)", //
		};
		return orderByCols;
	}

	private VoterDemographics buildFromRow(Object[] row) {
		int index = 0;

		VoterDemographics vd = new VoterDemographics();
		vd.setId(((Number) row[index++]).longValue());

		vd.setLastName((String) row[index++]);
		vd.setFirstName((String) row[index++]);
		vd.setMiddleName((String) row[index++]);
		vd.setSuffix((String) row[index++]);
		vd.setNickname((String) row[index++]);
		vd.setVoterId((String) row[index++]);
		vd.setPrecinct((String) row[index++]);
		vd.setParty((String) row[index++]);
		vd.setAffiliatedDate(DateUtil.asLocalDate((Date) row[index++]));
		vd.setRegistrationDate(DateUtil.asLocalDate((Date) row[index++]));
		vd.setEffectiveDate(DateUtil.asLocalDate((Date) row[index++]));
		vd.setStatusActive((Boolean) row[index++]);
		vd.setStatusReason((String) row[index++]);

		vd.setAddress((String) row[index++]);
		vd.setCity((String) row[index++]);
		vd.setState((String) row[index++]);
		vd.setZip((String) row[index++]);
		vd.setZipPlus((String) row[index++]);

		vd.setGender((String) row[index++]);
		vd.setBirthYear(((Integer) row[index++]));
		vd.setAgeApprox(((Integer) row[index++]));

		vd.setMailingAddress1((String) row[index++]);
		vd.setMailingAddress2((String) row[index++]);
		vd.setMailingAddress3((String) row[index++]);
		vd.setMailingCity((String) row[index++]);
		vd.setMailingState((String) row[index++]);
		vd.setMailingZip((String) row[index++]);
		vd.setMailingZipPlus((String) row[index++]);
		vd.setMailingCountry((String) row[index++]);

		vd.setBallotAddress1((String) row[index++]);
		vd.setBallotAddress2((String) row[index++]);
		vd.setBallotAddress3((String) row[index++]);
		vd.setBallotCity((String) row[index++]);
		vd.setBallotState((String) row[index++]);
		vd.setBallotZip((String) row[index++]);
		vd.setBallotZipPlus((String) row[index++]);
		vd.setBallotCountry((String) row[index++]);

		vd.setPhone((String) row[index++]);
		vd.setUserProvidedPhone((String) row[index++]);
		vd.setFax((String) row[index++]);
		vd.setEmail((String) row[index++]);
		vd.setUserProvidedEmail((String) row[index++]);

		return vd;
	}

}
