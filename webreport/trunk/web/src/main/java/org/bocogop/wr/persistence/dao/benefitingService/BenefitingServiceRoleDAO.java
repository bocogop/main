package org.bocogop.wr.persistence.dao.benefitingService;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;

import org.bocogop.wr.model.benefitingService.BenefitingServiceRole;
import org.bocogop.wr.model.benefitingService.BenefitingServiceRoleType;
import org.bocogop.wr.persistence.dao.CustomizableSortedDAO;
import org.bocogop.wr.persistence.queryCustomization.QueryCustomization;

/**
 * @author Connor
 *
 */
public interface BenefitingServiceRoleDAO extends CustomizableSortedDAO<BenefitingServiceRole> {

	List<BenefitingServiceRole> findByCriteria(String name, Collection<Long> facilityIds,
			boolean includeLocationsUnderSpecifiedFacilities, Boolean activeStatus,
			QueryCustomization... customization);

	Map<Long, Integer[]> countVolunteersForBenefitingServiceRoleIds(List<Long> allRoleIds);

	int bulkUpdateByCriteria(Long benefitingServiceTemplateId, Long benefitingServiceRoleTemplateId,
			Long benefitingServiceId, Long facilityOrLocationId, Boolean roleIsRequiredAndReadOnly, String name,
			Boolean activeStatus, BenefitingServiceRoleType roleType);

	int bulkDeleteByCriteria(Long benefitingServiceTemplateId, Long benefitingServiceRoleTemplateId,
			Long benefitingServiceId);

	/**
	 * Quickly searches for BenefitingServiceRoles
	 * 
	 * @param searchValue
	 *            The name to filter - this can match any part of the
	 *            BenefitingService or BenefitingServiceRole name
	 * @param facilityIdRestriction
	 *            If specified, results will be scoped to either the facility
	 *            with this ID, or to any location underneath the facility with
	 *            this ID
	 * @param length
	 *            The maximum number of results to return
	 * @return The search results. The locationName field in the returned
	 *         objects will be null if the item is scoped to the main facility
	 *         itself.
	 */
	SortedSet<BenefitingServiceRoleQuickSearchResult> quickSearch(String searchValue, Long facilityIdRestriction,
			Integer length);

}
