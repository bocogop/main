package org.bocogop.wr.persistence;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.Test;

import org.bocogop.wr.AbstractTransactionalWebDAOTest;
import org.bocogop.wr.model.expenditure.LedgerAdjustment;
import org.bocogop.wr.persistence.dao.CustomizableAppDAO;

public class TestLedgerAdjustmentDAO extends AbstractTransactionalWebDAOTest<LedgerAdjustment> {

	@Override
	protected CustomizableAppDAO<LedgerAdjustment> getDAO() {
		return ledgerAdjustmentDAO;
	}

	@Override
	protected LedgerAdjustment getInstanceToSave() {
		LedgerAdjustment e = new LedgerAdjustment();
		e.setAmount(new BigDecimal("100.05"));
		e.setOriginator(user);
		e.setJustification("Some Item");
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
		ledgerAdjustmentDAO.findByCriteria(getFacility().getId(), donGenPostFundDAO.findSome(1).get(0).getId(), null,
				null);
	}

}
