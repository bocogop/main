package org.bocogop.wr.service;

import org.bocogop.shared.service.validation.ServiceValidationException;
import org.bocogop.wr.model.benefitingService.BenefitingServiceTemplate;

public interface BenefitingServiceTemplateService {

	BenefitingServiceTemplate saveOrUpdate(BenefitingServiceTemplate benefitingServiceTemplate)
			throws ServiceValidationException;

	boolean canBeDeleted(long benefitingServiceTemplateId);

	void reactivate(long benefitingServiceTemplateId);

	void deleteOrInactivateBenefitingServiceTemplate(long benefitingServiceTemplateId);

	void deleteBenefitingServiceTemplate(long benefitingServiceTemplateId);

	void inactivateBenefitingServiceTemplate(long benefitingServiceTemplateId);

}
