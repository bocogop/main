package org.bocogop.wr.persistence.impl;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.persistence.Query;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import org.bocogop.shared.model.core.AbstractAuditedPersistent;
import org.bocogop.wr.model.printing.PrintRequest;
import org.bocogop.wr.persistence.dao.PrintRequestDAO;
import org.bocogop.wr.persistence.queryCustomization.QueryCustomization;

@Repository
public class PrintRequestDAOImpl extends GenericHibernateSortedDAOImpl<PrintRequest> implements PrintRequestDAO {
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(PrintRequestDAOImpl.class);

	@Override
	public List<PrintRequest> findByCriteria(Long kioskId, ZonedDateTime requestedBefore, Boolean completedStatus,
			ZonedDateTime completedBefore) {
		StringBuilder sb = new StringBuilder("select v from ").append(PrintRequest.class.getName()).append(" v");

		QueryCustomization cust = new QueryCustomization();
		cust.setOrderBy("v.kiosk.id");

		List<String> whereClauseItems = new ArrayList<>();
		Map<String, Object> params = new HashMap<>();

		if (kioskId != null) {
			whereClauseItems.add("v.kiosk.id = :kioskId");
			params.put("kioskId", kioskId);
		}

		if (requestedBefore != null) {
			whereClauseItems.add("v.requestTime < :requestedBefore");
			params.put("requestedBefore", requestedBefore);
		}

		if (completedStatus != null) {
			whereClauseItems.add("v.completionTime is " + (completedStatus ? "not" : "") + " null");
		}

		Query q = constructQuery(em, sb, whereClauseItems, params, null, cust);

		@SuppressWarnings("unchecked")
		List<PrintRequest> resultList = q.getResultList();
		return resultList;
	}

	@Override
	public int bulkUpdate(List<Long> printRequestIds, ZonedDateTime setCompletedDate) {
		if (printRequestIds == null)
			throw new IllegalArgumentException("No restriction criteria specified");
		if (setCompletedDate == null)
			throw new IllegalArgumentException("No updates specified");

		flush();

		List<String> updates = new ArrayList<>();
		Map<String, Object> params = new HashMap<>();

		if (setCompletedDate != null) {
			updates.add("completionTime = :completionTime");
			params.put("completionTime", setCompletedDate);
		}

		String jpql = "update " + PrintRequest.class.getName() //
				+ " set " + StringUtils.join(updates, ", ") //
				+ ", modifiedBy = :myUser, modifiedDate = :nowUTC, version = version + 1" // "
				+ " where id in (select pr.id from " + PrintRequest.class.getName() + " pr where 1=1" //
				+ (printRequestIds != null ? " and pr.id in (:printRequestIds)" : "") //
				+ ")";

		Query q = query(jpql);

		for (Entry<String, Object> param : params.entrySet())
			q.setParameter(param.getKey(), param.getValue());
		if (printRequestIds != null)
			q.setParameter("printRequestIds", printRequestIds);
		q.setParameter("myUser", AbstractAuditedPersistent.getCurrentUserIdForAudit()) //
				.setParameter("nowUTC", ZonedDateTime.now(ZoneId.of("Z")));

		return q.executeUpdate();
	}

	@Override
	public int bulkDeleteByCriteria(ZonedDateTime requestedTimeBefore, ZonedDateTime completedTimeBefore) {
		if (requestedTimeBefore == null && completedTimeBefore == null)
			throw new IllegalArgumentException("Must specify at least one piece of filtering criteria");

		flush();

		Query q = query("delete from " + PrintRequest.class.getName() + " where id in (select pr.id from "
				+ PrintRequest.class.getName() + " pr" //
				+ " where (1=2" //
				+ (requestedTimeBefore != null ? " or pr.requestTime < :requestedTimeBefore" : "") //
				+ (completedTimeBefore != null ? " or pr.completionTime < :completedTimeBefore" : "") //
				+ "))");
		if (requestedTimeBefore != null)
			q.setParameter("requestedTimeBefore", requestedTimeBefore);
		if (completedTimeBefore != null)
			q.setParameter("completedTimeBefore", completedTimeBefore);
		return q.executeUpdate();
	}

}
