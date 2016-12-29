package org.bocogop.wr.persistence;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.Test;

import org.bocogop.wr.AbstractTransactionalWebDAOTest;
import org.bocogop.wr.model.expenditure.Expenditure;
import org.bocogop.wr.persistence.dao.CustomizableAppDAO;

public class TestExpenditureDAO extends AbstractTransactionalWebDAOTest<Expenditure> {

	@Override
	protected CustomizableAppDAO<Expenditure> getDAO() {
		return expenditureDAO;
	}

	@Override
	protected Expenditure getInstanceToSave() {
		Expenditure e = new Expenditure();
		e.setAmount(new BigDecimal("100.05"));
		e.setOriginator(user);
		e.setDescription("Some Item");
		e.setFacility(getFacility());
		e.setRequestDate(LocalDate.now());
		e.setDonGenPostFund(donGenPostFundDAO.findSome(1).get(0));
		return e;
	}

	@Override
	protected boolean testDelete() {
		return true;
	}

	@Test
	public void testFindByCriteria() {
		expenditureDAO.findByCriteria(getFacility().getId(), donGenPostFundDAO.findSome(1).get(0).getId(),
				LocalDate.of(2015, 1, 1), LocalDate.of(2016, 1, 1));
	}

}
