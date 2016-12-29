package org.bocogop.wr.persistence.impl.benefitingService;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.bocogop.wr.AbstractTransactionalWebDAOTest;
import org.bocogop.wr.model.benefitingService.BenefitingServiceTemplate;
import org.bocogop.wr.persistence.dao.CustomizableAppDAO;
import org.bocogop.wr.persistence.dao.benefitingService.BenefitingServiceTemplateDAO;

public class TestBenefitingServiceTemplateDAOImpl extends AbstractTransactionalWebDAOTest<BenefitingServiceTemplate> {

	@Autowired
	protected BenefitingServiceTemplateDAO dao;

	@Override
	protected CustomizableAppDAO<BenefitingServiceTemplate> getDAO() {
		return dao;
	}

	@Override
	protected BenefitingServiceTemplate getInstanceToSave() {
		BenefitingServiceTemplate t = new BenefitingServiceTemplate();
		t.setName("unitTestName");
		return t;
	}

	@Test
	public void testGetUnusedBenefitingServiceAndRoleMap() {
		dao.getAssignableBenefitingServiceAndRoleTemplates(218L, null, false, false);
	}

	@Override
	protected boolean testDelete() {
		return true;
	}

}
