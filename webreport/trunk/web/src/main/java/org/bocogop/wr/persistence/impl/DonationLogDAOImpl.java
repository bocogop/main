package org.bocogop.wr.persistence.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Query;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import org.bocogop.wr.model.donation.DonationLog;
import org.bocogop.wr.model.donation.DonationSummary;
import org.bocogop.wr.persistence.dao.DonationLogDAO;
import org.bocogop.wr.persistence.queryCustomization.QueryCustomization;

@Repository
public class DonationLogDAOImpl extends GenericHibernateSortedDAOImpl<DonationLog> implements DonationLogDAO {
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(DonationLogDAOImpl.class);

	@SuppressWarnings("unchecked")
	@Override
	public List<DonationLog> findDonationLogByStationNumber(String stationNumber, boolean excludeProcessedItems) {
		StringBuilder sb = new StringBuilder("select d from ").append(DonationLog.class.getName()).append(" d");

		QueryCustomization cust = new QueryCustomization();

		if (cust.getOrderBy() == null)
			cust.setOrderBy("d.transactionDate desc");

		List<String> whereClauseItems = new ArrayList<>();
		Map<String, Object> params = new HashMap<>();

		if (!StringUtils.isBlank(stationNumber)) {
			whereClauseItems.add("d.facility = :stationNumber");
			params.put("stationNumber", stationNumber);
		}

		if (excludeProcessedItems)
			whereClauseItems.add("d.trackingId not in (select a.epayTrackingID from " + DonationSummary.class.getName()
					+ " a where a.epayTrackingID is not null)");

		Query q = constructQuery(em, sb, whereClauseItems, params, null, cust);

		return q.getResultList();
	}

	@Override
	public int countByCriteria(String stationNumber, boolean excludeProcessedItems) {
		String queryStr = "select count(*) from " + DonationLog.class.getName()
				+ " d where d.facility = :stationNumber";
		if (excludeProcessedItems)
			queryStr += " and not exists (select a from " + DonationSummary.class.getName()
					+ " a where a.epayTrackingID = d.trackingId)";
		Query q = em.createQuery(queryStr).setParameter("stationNumber", stationNumber);
		Number n = (Number) q.getSingleResult();
		return n.intValue();
	}

}
