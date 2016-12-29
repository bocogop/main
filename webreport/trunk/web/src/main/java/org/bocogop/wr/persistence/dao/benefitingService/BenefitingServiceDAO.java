package org.bocogop.wr.persistence.dao.benefitingService;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.bocogop.wr.model.benefitingService.BenefitingService;
import org.bocogop.wr.persistence.dao.CustomizableSortedDAO;
import org.bocogop.wr.persistence.queryCustomization.QueryCustomization;

public interface BenefitingServiceDAO extends CustomizableSortedDAO<BenefitingService> {

	List<BenefitingService> findByCriteria(String name, String subdivision, String abbreviation,
			Collection<Long> facilityIds, Boolean templateIsNull, Boolean gamesRelated, Boolean activeStatus,
			Boolean includeInactive,  QueryCustomization... customization);
	
	Map<Long, Integer[]> countVolunteersForBenefitingServiceIds(List<Long> benefitingServiceIds);

	int bulkUpdateByCriteria(Long benefitingServiceTemplateId, Long facilityOrLocationId, boolean updateName,
			String name, boolean updateAbbreviation, String abbreviation, boolean updateSubdivision, String subdivision,
			Boolean activeStatus, Boolean gamesRelated);

	int bulkDeleteByCriteria(Long benefitingServiceTemplateId);

	Map<Long, Integer> countOccasionalHoursForBenefitingServiceIds(List<Long> benefitingServiceIds);

	Map<Long, Integer> countOccasionalHoursForBenefitingServiceRoleIds(List<Long> benefitingServiceRoleIds);

}
