package org.bocogop.wr.persistence.dao.requirement;

import org.bocogop.wr.model.requirement.BenefitingServiceRoleTemplateRequirementAssociation;
import org.bocogop.wr.persistence.dao.CustomizableAppDAO;

public interface BenefitingServiceRoleTemplateRequirementAssociationDAO
		extends CustomizableAppDAO<BenefitingServiceRoleTemplateRequirementAssociation> {

	int bulkDeleteByCriteria(Long requirementId, Long benefitingServiceRoleTemplateId, Long benefitingServiceTemplateId);
	
}
