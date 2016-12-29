package org.bocogop.wr.persistence.impl.benefitingService;

import java.util.Arrays;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.bocogop.wr.AbstractTransactionalWebDAOTest;
import org.bocogop.wr.model.benefitingService.BenefitingService;
import org.bocogop.wr.model.benefitingService.ScopeType;
import org.bocogop.wr.persistence.dao.CustomizableAppDAO;
import org.bocogop.wr.persistence.dao.benefitingService.BenefitingServiceDAO;

public class TestBenefitingServiceDAOImpl extends AbstractTransactionalWebDAOTest<BenefitingService> {

	@Autowired
	protected BenefitingServiceDAO dao;

	@Override
	protected CustomizableAppDAO<BenefitingService> getDAO() {
		return dao;
	}

	@Override
	protected BenefitingService getInstanceToSave() {
		BenefitingService t = new BenefitingService();
		t.setName("unitTestName");
		t.setScope(ScopeType.NATIONAL);
		return t;
	}
	
	@Test
	public void testBulkDeleteByCriteria() {
		dao.bulkDeleteByCriteria(1234L);
	}

	@Test
	public void testCountVolunteersForBenefitingServiceIds() {
		dao.countVolunteersForBenefitingServiceIds(Arrays.asList(1L, 2L, 3L));
	}

	@Override
	protected boolean testDelete() {
		return true;
	}

}
