package org.bocogop.wr.persistence.dao;

import java.time.LocalDate;
import java.util.List;

import org.bocogop.wr.model.expenditure.LedgerAdjustment;
import org.bocogop.wr.persistence.queryCustomization.QueryCustomization;

public interface LedgerAdjustmentDAO extends CustomizableSortedDAO<LedgerAdjustment> {

	List<LedgerAdjustment> findByCriteria(Long facilityContextId, Long donGenPostFundId, LocalDate beginDate,
			LocalDate endDate, QueryCustomization... customization);

}
