package org.bocogop.wr.persistence.impl;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import org.bocogop.wr.model.Holiday;
import org.bocogop.wr.persistence.dao.HolidayDAO;
import org.bocogop.wr.persistence.queryCustomization.QueryCustomization;

@Repository
public class HolidayDAOImpl extends GenericHibernateSortedDAOImpl<Holiday> implements HolidayDAO {
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(HolidayDAOImpl.class);

	@Override
	public List<Holiday> findByCriteria(LocalDate observanceDate) {
		StringBuilder sb = new StringBuilder("select v from ").append(Holiday.class.getName()).append(" v");

		QueryCustomization cust = new QueryCustomization();

		List<String> whereClauseItems = new ArrayList<>();
		Map<String, Object> params = new HashMap<>();

		if (observanceDate != null) {
			whereClauseItems.add("v.observanceDate = :observanceDate");
			params.put("observanceDate", observanceDate);
		}

		Query q = constructQuery(em, sb, whereClauseItems, params, null, cust);

		@SuppressWarnings("unchecked")
		List<Holiday> resultList = q.getResultList();
		return resultList;
	}

}
