package org.bocogop.wr.persistence.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.persistence.Query;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import org.bocogop.wr.model.organization.AbstractBasicOrganization;
import org.bocogop.wr.model.organization.Organization;
import org.bocogop.wr.model.organization.OrganizationBranch;
import org.bocogop.wr.model.organization.ScopeType;
import org.bocogop.wr.model.volunteer.Volunteer;
import org.bocogop.wr.persistence.dao.organization.OrgQuickSearchResult;
import org.bocogop.wr.persistence.dao.organization.OrganizationDAO;
import org.bocogop.wr.persistence.queryCustomization.QueryCustomization;

@Repository
public class OrganizationDAOImpl extends GenericHibernateSortedDAOImpl<AbstractBasicOrganization>
		implements OrganizationDAO {
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(OrganizationDAOImpl.class);

	@SuppressWarnings("unchecked")
	@Override
	public List<AbstractBasicOrganization> findByCriteria(String name, boolean includeNational, boolean includeLocalFacility, boolean includeBranches, 
			List<Long> localFacilityIds, Boolean activeStatus, String abbreviation, Boolean onNationalAdvisoryCommittee, Boolean includeinactiveOrgs, 
			QueryCustomization... customization) {
		
		if (!includeLocalFacility && !includeNational)
			return new ArrayList<>();

		List<String> whereClauseItems = new ArrayList<>();
		Map<String, Object> params = new HashMap<>();

		String[] nameTerms = new String[0];
		boolean specifiedName = StringUtils.isNotBlank(name);
		if (specifiedName) {
			nameTerms = name.split("\\s+");
			for (int i = 0; i < nameTerms.length; i++) {
				params.put("nameComp" + i, "%" + nameTerms[i].toLowerCase() + "%");
			}
		}

		StringBuilder sb = new StringBuilder("select o from ") //
				.append(AbstractBasicOrganization.class.getName()).append(" o left join fetch o.organization parent") //
				.append(" left join o.facility facility");

		StringBuilder orgFrag = new StringBuilder("TYPE(o) = :organizationClass");
		params.put("organizationClass", Organization.class);

		if (specifiedName) {
			for (int i = 0; i < nameTerms.length; i++)
				orgFrag.append(" and LOWER(o.name) like :nameComp" + i);
		}

		if (onNationalAdvisoryCommittee != null) {
			orgFrag.append(" and o.onNationalAdvisoryCommittee = :onNationalAdvisoryCommittee");
			params.put("onNationalAdvisoryCommittee", onNationalAdvisoryCommittee);
		}
		
			
		StringBuilder branchFrag = new StringBuilder();
		if (includeBranches) {
			branchFrag.append("TYPE(o) = :branchClass");
			params.put("branchClass", OrganizationBranch.class);
			
			if (specifiedName) {
				for (int i = 0; i < nameTerms.length; i++)
					branchFrag.append(" and CONCAT(parent.name, '-', o.name) like :nameComp" + i);
			}
		}

		QueryCustomization cust = ArrayUtils.isEmpty(customization) ? new QueryCustomization() : customization[0];
		cust.appendRemainingJoins(sb, "o");

		whereClauseItems.add("(" + orgFrag + ")" + (includeBranches ? " or (" + branchFrag + ")" : ""));

		List<ScopeType> validScopeTypes = new ArrayList<>();
		if (includeLocalFacility) {
			validScopeTypes.add(ScopeType.LOCAL);
			if (localFacilityIds != null) {
				whereClauseItems.add("o.scope <> :localScope or COALESCE(facility.id, -1) in ( :localFacilityIds )");
				params.put("localFacilityIds", localFacilityIds);
				params.put("localScope", ScopeType.LOCAL);
			}
		}

		if (includeNational)
			validScopeTypes.add(ScopeType.NATIONAL);

		if (activeStatus != null && (includeinactiveOrgs == null || includeinactiveOrgs == false)) {
			whereClauseItems.add("o.inactive = :inactiveStatus");
			params.put("inactiveStatus", !activeStatus);
		}
		
		if (StringUtils.isNotBlank(abbreviation)) {
			whereClauseItems.add("LOWER(o.abbreviation) like :abbreviation");
			params.put("abbreviation", "%" + abbreviation.toLowerCase().trim() + "%");
		}

		whereClauseItems.add("o.scope in (:validScopes)");
		params.put("validScopes", validScopeTypes);

		if (cust.getOrderBy() == null)
			cust.setOrderBy("o.scope desc, " //
					+ "case when TYPE(o) = :organizationClass then o.name else parent.name end," //
					+ "case when TYPE(o) = :organizationClass then '1' else '2' end," //
					+ "o.name");

		Query q = constructQuery(em, sb, whereClauseItems, params, null, cust);
		if (cust.getRowLimitation() != null)
			q.setMaxResults(cust.getRowLimitation());

		return q.getResultList();
	}

	@Override
	public SortedSet<OrgQuickSearchResult> quickSearch(String searchValue, Long facilityIdRestriction,
			Integer maxResults) {
		StringBuilder sb = new StringBuilder();
		sb.append("select t.id, t.scope, t.name, t.abbreviation, i.name, t.inactive, parent.name from ");
		sb.append(AbstractBasicOrganization.class.getName());
		sb.append(" t left join t.facility i left join t.organization parent where 1=1");

		Map<String, Object> params = new HashMap<>();

		if (StringUtils.isNotBlank(searchValue)) {
			String[] tokens = searchValue.split("\\W");
			for (int i = 0; i < tokens.length; i++) {
				String token = tokens[i];
				if (StringUtils.isBlank(token))
					continue;

				sb.append(" and (lower(t.name) like :text").append(i);
				sb.append(" or lower(i.name) like :text").append(i).append(")");
				params.put("text" + i, "%" + token.toLowerCase() + "%");
			}
		}

		if (facilityIdRestriction != null) {
			sb.append(" and (i.name is null or i.id = :facilityId)");
			params.put("facilityId", facilityIdRestriction);
		}

		Query q = query(sb.toString());
		for (Entry<String, Object> entry : params.entrySet())
			q.setParameter(entry.getKey(), entry.getValue());

		if (maxResults != null && maxResults > 0)
			q.setMaxResults(maxResults);

		@SuppressWarnings("unchecked")
		List<Object[]> results = q.getResultList();

		SortedSet<OrgQuickSearchResult> returnResults = new TreeSet<>();
		for (Object[] result : results) {
			String name = (String) result[2];
			String parentName = (String) result[6];
			if (parentName != null)
				name = parentName + " - " + name;

			returnResults.add(new OrgQuickSearchResult(((Number) result[0]).longValue(), (ScopeType) result[1], name,
					(String) result[3], (String) result[4], !((Boolean) result[5]), null));
		}
		return returnResults;
	}

	public Map<Long, SortedSet<OrgQuickSearchResult>> quickSearchForTimePosting(Collection<Long> volunteerIds,
			boolean onlyActive, Long facilityId) {
		Map<Long, SortedSet<OrgQuickSearchResult>> results = new HashMap<>();

		StringBuilder sb = new StringBuilder(
				"select v.id, o.id, o.scope, o.name, o.abbreviation, f.name, p.name, o.inactive, vo.inactive") //
						.append(" from ").append(Volunteer.class.getName()).append(" v") //
						.append(" join v.volunteerOrganizations vo") //
						.append(" join vo.organization o") //
						.append(" left join o.organization p") //
						.append(" left join o.facility f") //
						.append(" where 1=1") //
						.append(" and v.id in (:volunteerIds)") //
						.append(" and (f is null or f.id = :facilityId)");
		;

		if (onlyActive) {
			sb.append(" and vo.inactive = false");
		}

		sb.append(" and o.inactive = false and (p is null or p.inactive = false)");

		Query q = query(sb.toString()) //
				.setParameter("volunteerIds", volunteerIds) //
				.setParameter("facilityId", facilityId);

		@SuppressWarnings("unchecked")
		List<Object[]> r = q.getResultList();

		for (Object[] result : r) {
			long id = ((Number) result[0]).longValue();
			SortedSet<OrgQuickSearchResult> orgs = results.computeIfAbsent(id,
					k -> new TreeSet<OrgQuickSearchResult>());
			String childName = (String) result[3];
			String parentName = (String) result[6];
			String displayName = (parentName == null) ? childName : parentName + " - " + childName;
			orgs.add(new OrgQuickSearchResult(((Number) result[1]).longValue(), (ScopeType) result[2], displayName,
					(String) result[4], (String) result[5], !((Boolean) result[7]), !((Boolean) result[8])));
		}

		return results;
	}

	@Override
	public List<OrganizationBranch> getLocalBranchesForOrgId(Long orgId, Long facilityId, String name) {

		if (orgId == null) {
			throw new IllegalArgumentException("Organization Id cannot be null");
		}

		StringBuilder sb = new StringBuilder("select b from ").append(OrganizationBranch.class.getName()).append(" b");

		QueryCustomization cust = new QueryCustomization();

		List<String> whereClauseItems = new ArrayList<>();
		Map<String, Object> params = new HashMap<>();

		whereClauseItems.add("b.organization.id = :orgId");
		params.put("orgId", orgId);

		if (facilityId != null) {
			whereClauseItems.add("b.facility.id = :facilityId");
			params.put("facilityId", facilityId);
		}

		if (name != null) {
			whereClauseItems.add("b.name = :name");
			params.put("name", name);
		}

		Query q = constructQuery(em, sb, whereClauseItems, params, null, cust);

		if (cust.getRowLimitation() != null)
			q.setMaxResults(cust.getRowLimitation());

		@SuppressWarnings("unchecked")
		List<OrganizationBranch> resultList = q.getResultList();
		return resultList;

	}

	@Override
	public Organization getOrganizationByName(String name, boolean checkNationaLevel, Long facilityId) {

		StringBuilder sb = new StringBuilder("select o from ").append(Organization.class.getName()).append(" o");

		QueryCustomization cust = new QueryCustomization();

		List<String> whereClauseItems = new ArrayList<>();
		Map<String, Object> params = new HashMap<>();

		if (name != null) {
			whereClauseItems.add("o.name = :name");
			params.put("name", name);
		}
		if (checkNationaLevel) {
			whereClauseItems.add("o.scope = :scope");
			params.put("scope", ScopeType.NATIONAL);
		} else {

			sb.append(" left join o.facility facility");
			if (facilityId != null) {
				whereClauseItems.add("facility.id = :facilityId");
				params.put("facilityId", facilityId);
				whereClauseItems.add("o.scope = :scope");
				params.put("scope", ScopeType.LOCAL);
			}

		}

		Query q = constructQuery(em, sb, whereClauseItems, params, null, cust);

		if (cust.getRowLimitation() != null)
			q.setMaxResults(cust.getRowLimitation());

		@SuppressWarnings("unchecked")
		List<Organization> resultList = q.getResultList();
		return resultList.size() > 0 ? resultList.get(0) : null;

	}

}
