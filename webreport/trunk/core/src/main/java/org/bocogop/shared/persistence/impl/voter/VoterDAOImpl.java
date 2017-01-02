package org.bocogop.shared.persistence.impl.voter;

import java.time.LocalDate;
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
import org.bocogop.shared.model.precinct.Precinct;
import org.bocogop.shared.model.voter.Voter;
import org.bocogop.shared.persistence.dao.voter.VoterDAO;
import org.bocogop.shared.persistence.dao.voter.VoterQuickSearchResult;
import org.bocogop.shared.persistence.impl.GenericHibernateSortedDAOImpl;
import org.bocogop.shared.persistence.queryCustomization.QueryCustomization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

@Repository
public class VoterDAOImpl extends GenericHibernateSortedDAOImpl<Voter> implements VoterDAO {
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(VoterDAOImpl.class);

	@Value("${voterSearch.maxResults}")
	private int maxResults;

	@SuppressWarnings("unchecked")
	@Override
	public List<Voter> findByCriteria(String voterId, String firstName, String middleName, String lastName,
			boolean firstNameOrLastNameMatches, boolean useExactNameMatching, Integer birthYear, String addressStreet,
			String city, String state, String zip, String phone, String email, Collection<Long> precinctIds,
			QueryCustomization... customization) {
		boolean hasPrecinctIds = CollectionUtils.isNotEmpty(precinctIds);

		StringBuilder sb = new StringBuilder("select v from ").append(Voter.class.getName()).append(" v");

		if (hasPrecinctIds) {
			sb.append(" left join v.voterAssignments fa left join fa.precinct faf");
			sb.append(" left join v.primaryPrecinct pf left join faf.parent faf_parent");
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

		if (StringUtils.isNotBlank(voterId)) {
			whereClauseItems.add("v.voterId = :voterId");
			params.put("voterId", voterId);
		}

		if (birthYear != null) {
			whereClauseItems.add("v.birthYear = :birthYear");
			params.put("birthYear", birthYear);
		}

		if (hasPrecinctIds) {
			whereClauseItems.add("(   (pf.id is not null and pf.id in (:precinctIds))" //
					+ " or (faf is not null and TYPE(faf) = :precinctType and faf.id in (:precinctIds))" //
					+ " or (faf is not null and TYPE(faf) = :locationType and faf_parent.id in (:precinctIds)) )");
			params.put("precinctIds", precinctIds);
			params.put("precinctType", Precinct.class);
		}

		if (StringUtils.isNotBlank(addressStreet)) {
			whereClauseItems.add("v.addressLine1 like :addressStreet" + " or v.addressLine2 like :addressStreet");
			params.put("addressStreet", "%" + addressStreet + "%");
		}

		if (StringUtils.isNotBlank(city)) {
			whereClauseItems.add("v.city like :city");
			params.put("city", "%" + city + "%");
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

		List<Voter> resultList = q.setMaxResults(maxResults).getResultList();
		return resultList;
	}

	@Override
	public List<String> findZipCodesAtPrecincts(List<Long> precinctIds, Boolean voterActive,
			Boolean voterAssignmentActive) {
		String query = "select distinct SUBSTRING(v.zip, 1, 5) from " + Voter.class.getName()
				+ " v join v.voterAssignments vfa" + " where vfa.precinct.id in (:precinctIds)";
		if (voterActive != null)
			query += " and v.status.voterActive = " + voterActive;
		if (voterAssignmentActive != null)
			query += " and vfa.inactive = " + !voterAssignmentActive;
		query += " order by SUBSTRING(v.zip, 1, 5)";

		@SuppressWarnings("unchecked")
		List<String> results = query(query).setParameter("precinctIds", precinctIds).getResultList();
		return results;
	}

	@SuppressWarnings("unchecked")
	@Override
	public SortedSet<Precinct> findPrecinctsForVoter(long voterId) {
		return new TreeSet<>(query(
				// "select vfa.precinct from " + VoterAssignment.class.getName()
				// + " vfa where vfa.voter.id = :id"
				// TODO BOCOGOP
				"").setParameter("id", voterId).getResultList());
	}

	@Override
	public SortedSet<VoterQuickSearchResult> quickSearch(String searchValue, Long voterId, long precinctId,
			boolean includeAssignments, boolean includeOrganizations, boolean onlyActiveAssignmentsAndOrgs) {
		StringBuilder sb = new StringBuilder();
		sb.append("select v.id, v.identifyingCode, v.firstName, v.middleName, v.lastName, v.suffix, v.dateOfBirth");
		if (includeAssignments)
			sb.append(", va.id, bs.name, bs.subdivision, bsr.name, f.id, f.name, fp.id, fp.name, va.inactive");
		sb.append(" from ");

		// TODO BOCOGOP
		// sb.append(VoterAssignment.class.getName())
		// .append(" va join va.voter v join va.precinct f left join f.parent
		// fp");
		if (includeAssignments)
			sb.append(" join va.benefitingService bs join va.benefitingServiceRole bsr");

		Map<String, Object> params = new HashMap<>();

		sb.append(
				" where ((TYPE(f) = :precinctClass and f.id = :precinctId) or (TYPE(f) = :locationClass and fp.id = :precinctId))");
		params.put("precinctId", precinctId);
		params.put("precinctClass", Precinct.class);

		sb.append(" and v.status.voterActive = true");

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

		if (voterId != null) {
			sb.append(" and v.id = :voterId");
			params.put("voterId", voterId);
		}

		Query q = query(sb.toString());
		for (Entry<String, Object> entry : params.entrySet())
			q.setParameter(entry.getKey(), entry.getValue());

		@SuppressWarnings("unchecked")
		List<Object[]> results = q.getResultList();
		if (results.isEmpty())
			return new TreeSet<>();

		/* key = voter ID */
		Map<Long, VoterQuickSearchResult> m = new HashMap<>();
		for (Object[] result : results) {
			long id = ((Number) result[0]).longValue();
			VoterQuickSearchResult r = m
					.computeIfAbsent(id,
							k -> new VoterQuickSearchResult(id,
									String.valueOf(result[1]), Voter.getDisplayName((String) result[2],
											(String) result[3], (String) result[4], (String) result[5]),
									(LocalDate) result[6]));

		}

		SortedSet<VoterQuickSearchResult> returnResults = new TreeSet<>(m.values());
		return returnResults;
	}

}
