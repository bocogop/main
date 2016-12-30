package org.bocogop.wr.persistence.impl.audit;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Query;

import org.bocogop.wr.model.AppUser;
import org.bocogop.wr.model.AuditLogEntry;
import org.bocogop.wr.persistence.dao.audit.AuditLogEntryDAO;
import org.bocogop.wr.persistence.impl.GenericHibernateSortedDAOImpl;
import org.bocogop.wr.persistence.queryCustomization.QueryCustomization;
import org.springframework.stereotype.Repository;

@Repository
public class AuditLogEntryDAOImpl extends GenericHibernateSortedDAOImpl<AuditLogEntry>implements AuditLogEntryDAO {

	@SuppressWarnings("unchecked")
	@Override
	public List<AuditLogEntry> findByCriteria(AppUser appUser, String methodNameContains,
			ZonedDateTime earliestDateInclusive, ZonedDateTime latestDateExclusive,
			QueryCustomization... customization) {
		StringBuilder sb = new StringBuilder("from ").append(AuditLogEntry.class.getName()).append(" r");

		List<String> whereClauseItems = new ArrayList<>();
		Map<String, Object> params = new HashMap<>();

		if (appUser != null) {
			whereClauseItems.add("r.appUserId = :appUserId");
			params.put("appUserId", appUser.getId());
		}

		if (methodNameContains != null) {
			whereClauseItems.add("LOWER(r.methodName) like :name");
			params.put("name", "%" + methodNameContains.toLowerCase() + "%");
		}

		if (earliestDateInclusive != null) {
			whereClauseItems.add("r.date >= :earliestDate");
			params.put("earliestDate", earliestDateInclusive);
		}

		if (latestDateExclusive != null) {
			whereClauseItems.add("r.date < :latestDate");
			params.put("latestDate", latestDateExclusive);
		}

		Query q = constructQuery(em, sb, whereClauseItems, params, null, customization);

		return q.getResultList();
	}

}
