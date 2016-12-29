package org.bocogop.wr.web.conversion;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import org.bocogop.wr.model.benefitingService.BenefitingServiceRoleType;
import org.bocogop.wr.persistence.dao.benefitingService.BenefitingServiceRoleTypeDAO;

@Component
public class BenefitingServiceRoleTypeConverter extends AbstractStringToPersistentConverter<BenefitingServiceRoleType> {

	@Autowired
	protected BenefitingServiceRoleTypeConverter(BenefitingServiceRoleTypeDAO dao) {
		super(dao);
	}
}
