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

import org.bocogop.wr.model.mealTicket.MealTicket;
import org.bocogop.wr.persistence.dao.MealTicketDAO;
import org.bocogop.wr.persistence.queryCustomization.QueryCustomization;

@Repository
public class MealTicketDAOImpl extends GenericHibernateSortedDAOImpl<MealTicket>
		implements MealTicketDAO {
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(MealTicketDAOImpl.class);

	@SuppressWarnings("unchecked")
	@Override
	public List<MealTicket> findByCriteria(Long facilityId, Long volunteerId, LocalDate mealDate, QueryCustomization... customization) {
		
		List<String> whereClauseItems = new ArrayList<>();
		Map<String, Object> params = new HashMap<>();

		StringBuilder sb = new StringBuilder("select mt from ").append(MealTicket.class.getName()).append(" mt") //
				.append(" left join fetch mt.volunteer v") //
				.append(" join mt.facility f");

		QueryCustomization cust = ArrayUtils.isEmpty(customization) ? new QueryCustomization() : customization[0];
		cust.appendRemainingJoins(sb, "mt");

		if (facilityId != null) {
			whereClauseItems.add("mt.facility.id = :facilityId");
			params.put("facilityId", facilityId);
		}
		
		if (volunteerId != null) {
			whereClauseItems.add("mt.volunteer.id = :volunteerId");
			params.put("volunteerId", volunteerId);
		}

		if (mealDate != null) {
			whereClauseItems.add("mt.mealDate = :mealDate");
			params.put("mealDate", mealDate);
		}

		if (cust.getOrderBy() == null)
			cust.setOrderBy("mt.unscheduled, v.lastName, mt.occasionalLastName,  mt.occasionalFirstName, mt.id");

		Query q = constructQuery(em, sb, whereClauseItems, params, null, cust);
		return new ArrayList<MealTicket>(q.getResultList());
	}

}
