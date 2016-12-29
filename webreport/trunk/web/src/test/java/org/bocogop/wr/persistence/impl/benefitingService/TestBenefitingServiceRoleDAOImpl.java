package org.bocogop.wr.persistence.impl.benefitingService;

import java.util.Arrays;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.bocogop.wr.AbstractTransactionalWebDAOTest;
import org.bocogop.wr.model.benefitingService.BenefitingServiceRole;
import org.bocogop.wr.model.benefitingService.ScopeType;
import org.bocogop.wr.persistence.dao.CustomizableAppDAO;
import org.bocogop.wr.persistence.dao.benefitingService.BenefitingServiceRoleDAO;

public class TestBenefitingServiceRoleDAOImpl extends AbstractTransactionalWebDAOTest<BenefitingServiceRole> {

	@Autowired
	protected BenefitingServiceRoleDAO dao;

	@Override
	protected CustomizableAppDAO<BenefitingServiceRole> getDAO() {
		return dao;
	}

	@Override
	protected BenefitingServiceRole getInstanceToSave() {
		BenefitingServiceRole t = new BenefitingServiceRole();
		t.setBenefitingService(benefitingServiceDAO.findSome(1).get(0));
		t.setFacility(facilityDAO.findSome(1).get(0));
		t.setName("unitTestName");
		t.setScope(ScopeType.NATIONAL);
		return t;
	}

	@Test
	public void testFindByCriteria() {
		dao.findByCriteria(null, null, false, null);
		dao.findByCriteria(null, Arrays.asList(218L), false, null);
		dao.findByCriteria(null, Arrays.asList(218L), true, null);
	}

	@Test
	public void testQuickSearch() {
		dao.quickSearch("F", 218L, 10);
	}
	
	@Override
	protected boolean testDelete() {
		return true;
	}

}
