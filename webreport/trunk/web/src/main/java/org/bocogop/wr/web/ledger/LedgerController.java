package org.bocogop.wr.web.ledger;

import static org.bocogop.wr.util.DateUtil.TWO_DIGIT_DATE_ONLY;
import static org.bocogop.wr.util.DateUtil.TWO_DIGIT_DATE_ONLY_FORMAT;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import org.bocogop.wr.model.donation.DonGenPostFund;
import org.bocogop.wr.util.DateUtil;
import org.bocogop.wr.web.AbstractAppController;
import org.bocogop.wr.web.breadcrumbs.Breadcrumb;
import org.bocogop.wr.web.expenditure.ExpenditureController;
import org.bocogop.wr.web.interceptor.BreadcrumbsInterceptor;

@Controller
public class LedgerController extends AbstractAppController {

	@Autowired
	private ExpenditureController expenditureController;
	@Autowired
	private BreadcrumbsInterceptor breadcrumbsInterceptor;

	@RequestMapping("/ledger/summary")
	public @ResponseBody List<GPFSummary> getSummary(@RequestParam String dateRangeType,
			@RequestParam(required = false) Integer specificFY, HttpSession session) {
		LocalDate startDate = null;
		LocalDate endDate = null;

		ZoneId tz = getFacilityTimeZone();

		if ("fy".equals(dateRangeType)) {
			startDate = dateUtil.getCurrentFiscalYearStartDate(tz);
			endDate = dateUtil.getCurrentFiscalYearEndDate(tz);
		} else if ("lastfy".equals(dateRangeType)) {
			startDate = dateUtil.getPreviousFiscalYearStartDate(tz);
			endDate = dateUtil.getPreviousFiscalYearEndDate(tz);
		} else if ("month".equals(dateRangeType)) {
			startDate = LocalDate.now(tz).withDayOfMonth(1);
			endDate = LocalDate.now(tz);
		} else if ("lastmonth".equals(dateRangeType)) {
			startDate = LocalDate.now(tz).withDayOfMonth(1).minusMonths(1);
			endDate = startDate.with(TemporalAdjusters.lastDayOfMonth());
		} else if ("specificfy".equals(dateRangeType)) {
			startDate = dateUtil.getFiscalYearStartDateForDate(LocalDate.of(specificFY, 1, 1));
			endDate = dateUtil.getFiscalYearEndDateForDate(LocalDate.of(specificFY, 1, 1));
		} else {
			throw new IllegalArgumentException("Unsupported date range type '" + dateRangeType + "'");
		}

		List<GPFSummary> results = getGPFBalances(startDate.getYear(), startDate.getMonthValue(), endDate.getYear(),
				endDate.getMonthValue());

		Map<String, Object> params = new HashMap<>();
		params.put("dateRangeType", dateRangeType);
		if ("specificfy".equals(dateRangeType) && specificFY != null)
			params.put("specificFY", specificFY);
		breadcrumbsInterceptor.updateCurrentBreadcrumbParameters(session, params);

		return results;
	}

	@RequestMapping("/ledger/gpf")
	public @ResponseBody Map<String, Object> getDailySummaryForGPF(@RequestParam long id,
			@RequestParam String dateRangeType, @RequestParam(required = false) Integer specificFY,
			@RequestParam(required = false) @DateTimeFormat(pattern = TWO_DIGIT_DATE_ONLY) LocalDate startDate,
			@RequestParam(required = false) @DateTimeFormat(pattern = TWO_DIGIT_DATE_ONLY) LocalDate endDate,
			HttpSession session) {
		Map<String, Object> resultMap = new HashMap<>();

		DonGenPostFund gpf = donGenPostFundDAO.findRequiredByPrimaryKey(id);

		ZoneId tz = getFacilityTimeZone();

		if (!"custom".equals(dateRangeType)) {
			if ("fy".equals(dateRangeType)) {
				startDate = dateUtil.getCurrentFiscalYearStartDate(tz);
				endDate = LocalDate.now(tz);
			} else if ("lastfy".equals(dateRangeType)) {
				startDate = dateUtil.getPreviousFiscalYearStartDate(tz);
				endDate = dateUtil.getPreviousFiscalYearEndDate(tz);
			} else if ("specificfy".equals(dateRangeType)) {
				startDate = dateUtil.getFiscalYearStartDateForDate(LocalDate.of(specificFY, 1, 1));
				endDate = dateUtil.getFiscalYearEndDateForDate(LocalDate.of(specificFY, 1, 1));
			} else if ("last6month".equals(dateRangeType)) {
				startDate = LocalDate.now(tz).minusMonths(6);
				endDate = LocalDate.now(tz);
			} else if ("lastmonth".equals(dateRangeType)) {
				startDate = LocalDate.now(tz).withDayOfMonth(1).minusMonths(1);
				endDate = startDate.with(TemporalAdjusters.lastDayOfMonth());
			} else if ("month".equals(dateRangeType)) {
				startDate = LocalDate.now(tz).withDayOfMonth(1);
				endDate = LocalDate.now(tz);
			} else {
				throw new IllegalArgumentException("Unsupported date range type '" + dateRangeType + "'");
			}
		}

		BigDecimal[] gpfReport = donGenPostFundDAO.getDonGenPostFundReport(id, LocalDate.of(1900, 1, 1), startDate);
		resultMap.put("startingBalance", new GPFSummary(gpf, gpfReport).getPeriodChange());

		Map<LocalDate, BigDecimal[]> dailySummaries = donGenPostFundDAO.getDailyLedger(id, startDate, endDate);

		List<GPFDailySummary> results = new ArrayList<>();
		for (Entry<LocalDate, BigDecimal[]> entry : dailySummaries.entrySet()) {
			BigDecimal[] vals = entry.getValue();
			results.add(new GPFDailySummary(entry.getKey(), gpf, vals[0], vals[1], vals[2]));
		}
		resultMap.put("dailySummaries", results);

		Map<String, Object> params = new HashMap<>();
		params.put("donGenPostFundId", id);
		params.put("dateRangeType", dateRangeType);
		if ("specificfy".equals(dateRangeType) && specificFY != null)
			params.put("specificFY", specificFY);
		if ("custom".equals(dateRangeType) && startDate != null)
			params.put("startDate", startDate.format(TWO_DIGIT_DATE_ONLY_FORMAT));
		if ("custom".equals(dateRangeType) && endDate != null)
			params.put("endDate", endDate.format(TWO_DIGIT_DATE_ONLY_FORMAT));
		breadcrumbsInterceptor.updateCurrentBreadcrumbParameters(session, params);

		return resultMap;
	}

