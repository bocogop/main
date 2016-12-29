package org.bocogop.wr.persistence.dao.organization;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;

import org.bocogop.wr.model.organization.AbstractBasicOrganization;
import org.bocogop.wr.model.organization.BasicOrganization;
import org.bocogop.wr.model.organization.Organization;
import org.bocogop.wr.model.organization.OrganizationBranch;
import org.bocogop.wr.persistence.dao.CustomizableSortedDAO;
import org.bocogop.wr.persistence.queryCustomization.QueryCustomization;

public interface OrganizationDAO extends CustomizableSortedDAO<AbstractBasicOrganization> {

	List<AbstractBasicOrganization> findByCriteria(String name, boolean includeNational, boolean includeLocalFacility,
			boolean includeBranches, List<Long> localFacilityIds, Boolean activeStatus, String abbreviation,
			Boolean onNationalAdvisoryCommittee, Boolean inactiveOrgOnly, QueryCustomization... customization);

	SortedSet<OrgQuickSearchResult> quickSearch(String searchValue, Long facilityIdRestriction, Integer maxResults);

	Map<Long, SortedSet<OrgQuickSearchResult>> quickSearchForTimePosting(Collection<Long> volunteerIds,
			boolean onlyActive, Long facilityId);

	List<OrganizationBranch> getLocalBranchesForOrgId(Long orgId, Long facilityId, String name);

	public Organization getOrganizationByName(String name, boolean checkNationaLevel, Long facilityId);

}
