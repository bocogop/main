package org.bocogop.wr.persistence.dao;

import java.time.LocalDate;
import java.util.List;

import org.bocogop.wr.model.mealTicket.MealTicket;
import org.bocogop.wr.persistence.queryCustomization.QueryCustomization;

public interface MealTicketDAO extends CustomizableSortedDAO<MealTicket> {

	public List<MealTicket> findByCriteria(Long facilityId, Long volunteerId, LocalDate mealDate,
			QueryCustomization... customization);

}
