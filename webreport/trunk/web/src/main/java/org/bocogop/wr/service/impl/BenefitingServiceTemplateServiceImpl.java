package org.bocogop.wr.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import org.bocogop.wr.model.benefitingService.BenefitingServiceTemplate;
import org.bocogop.wr.service.BenefitingServiceTemplateService;

@Service
public class BenefitingServiceTemplateServiceImpl extends AbstractServiceImpl
		implements BenefitingServiceTemplateService {
	private static final Logger log = LoggerFactory.getLogger(BenefitingServiceTemplateServiceImpl.class);

	public BenefitingServiceTemplate saveOrUpdate(BenefitingServiceTemplate benefitingServiceTemplate) {
		benefitingServiceTemplateDAO.detach(benefitingServiceTemplate);

		boolean inactivating = false;
		boolean activating = false;
		if (benefitingServiceTemplate.isPersistent()) {
			BenefitingServiceTemplate existingServiceTemplate = benefitingServiceTemplateDAO
					.findRequiredByPrimaryKey(benefitingServiceTemplate.getId());
			inactivating = !existingServiceTemplate.isInactive() && benefitingServiceTemplate.isInactive();
			activating = existingServiceTemplate.isInactive() && !benefitingServiceTemplate.isInactive();
		}

		benefitingServiceTemplate.setName(StringUtils.trim(benefitingServiceTemplate.getName()));

		benefitingServiceTemplate = benefitingServiceTemplateDAO.saveOrUpdate(benefitingServiceTemplate);

		if (inactivating) {
			cascadeInactivation(benefitingServiceTemplate.getId());
		} else if (activating) {
			cascadeActivation(benefitingServiceTemplate.getId());
		}

		benefitingServiceDAO.bulkUpdateByCriteria(benefitingServiceTemplate.getId(), null, true,
				benefitingServiceTemplate.getName(), true, benefitingServiceTemplate.getAbbreviation(), true,
				benefitingServiceTemplate.getSubdivision(), null, benefitingServiceTemplate.isGamesRelated());
		benefitingServiceTemplateDAO.flushAndRefresh(benefitingServiceTemplate);

		return benefitingServiceTemplate;
	}

	public boolean canBeDeleted(long benefitingServiceTemplateId) {
		return !workEntryDAO.existsForCriteria(benefitingServiceTemplateId, null, null, null)
				&& !occasionalWorkEntryDAO.existsForCriteria(benefitingServiceTemplateId, null, null, null);
	}

	public void deleteOrInactivateBenefitingServiceTemplate(long benefitingServiceTemplateId) {
		if (canBeDeleted(benefitingServiceTemplateId)) {
			deleteBenefitingServiceTemplateInternal(benefitingServiceTemplateId, false);
		} else {
			inactivateBenefitingServiceTemplate(benefitingServiceTemplateId);
		}
	}

	public void deleteBenefitingServiceTemplate(long benefitingServiceTemplateId) {
		deleteBenefitingServiceTemplateInternal(benefitingServiceTemplateId, true);
	}

	private void deleteBenefitingServiceTemplateInternal(long benefitingServiceTemplateId, boolean check) {
		if (!check || canBeDeleted(benefitingServiceTemplateId)) {
			volunteerAssignmentDAO.bulkDeleteByCriteria(benefitingServiceTemplateId, null, null, null);
			benefitingServiceRoleRequirementAssociationDAO.bulkDeleteByCriteria(null, null, null, null,
					benefitingServiceTemplateId);
			benefitingServiceRoleTemplateRequirementAssociationDAO.bulkDeleteByCriteria(null, null,
					benefitingServiceTemplateId);
			benefitingServiceRoleDAO.bulkDeleteByCriteria(benefitingServiceTemplateId, null, null);
			benefitingServiceDAO.bulkDeleteByCriteria(benefitingServiceTemplateId);
			benefitingServiceRoleTemplateDAO.bulkDeleteByCriteria(benefitingServiceTemplateId);
			benefitingServiceTemplateDAO.delete(benefitingServiceTemplateId);
		}
	}

	public void inactivateBenefitingServiceTemplate(long benefitingServiceTemplateId) {
		BenefitingServiceTemplate benefitingServiceTemplate = benefitingServiceTemplateDAO
				.findRequiredByPrimaryKey(benefitingServiceTemplateId);
		benefitingServiceTemplate.setInactive(true);
		benefitingServiceTemplate = benefitingServiceTemplateDAO.saveOrUpdate(benefitingServiceTemplate);
		cascadeInactivation(benefitingServiceTemplateId);
	}

	public void cascadeInactivation(long benefitingServiceTemplateId) {
		volunteerAssignmentDAO.bulkInactivateByCriteria(benefitingServiceTemplateId, null, null, null, null);
		benefitingServiceRoleDAO.bulkUpdateByCriteria(benefitingServiceTemplateId, null, null, null, null, null, false,
				null);
		benefitingServiceDAO.bulkUpdateByCriteria(benefitingServiceTemplateId, null, false, null, false, null, false,
				null, false, null);
		benefitingServiceRoleTemplateDAO.bulkUpdateByCriteria(benefitingServiceTemplateId, null, false);
	}

	public void cascadeActivation(long benefitingServiceTemplateId) {
		benefitingServiceRoleTemplateDAO.bulkUpdateByCriteria(benefitingServiceTemplateId, true, true);
	}

	@Override
	public void reactivate(long benefitingServiceTemplateId) {
		BenefitingServiceTemplate existingServiceTemplate = benefitingServiceTemplateDAO
				.findRequiredByPrimaryKey(benefitingServiceTemplateId);
		boolean wasInactive = existingServiceTemplate.isInactive();
		if (wasInactive) {
			existingServiceTemplate.setInactive(false);
			existingServiceTemplate = benefitingServiceTemplateDAO.saveOrUpdate(existingServiceTemplate);
			cascadeActivation(benefitingServiceTemplateId);
		}
	}

}
