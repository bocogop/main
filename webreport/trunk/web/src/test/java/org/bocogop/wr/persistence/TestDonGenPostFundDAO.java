package org.bocogop.wr.persistence;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.SortedMap;

import org.junit.Test;

import org.bocogop.wr.AbstractTransactionalWebDAOTest;
import org.bocogop.wr.model.donation.DonGenPostFund;
import org.bocogop.wr.persistence.dao.CustomizableAppDAO;

public class TestDonGenPostFundDAO extends AbstractTransactionalWebDAOTest<DonGenPostFund> {

	@Override
	protected CustomizableAppDAO<DonGenPostFund> getDAO() {
		return donGenPostFundDAO;
	}

	@Override
	protected DonGenPostFund getInstanceToSave() {
		DonGenPostFund dl = new DonGenPostFund();
		dl.setFacility(getFacility());
		dl.setGeneralPostFund("9054 - TEST_FUND");
		return dl;
	}

	@Override
	protected boolean testDelete() {
		return true;
	}

	@Test
	public void testGetDonGenPostFundReportByFacility() {
		// donGenPostFundDAO.getDonGenPostFundReportByFacility(getFacility().getId(),
		// 1900, 1, 2015, 5);

		LocalDate today = LocalDate.now();
		LocalDate nextMonth = today.plusMonths(1);
		Map<Long, BigDecimal[]> reportByFacility = donGenPostFundDAO.getDonGenPostFundReportByFacility(
				getFacility().getId(), today.getYear(), today.getMonthValue(), nextMonth.getYear(),
				nextMonth.getMonthValue());
		System.out.println(reportByFacility);
	}

	@Test
	public void testGetDailyLedger() {
		SortedMap<LocalDate, BigDecimal[]> reportByFacility = donGenPostFundDAO.getDailyLedger(
				donGenPostFundDAO.findSome(1).get(0).getId(), LocalDate.of(2000, 1, 1), LocalDate.of(2016, 1, 1));
		System.out.println(reportByFacility);
	}

	@Test
	public void testGetDonGenPostFundReport() {
		donGenPostFundDAO.getDonGenPostFundReport(donGenPostFundDAO.findSome(1).get(0).getId(),
				LocalDate.of(2000, 1, 1), LocalDate.of(2016, 1, 1));
	}

}
