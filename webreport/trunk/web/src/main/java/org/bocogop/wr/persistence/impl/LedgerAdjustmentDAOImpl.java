package org.bocogop.wr.persistence.impl;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Query;

import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import org.bocogop.wr.model.expenditure.LedgerAdjustment;
import org.bocogop.wr.persistence.dao.LedgerAdjustmentDAO;
import org.bocogop.wr.persistence.queryCustomization.QueryCustomization;

@Repository
public class LedgerAdjustmentDAOImpl extends GenericHibernateSortedDAOImpl<LedgerAdjustment>
		implements LedgerAdjustmentDAO {
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(LedgerAdjustmentDAOImpl.class);

	@SuppressWarnings("unchecked")
	@Override
	public List<LedgerAdjustment> findByCriteria(Long facilityId, Long donGenPostFundId, LocalDate onOrAfterDate,
			LocalDate onOrBeforeDate, QueryCustomization... customization) {
		List<String> whereClauseItems = new ArrayList<>();
		Map<String, Object> params = new HashMap<>();

		StringBuilder sb = new StringBuilder("select e from ").append(LedgerAdjustment.class.getName()).append(" e");

		QueryCustomization cust = ArrayUtils.isEmpty(customization) ? new QueryCustomization() : customization[0];
		cust.appendRemainingJoins(sb, "e");

		if (facilityId != null) {
			whereClauseItems.add("e.facility.id = :facilityId");
			params.put("facilityId", facilityId);
		}

		if (donGenPostFundId != null) {
			whereClauseItems.add("e.donGenPostFund.id = :donGenPostFundId");
			params.put("donGenPostFundId", donGenPostFundId);
		}

		if (onOrAfterDate != null) {
			whereClauseItems.add("e.requestDate >= :onOrAfterDate");
			params.put("onOrAfterDate", onOrAfterDate);
		}

		if (onOrBeforeDate != null) {
			whereClauseItems.add("e.requestDate <= :onOrBeforeDate");
			params.put("onOrBeforeDate", onOrBeforeDate);
		}

		if (cust.getOrderBy() == null)
			cust.setOrderBy("e.requestDate");

		Query q = constructQuery(em, sb, whereClauseItems, params, null, cust);
		return q.getResultList();
	}

}
