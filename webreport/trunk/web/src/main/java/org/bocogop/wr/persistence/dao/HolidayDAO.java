package org.bocogop.wr.persistence.dao;

import java.time.LocalDate;
import java.util.List;

import org.bocogop.wr.model.Holiday;

public interface HolidayDAO extends CustomizableSortedDAO<Holiday> {

	List<Holiday> findByCriteria(LocalDate observanceDate);

}