	@RequestMapping("/ledger/gpfBalances")
	public @ResponseBody List<GPFSummary> getGPFSummary() {
		LocalDate today = LocalDate.now();
		LocalDate startOfMonth = today.withDayOfMonth(1);
		return getGPFBalances(startOfMonth.getYear(), startOfMonth.getMonthValue(), today.getYear(),
				today.getMonthValue());
	}

	private List<GPFSummary> getGPFBalances(int startYear, int startMonth, int endYear, int endMonth) {
		List<GPFSummary> results = new ArrayList<>();
		List<DonGenPostFund> gpfs = donGenPostFundDAO.findByFacility(getFacilityContextId());
		Map<Long, BigDecimal[]> gpfSums = donGenPostFundDAO.getDonGenPostFundReportByFacility(getFacilityContextId(),
				startYear, startMonth, endYear, endMonth);
		for (DonGenPostFund gpf : gpfs) {
			BigDecimal[] sums = gpfSums.get(gpf.getId());
			results.add(new GPFSummary(gpf, sums));
		}

		return results;
	}

	@RequestMapping("/ledger.htm")
	@Breadcrumb("General Ledger")
	public String showGeneralLedger(ModelMap model, @RequestParam(required = false) String dateRangeType,
			@RequestParam(required = false) Integer specificFY) {
		model.put("dateRangeType", dateRangeType);
		model.put("specificFY", specificFY);

		ZoneId tz = getFacilityTimeZone();
		model.put("currentfy", new LocalDate[] { dateUtil.getCurrentFiscalYearStartDate(tz),
				dateUtil.getCurrentFiscalYearEndDate(tz) });
		model.put("lastfy", new LocalDate[] { dateUtil.getPreviousFiscalYearStartDate(tz),
				dateUtil.getPreviousFiscalYearEndDate(tz) });
		model.put("month", new LocalDate[] { LocalDate.now().withDayOfMonth(1), LocalDate.now() });
		model.put("lastmonth", new LocalDate[] { LocalDate.now().minusMonths(1).withDayOfMonth(1),
				LocalDate.now().minusMonths(1).with(TemporalAdjusters.lastDayOfMonth()) });

		Map<String, LocalDate[]> fyDates = new HashMap<>();
		for (int i = 1995; i <= LocalDate.now().getYear() + 1; i++)
			fyDates.put(String.valueOf(i), dateUtil.getStartAndEndDatesForFiscalYear(i));
		model.put("fyDates", fyDates);

		createReferenceData(model);
		return "ledger";
	}

	@RequestMapping("/ledgerDaily.htm")
	@Breadcrumb("GPF Daily Ledger")
	public String showDailyLedger(@RequestParam long donGenPostFundId,
			@RequestParam(required = false) Integer specificFY, @RequestParam(required = false) String dateRangeType,
			@RequestParam(required = false) @DateTimeFormat(pattern = TWO_DIGIT_DATE_ONLY) LocalDate startDate,
			@RequestParam(required = false) @DateTimeFormat(pattern = TWO_DIGIT_DATE_ONLY) LocalDate endDate,
			ModelMap model) {
		DonGenPostFund f = donGenPostFundDAO.findRequiredByPrimaryKey(donGenPostFundId);
		model.put("donGenPostFund", f);
		model.put("dateRangeType", dateRangeType);
		model.put("specificFY", specificFY);
		if (startDate != null)
			model.put("startDate", startDate.format(TWO_DIGIT_DATE_ONLY_FORMAT));
		if (endDate != null)
			model.put("endDate", endDate.format(TWO_DIGIT_DATE_ONLY_FORMAT));
		createReferenceData(model);
		return "ledgerDaily";
	}

	private void createReferenceData(ModelMap model) {
		expenditureController.populateReferenceDataForPopup(model);
		model.put("currentFY", dateUtil.getCurrentFiscalYear(getFacilityTimeZone()));
	}
}
