package org.bocogop.shared.persistence.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Query;

import org.bocogop.shared.model.Event;
import org.bocogop.shared.persistence.dao.EventDAO;
import org.bocogop.shared.persistence.queryCustomization.QueryCustomization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;


@Repository
public class EventDAOImpl extends GenericHibernateSortedDAOImpl<Event> implements EventDAO {
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(EventDAOImpl.class);

	@Override
	public List<Event> findByCriteria(Boolean registrationStatus) {
		StringBuilder sb = new StringBuilder("select v from ").append(Event.class.getName()).append(" v");

		/* Don't bother with this yet - CPB */
		QueryCustomization cust = new QueryCustomization();

		List<String> whereClauseItems = new ArrayList<>();
		Map<String, Object> params = new HashMap<>();

		if (registrationStatus != null) {
			whereClauseItems.add("v.registered = :registrationStatus");
			params.put("registrationStatus", registrationStatus);
		}

		Query q = constructQuery(em, sb, whereClauseItems, params, null, cust);

		@SuppressWarnings("unchecked")
		List<Event> resultList = q.getResultList();
		return resultList;
	}

}
