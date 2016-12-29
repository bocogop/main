package org.bocogop.wr.persistence.dao.benefitingService;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.bocogop.wr.model.benefitingService.BenefitingServiceRoleTemplate;
import org.bocogop.wr.persistence.dao.CustomizableSortedDAO;
import org.bocogop.wr.persistence.queryCustomization.QueryCustomization;

public interface BenefitingServiceRoleTemplateDAO extends CustomizableSortedDAO<BenefitingServiceRoleTemplate> {

	List<BenefitingServiceRoleTemplate> findByCriteria(String name, Boolean activeStatus,
			QueryCustomization... customization);

	Map<Long, Integer[]> countVolunteersForBenefitingServiceRoleTemplateIds(Collection<Long> allRoleTemplateIds);

	int bulkUpdateByCriteria(Long benefitingServiceTemplateId, Boolean roleIsRequiredAndReadOnly, Boolean activeStatus);
	
	int bulkDeleteByCriteria(Long benefitingServiceTemplateId);
	
}
