package org.bocogop.wr.persistence;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import org.bocogop.wr.AbstractTransactionalWebDAOTest;
import org.bocogop.wr.model.organization.AbstractBasicOrganization;
import org.bocogop.wr.model.organization.BasicOrganization;
import org.bocogop.wr.model.organization.Organization;
import org.bocogop.wr.model.organization.ScopeType;
import org.bocogop.wr.persistence.dao.CustomizableAppDAO;

public class TestOrganizationDAO extends AbstractTransactionalWebDAOTest<AbstractBasicOrganization> {

	@Override
	protected CustomizableAppDAO<AbstractBasicOrganization> getDAO() {
		return organizationDAO;
	}

	@Override
	protected Organization getInstanceToSave() {
		Organization o = new Organization();
		o.setName("UnitTestOrganization");
		o.setFullName(o.getName());
		o.setScope(ScopeType.LOCAL);
		return o;
	}

	@Override
	protected boolean testDelete() {
		return true;
	}

	@Test
	public void testFindByCriteria() {
		List<Long> staList1 = new ArrayList<Long>();
		staList1.add(1234L);
		organizationDAO.findByCriteria("test", false, false, true, staList1, null, null, null, null);
		organizationDAO.findByCriteria("test", true, false, true, staList1, null, null, null, null);
		organizationDAO.findByCriteria("test", false, true, true, staList1, null, null, null, null);
		organizationDAO.findByCriteria("test", true, true, true, staList1, null, null, null, null);

		List<Long> staList2 = new ArrayList<Long>();
		staList2.add(1000098L);
		List<AbstractBasicOrganization> resultsFor442 = organizationDAO.findByCriteria(null, true, true, true, null, null, null, null, null);
		Assert.assertFalse("Failed - 442 didn't return any local or national results", resultsFor442.isEmpty());
		resultsFor442 = organizationDAO.findByCriteria(null, false, true, true, null, null, null, null, null);
		Assert.assertFalse("Failed - 442 didn't return any local results", resultsFor442.isEmpty());
	}

}
