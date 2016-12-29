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

import org.bocogop.wr.model.time.AdjustedHoursEntry;
import org.bocogop.wr.persistence.dao.AdjustedHoursEntryDAO;
import org.bocogop.wr.persistence.queryCustomization.QueryCustomization;

@Repository
public class AdjustedHoursEntryDAOImpl extends GenericHibernateSortedDAOImpl<AdjustedHoursEntry>
		implements AdjustedHoursEntryDAO {
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(AdjustedHoursEntryDAOImpl.class);

	@SuppressWarnings("unchecked")
	@Override
	public List<AdjustedHoursEntry> findByCriteria(Long facilityId, Long volunteerId, LocalDate date,
			QueryCustomization... customization) {
		List<String> whereClauseItems = new ArrayList<>();
		Map<String, Object> params = new HashMap<>();

		StringBuilder sb = new StringBuilder("select mt from ").append(AdjustedHoursEntry.class.getName())
				.append(" mt");

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

		if (date != null) {
			whereClauseItems.add("mt.date = :date");
			params.put("date", date);
		}

		if (cust.getOrderBy() == null)
			cust.setOrderBy("mt.date");

		Query q = constructQuery(em, sb, whereClauseItems, params, null, cust);
		return q.getResultList();
	}

}
