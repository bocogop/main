package org.bocogop.wr.persistence.dao.requirement;

import org.bocogop.wr.model.requirement.BenefitingServiceRoleRequirementAssociation;
import org.bocogop.wr.persistence.dao.CustomizableAppDAO;

public interface BenefitingServiceRoleRequirementAssociationDAO
		extends CustomizableAppDAO<BenefitingServiceRoleRequirementAssociation> {

	int bulkDeleteByCriteria(Long requirementId, Long benefitingServiceRoleId, Long benefitingServiceId, Long benefitingServiceRoleTemplateId, Long benefitingServiceTemplateId);
	
}
