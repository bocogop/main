package org.bocogop.wr.persistence.dao;

import java.time.LocalDate;
import java.util.List;

import org.bocogop.wr.model.expenditure.Expenditure;
import org.bocogop.wr.persistence.queryCustomization.QueryCustomization;

public interface ExpenditureDAO extends CustomizableSortedDAO<Expenditure> {

	List<Expenditure> findByCriteria(Long facilityId, Long donGenPostFundId, LocalDate onOrAfterDate,
			LocalDate onOrBeforeDate, QueryCustomization... customization);

}
