package org.bocogop.wr.persistence.impl.donGenPostFund;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.persistence.Query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import org.bocogop.wr.model.donation.DonGenPostFund;
import org.bocogop.wr.model.facility.Facility;
import org.bocogop.wr.persistence.dao.donGenPostFund.DonGenPostFundDAO;
import org.bocogop.wr.persistence.dao.facility.FacilityDAO;
import org.bocogop.wr.persistence.impl.GenericHibernateSortedDAOImpl;
import org.bocogop.wr.persistence.queryCustomization.QueryCustomization;

@Repository
public class DonGenPostFundDAOImpl extends GenericHibernateSortedDAOImpl<DonGenPostFund> implements DonGenPostFundDAO {
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(DonGenPostFundDAOImpl.class);

	@Autowired
	private FacilityDAO facilityDAO;

	/*
	 * @Override public List<DonGenPostFund> findByCriteria(Long id,
	 * QueryCustomization... customization) { return null; }
	 */

	@SuppressWarnings("unchecked")
	@Override
	public DonGenPostFund findByProgramCode(String gpfCode, long facilityId) {

		List<String> whereClauseItems = new ArrayList<>();
		Map<String, Object> params = new HashMap<>();

		StringBuilder sb = new StringBuilder("select gpf from ").append(DonGenPostFund.class.getName()).append(" gpf");
		sb.append(" left join fetch gpf.facility f");

		if (gpfCode != null) {
			whereClauseItems.add("TRIM(LOWER(gpf.generalPostFund)) like :gpfCode");
			params.put("gpfCode", gpfCode.toLowerCase().trim() + "%");
		}
		if (facilityId > 0) {
			whereClauseItems.add("f.id = :facilityId");
			params.put("facilityId", facilityId);
		}

		Query q = constructQuery(em, sb, whereClauseItems, params, null);
		List<DonGenPostFund> results = q.getResultList();

		return results.isEmpty() ? null : results.get(0);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<DonGenPostFund> findByFacility(long facilityId) {
		StringBuilder sb = new StringBuilder("select d from ").append(DonGenPostFund.class.getName()).append(" d");
		sb.append(" left join fetch d.facility f");

		QueryCustomization cust = new QueryCustomization();

		if (cust.getOrderBy() == null)
			cust.setOrderBy("d.generalPostFund");

		List<String> whereClauseItems = new ArrayList<>();
		Map<String, Object> params = new HashMap<>();

		if (facilityId > 0) {
			whereClauseItems.add("d.facility.id = :facilityId");
			params.put("facilityId", facilityId);
		}

		Query q = constructQuery(em, sb, whereClauseItems, params, null, cust);

		return q.getResultList();
	}

	@Override
	public BigDecimal[] getDonGenPostFundReport(long donGenPostFundId, LocalDate startDateInclusive,
			LocalDate endDateInclusive) {
		LocalDate endDateExclusive = endDateInclusive.plusDays(1);

		String query = "with donation_sums as (" //
				+ "		select top 1 id = :gpfId,"
				+ "			period_total = sum(dd.DonationValue)" //
				+ "		from wr.DonationSummary ds" //
				+ "			join wr.DonationDetail dd on ds.id = dd.DonationSummaryFK" //
				+ "		where dd.GeneralPostFundFK = :gpfId" //
				+ "			and ds.DonationDate >= :startDateInclusive" //
				+ "			and ds.DonationDate < :endDateExclusive" //
				+ "	)," //
				+ "	expenditure_sums as (" //
				+ "		select top 1 id = :gpfId,"
				+ "			period_total = sum(e.Amount)" //
				+ "		from wr.Expenditure e" //
				+ "		where e.GeneralPostFundFK = :gpfId" //
				+ "			and e.RequestDate >= :startDateInclusive" //
				+ "			and e.RequestDate < :endDateExclusive" //
				+ "	)," //
				+ "	ledger_adjustment_sums as (" //
				+ "		select top 1 id = :gpfId,"
				+ "			period_total = sum(e.Amount)" //
				+ "		from wr.LedgerAdjustment e" //
				+ "		where e.GeneralPostFundFK = :gpfId" //
				+ "			and e.RequestDate >= :startDateInclusive" //
				+ "			and e.RequestDate < :endDateExclusive" //
				+ "	)" //
				+ "	select period_donation_total = ISNULL(ds.period_total, 0)," //
				+ "		period_expenditure_total = ISNULL(es.period_total, 0)," //
				+ "		period_ledger_adjustment_total = ISNULL(las.period_total, 0)" //
				+ "	from wr.DonGenPostFund gpf" //
				+ "		left join donation_sums ds on gpf.id = ds.id" //
				+ "		left join expenditure_sums es on gpf.id = es.id" //
				+ "		left join ledger_adjustment_sums las on gpf.id = las.id" //
				+ "	where gpf.id = :gpfId";

		Query q = em.createNativeQuery(query) //
				.setParameter("startDateInclusive", startDateInclusive) //
				.setParameter("endDateExclusive", endDateExclusive) //
				.setParameter("gpfId", donGenPostFundId);

		@SuppressWarnings("unchecked")
		List<Object[]> rows = q.getResultList();
		if (rows.isEmpty())
			return null;

		Object[] row = rows.get(0);
		BigDecimal periodDonationTotal = (BigDecimal) row[0];
		BigDecimal periodExpenditureTotal = (BigDecimal) row[1];
		BigDecimal periodLedgerAdjustmentTotal = (BigDecimal) row[2];
		return new BigDecimal[] { periodDonationTotal, periodExpenditureTotal, periodLedgerAdjustmentTotal };
	}

	@SuppressWarnings("unchecked")
	@Override
	public Map<Long, BigDecimal[]> getDonGenPostFundReportByFacility(long facilityId, int startYear, int startMonth,
			int endYearInclusive, int endMonthInclusive) {
		Facility f = facilityDAO.findRequiredByPrimaryKey(facilityId);
		LocalDate fiscalYearStart = dateUtil.getCurrentFiscalYearStartDate(f.getTimeZone());

		LocalDate startDateInclusive = LocalDate.of(startYear, startMonth, 1);
		LocalDate endDateExclusive = LocalDate.of(endYearInclusive, endMonthInclusive, 1).plusMonths(1);

		String query = "with donation_sums as (" //
				+ "		select gpf.id," //
				+ "			period_total = sum(case when ds.DonationDate >= :startDateInclusive" //
				+ "				and ds.DonationDate < :endDateExclusive then dd.DonationValue else 0 end)," //
				+ "			grand_total = sum(dd.DonationValue)" //
				+ "		from wr.DonGenPostFund gpf" //
				+ "			left join wr.DonationSummary ds on gpf.FacilityFK = ds.FacilityFK" //
				+ "				and ds.DonationDate >= :fiscalYearStartDate" //
				+ "			left join wr.DonationDetail dd on ds.id = dd.DonationSummaryFK" //
				+ "				and dd.GeneralPostFundFK = gpf.id" //
				+ "		where gpf.FacilityFK = :facilityId" //
				+ "		group by gpf.id" //
				+ "	)," //
				+ "	expenditure_sums as (" //
				+ "		select gpf.id," //
				+ "			period_total = sum(case when e.RequestDate >= :startDateInclusive" //
				+ "				and e.RequestDate < :endDateExclusive then e.Amount else 0 end)," //
				+ "			grand_total = sum(e.Amount)" //
				+ "		from wr.DonGenPostFund gpf" //
				+ "			left join wr.Expenditure e on gpf.FacilityFK = e.FacilityFK" //
				+ "				and e.GeneralPostFundFK = gpf.id" //
				+ "				and e.RequestDate >= :fiscalYearStartDate" //
				+ "		where gpf.FacilityFK = :facilityId" //
				+ "		group by gpf.id" //
				+ "	)," //
				+ "	ledger_adjustment_sums as (" //
				+ "		select gpf.id," //
				+ "			period_total = sum(case when e.RequestDate >= :startDateInclusive" //
				+ "				and e.RequestDate < :endDateExclusive then e.Amount else 0 end)," //
				+ "			grand_total = sum(e.Amount)" //
				+ "		from wr.DonGenPostFund gpf" //
				+ "			left join wr.LedgerAdjustment e on gpf.FacilityFK = e.FacilityFK" //
				+ "				and e.GeneralPostFundFK = gpf.id" //
				+ "				and e.RequestDate >= :fiscalYearStartDate" //
				+ "		where gpf.FacilityFK = :facilityId" //
				+ "		group by gpf.id" //
				+ "	)" //
				+ " select DonGenPostFundFK," //
				+ "		period_donation_total = sum(period_donation_total)," //
				+ "		period_expenditure_total = sum(period_expenditure_total),"
				+ "		period_ledger_adjustment_total = sum(period_ledger_adjustment_total)," //
				+ "		grand_donation_total = sum(grand_donation_total)," //
				+ "		grand_expenditure_total = sum(grand_expenditure_total),"
				+ "		grand_ledger_adjustment_total = sum(grand_ledger_adjustment_total)" //
				+ "	from (" //
				+ "		select DonGenPostFundFK = gpf.id," //
				+ "			period_donation_total = ISNULL(ds.period_total, 0)," //
				+ "			period_expenditure_total = ISNULL(es.period_total, 0)," //
				+ "			period_ledger_adjustment_total = ISNULL(las.period_total, 0)," //
				+ "			grand_donation_total = ISNULL(ds.grand_total, 0)," //
				+ "			grand_expenditure_total = ISNULL(es.grand_total, 0)," //
				+ "			grand_ledger_adjustment_total = ISNULL(las.grand_total, 0)" //
				+ "		from wr.DonGenPostFund gpf" //
				+ "			left join donation_sums ds on gpf.id = ds.id" //
				+ "			left join expenditure_sums es on gpf.id = es.id" //
				+ "			left join ledger_adjustment_sums las on gpf.id = las.id" //
				+ "		where gpf.FacilityFK = :facilityId" //
				+ " union all " //
				+ "		select d.DonGenPostFundFK," //
				+ "			period_donation_total = case when d.MonthStartDay >= :startDateInclusive" //
				+ "				and d.MonthStartDay < :endDateExclusive then d.TotalDonations else 0 end," //
				+ "			period_expenditure_total = case when d.MonthStartDay >= :startDateInclusive" //
				+ "				and d.MonthStartDay < :endDateExclusive then d.TotalExpenditures else 0 end," //
				+ "			period_ledger_adjustment_total = case when d.MonthStartDay >= :startDateInclusive" //
				+ "				and d.MonthStartDay < :endDateExclusive then d.TotalAdjustments else 0 end," //
				+ "			grand_donation_total = d.TotalDonations," //
				+ "			grand_expenditure_total = d.TotalExpenditures," //
				+ "			grand_ledger_adjustment_total = d.TotalAdjustments" //
				+ "		from wr.SUMM_DonGenPostFund d" //
				+ "			join wr.DonGenPostFund g on d.DonGenPostFundFK = g.id" //
				+ "		where g.FacilityFK = :facilityId" //
				+ "			and d.MonthStartDay < :fiscalYearStartDate";
		query += "	) a " //
				+ "	group by a.DonGenPostFundFK";

		Query q = em.createNativeQuery(query) //
				.setParameter("startDateInclusive", startDateInclusive) //
				.setParameter("endDateExclusive", endDateExclusive) //
				.setParameter("fiscalYearStartDate", fiscalYearStart) //
				.setParameter("facilityId", facilityId);

		Map<Long, BigDecimal[]> results = new HashMap<>();

		List<Object[]> rows = q.getResultList();
		for (Object[] row : rows) {
			long gpfId = ((Number) row[0]).longValue();
			BigDecimal periodDonationTotal = (BigDecimal) row[1];
			BigDecimal periodExpenditureTotal = (BigDecimal) row[2];
			BigDecimal periodLedgerAdjustmentTotal = (BigDecimal) row[3];
			BigDecimal grandDonationTotal = (BigDecimal) row[4];
			BigDecimal grandExpenditureTotal = (BigDecimal) row[5];
			BigDecimal grandLedgerAdjustmentTotal = (BigDecimal) row[6];
			results.put(gpfId,
					new BigDecimal[] { periodDonationTotal, periodExpenditureTotal, periodLedgerAdjustmentTotal,
							grandDonationTotal, grandExpenditureTotal, grandLedgerAdjustmentTotal });
		}
		return results;
	}

	@Override
	public SortedMap<LocalDate, BigDecimal[]> getDailyLedger(long donGenPostFundId, LocalDate startDateInclusive,
			LocalDate endDateInclusive) {
		LocalDate endDateExclusive = endDateInclusive.plusDays(1);

		Query q = em.createNativeQuery("with change_dates as (" //
				+ "	select distinct dt = ds.DonationDate" //
				+ "	from wr.DonationSummary ds" //
				+ "		join wr.DonationDetail dd on dd.DonationSummaryFK = ds.id" //
				+ "	where dd.GeneralPostFundFK = :gpfId" //
				+ "		and ds.DonationDate >= :startDate" //
				+ "		and ds.DonationDate < :endDate" //
				+ "	union all" //
				+ "	select distinct dt = e.RequestDate" //
				+ "	from wr.Expenditure e" //
				+ "	where e.GeneralPostFundFK = :gpfId" //
				+ "		and e.RequestDate >= :startDate" //
				+ "		and e.RequestDate < :endDate" //
				+ "	union all" //
				+ "	select distinct dt = la.RequestDate" //
				+ "	from wr.LedgerAdjustment la" //
				+ "	where la.GeneralPostFundFK = :gpfId" //
				+ "		and la.RequestDate >= :startDate" //
				+ "		and la.RequestDate < :endDate" //
				+ "	)" //
				+ "	select cd.dt," //
				+ "		don_sum = ISNULL(sum(dd.DonationValue), 0)," //
				+ "		exp_sum = ISNULL(sum(e.Amount), 0)," //
				+ "		adj_sum = ISNULL(sum(la.Amount), 0)" //
				+ "	from change_dates cd" //
				+ "		LEFT JOIN wr.DonationSummary ds on cd.dt = ds.DonationDate" //
				+ "		LEFT JOIN wr.DonationDetail dd on dd.DonationSummaryFK = ds.id" //
				+ "			and dd.GeneralPostFundFK = :gpfId" //
				+ "		LEFT JOIN wr.Expenditure e on e.RequestDate = cd.dt" //
				+ "			and e.GeneralPostFundFK = :gpfId" //
				+ "		LEFT JOIN wr.LedgerAdjustment la on la.RequestDate = cd.dt" //
				+ "			and la.GeneralPostFundFK = :gpfId" //
				+ "	group by cd.dt" //
				+ "	order by cd.dt");
		q.setParameter("gpfId", donGenPostFundId);
		q.setParameter("startDate", startDateInclusive);
		q.setParameter("endDate", endDateExclusive);

		SortedMap<LocalDate, BigDecimal[]> results = new TreeMap<>();
		@SuppressWarnings("unchecked")
		List<Object[]> queryResults = q.getResultList();
		for (Object[] result : queryResults) {
			results.put(((Timestamp) result[0]).toLocalDateTime().toLocalDate(),
					new BigDecimal[] { (BigDecimal) result[1], (BigDecimal) result[2], (BigDecimal) result[3] });
		}
		return results;
	}

}
