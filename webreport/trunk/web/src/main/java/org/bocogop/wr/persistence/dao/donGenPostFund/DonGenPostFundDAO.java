package org.bocogop.wr.persistence.dao.donGenPostFund;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

import org.bocogop.wr.model.donation.DonGenPostFund;
import org.bocogop.wr.persistence.dao.CustomizableSortedDAO;

public interface DonGenPostFundDAO extends CustomizableSortedDAO<DonGenPostFund> {

	List<DonGenPostFund> findByFacility(long facilityId);

	DonGenPostFund findByProgramCode(String gpfCode, long facilityId);

	/**
	 * @param facilityId
	 * @param startYear
	 *            TODO
	 * @param startMonth
	 *            TODO
	 * @param endYear
	 *            TODO
	 * @param endMonth
	 *            TODO
	 * @return A map of GPF ID to [donation total, expenditure total, ledger
	 *         adjustment total]
	 */
	Map<Long, BigDecimal[]> getDonGenPostFundReportByFacility(long facilityId, int startYear, int startMonth,
			int endYearInclusive, int endMonthInclusive);

	SortedMap<LocalDate, BigDecimal[]> getDailyLedger(long donGenPostFundId, LocalDate startDate, LocalDate endDate);

	BigDecimal[] getDonGenPostFundReport(long donGenPostFundId, LocalDate startDateInclusive,
			LocalDate endDateInclusive);

}
