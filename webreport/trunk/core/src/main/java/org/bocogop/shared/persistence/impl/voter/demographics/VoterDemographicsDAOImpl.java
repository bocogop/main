package org.bocogop.shared.persistence.impl.voter.demographics;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.persistence.Query;

import org.apache.commons.lang3.StringUtils;
import org.bocogop.shared.model.voter.VoterDemographics;
import org.bocogop.shared.persistence.dao.voter.demographics.VoterDemographicsColumn;
import org.bocogop.shared.persistence.dao.voter.demographics.VoterDemographicsDAO;
import org.bocogop.shared.persistence.dao.voter.demographics.VoterDemographicsSearchParams;
import org.bocogop.shared.persistence.impl.AbstractAppDAOImpl;
import org.bocogop.shared.util.DateUtil;
import org.bocogop.shared.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

@Repository
public class VoterDemographicsDAOImpl extends AbstractAppDAOImpl<VoterDemographics> implements VoterDemographicsDAO {
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(VoterDemographicsDAOImpl.class);

	static class SearchContext {
		VoterDemographicsSearchParams searchParams;
		Map<String, Object> params = new HashMap<>();
		StringBuilder sb = new StringBuilder();

		public SearchContext(VoterDemographicsSearchParams searchParams) {
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
	public List<VoterDemographics> findDemographics(VoterDemographicsSearchParams searchParams, int start, int length,
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
	public int[] findDemographicsTotalAndFilteredNumber(VoterDemographicsSearchParams searchParams, long appUserId) {
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
		VoterDemographicsSearchParams searchParams = sb.searchParams;

		// ------------ Affiliated Date options

		processAdvancedOption(sb, "affiliatedDateOptions=within30Days", "v.AffiliatedDate >= :affiliatedDateAfter",
				"affiliatedDateAfter", LocalDate.now().minusDays(30));
		processAdvancedOption(sb, "affiliatedDateOptions=within60Days", "v.AffiliatedDate >= :affiliatedDateAfter",
				"affiliatedDateAfter", LocalDate.now().minusDays(60));
		processAdvancedOption(sb, "affiliatedDateOptions=within90Days", "v.AffiliatedDate >= :affiliatedDateAfter",
				"affiliatedDateAfter", LocalDate.now().minusDays(90));
		processAdvancedOption(sb, "affiliatedDateOptions=withinYear", "v.AffiliatedDate >= :affiliatedDateAfter",
				"affiliatedDateAfter", LocalDate.of(LocalDate.now().getYear(), 1, 1));

		String affiliatedDateBefore = searchParams.restrictions.get("affiliatedDateBefore");
		if (StringUtils.isNotBlank(affiliatedDateBefore))
			processAdvancedOption(sb, "affiliatedDateOptions=before", "v.AffiliatedDate <= :affiliatedDateBefore",
					"affiliatedDateBefore", LocalDate.parse(affiliatedDateBefore, DateUtil.TWO_DIGIT_DATE_ONLY_FORMAT));

		String affiliatedDateAfter = searchParams.restrictions.get("affiliatedDateAfter");
		if (StringUtils.isNotBlank(affiliatedDateAfter))
			processAdvancedOption(sb, "affiliatedDateOptions=after", "v.AffiliatedDate >= :affiliatedDateAfter",
					"affiliatedDateAfter", LocalDate.parse(affiliatedDateAfter, DateUtil.TWO_DIGIT_DATE_ONLY_FORMAT));

		String affiliatedDateBetweenStart = searchParams.restrictions.get("affiliatedDateBetweenStart");
		String affiliatedDateBetweenEnd = searchParams.restrictions.get("affiliatedDateBetweenEnd");
		if (StringUtils.isNotBlank(affiliatedDateBetweenStart) && StringUtils.isNotBlank(affiliatedDateBetweenEnd)) {
			Map<String, Object> newParams = new HashMap<>();
			newParams.put("affiliatedDateAfter",
					LocalDate.parse(affiliatedDateBetweenStart, DateUtil.TWO_DIGIT_DATE_ONLY_FORMAT));
			newParams.put("affiliatedDateBefore",
					LocalDate.parse(affiliatedDateBetweenEnd, DateUtil.TWO_DIGIT_DATE_ONLY_FORMAT));
			processAdvancedOption(sb, "affiliatedDateOptions=between",
					"v.AffiliatedDate >= :affiliatedDateAfter and v.AffiliatedDate <= :affiliatedDateBefore",
					newParams);
		}
	}

	// private void processAdvancedOption(SearchContext sb, String nameValCSVs,
	// String whereClauseItem) {
	// processAdvancedOption(sb, nameValCSVs, whereClauseItem, null);
	// }

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
		VoterDemographicsSearchParams searchParams = sb.searchParams;

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

		for (Entry<VoterDemographicsColumn, String> entry : searchParams.filters.entrySet()) {
			VoterDemographicsColumn colIndex = entry.getKey();
			String filterText = entry.getValue();
			/*
			 * all the where clause table references below have to exist in both
			 * the main query and the totalAndFilteredNumber query - CPB
			 */

			if (colIndex == VoterDemographicsColumn.NAME) {
				String[] nameComponents = StringUtil.parseNameComponents(filterText);
				if (StringUtils.isNotBlank(nameComponents[0])) {
					whereClauseItems.add("v.LastName like :lastNameFilter");
					sb.addParam("lastNameFilter", nameComponents[0] + "%");
				}
				if (StringUtils.isNotBlank(nameComponents[1])) {
					whereClauseItems.add("v.FirstName like :firstNameFilter");
					sb.addParam("firstNameFilter", nameComponents[1] + "%");
				}
				if (StringUtils.isNotBlank(nameComponents[2])) {
					whereClauseItems.add("v.MiddleName like :middleNameFilter");
					sb.addParam("middleNameFilter", nameComponents[2] + "%");
				}
			} else if (colIndex == VoterDemographicsColumn.VOTER_ID) {
				whereClauseItems.add("v.VoterId = :voterIdFilter");
				sb.addParam("voterIdFilter", filterText);
			} else if (colIndex == VoterDemographicsColumn.PRECINCT) {
				whereClauseItems.add("v.PrecinctFK = :precinctIdFilter");
				sb.addParam("precinctIdFilter", filterText);
			} else if (colIndex == VoterDemographicsColumn.PARTY) {
				whereClauseItems.add("v.PartyFK = :partyIdFilter");
				sb.addParam("partyIdFilter", filterText);
			} else if (colIndex == VoterDemographicsColumn.STATUS) {
				whereClauseItems.add("v.VoterStatusActive = :statusActiveFilter");
				sb.addParam("statusActiveFilter", "Active".equals(filterText) ? "1" : "0");
			} else if (colIndex == VoterDemographicsColumn.GENDER) {
				whereClauseItems.add("v.GenderFK = :genderIdFilter");
				sb.addParam("genderIdFilter", filterText);
			} else if (colIndex == VoterDemographicsColumn.FULL_ADDRESS) {
				whereClauseItems.add("(v.ResidentialAddress like :fullAddressFilter" //
						+ " or v.ResidentialCity like :fullAddressFilter" //
						+ " or v.ResidentialState like :fullAddressFilter" //
						+ " or (case when v.ResidentialZipPlus is not null then" //
						+ "		CONCAT(v.ResidentialZip, '-', v.ResidentialZipPlus) else v.ResidentialZip end) like :fullAddressFilter)" //
				);
				sb.addParam("fullAddressFilter", "%" + filterText + "%");
			} else if (colIndex == VoterDemographicsColumn.CITY) {
				whereClauseItems.add("v.ResidentialCity = :cityFilter");
				sb.addParam("cityFilter", filterText);
			} else if (colIndex == VoterDemographicsColumn.STATE) {
				whereClauseItems.add("v.ResidentialState = :stateCode");
				sb.addParam("stateCode", filterText);
			} else if (colIndex == VoterDemographicsColumn.ZIP) {
				whereClauseItems.add("(case when v.ResidentialZipPlus is not null then" //
						+ "		CONCAT(v.ResidentialZip, '-', v.ResidentialZipPlus) else v.ResidentialZip end) = :zipFilter");
				sb.addParam("zipFilter", filterText);
			}
		}

		if (!whereClauseItems.isEmpty())
			// requires that multi-expression criteria be surrounded earlier
			// with parens - CPB
			sb.append(" AND ").append(StringUtils.join(whereClauseItems, " AND "));
	}

	private String[] getOrderByCols(boolean asc) {
		String orderDir = asc ? "" : " desc";
		String orderDirOpposite = asc ? " desc" : " ";

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
				"v.VoterStatusActive" + orderDirOpposite, //
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
				"ISNULL(v.PhoneUserProvided, v.Phone), v.Fax, ISNULL(v.EmailUserProvided, v.Email)" + orderDir, //
				"ISNULL(v.PhoneUserProvided, v.Phone)" + orderDir, //
				"v.Fax" + orderDir, //
				"ISNULL(v.EmailUserProvided, v.Email)" + orderDir, //
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
