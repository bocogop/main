package org.bocogop.wr.persistence.dao;

import java.time.LocalDate;
import java.util.List;

import org.bocogop.wr.model.time.AdjustedHoursEntry;
import org.bocogop.wr.persistence.queryCustomization.QueryCustomization;

public interface AdjustedHoursEntryDAO extends CustomizableSortedDAO<AdjustedHoursEntry> {

	public List<AdjustedHoursEntry> findByCriteria(Long facilityId, Long volunteerId, LocalDate date, QueryCustomization... customization);

}
