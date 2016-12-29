package org.bocogop.wr.persistence.dao.audit;

import java.time.ZonedDateTime;
import java.util.List;

import org.bocogop.shared.model.AppUser;
import org.bocogop.wr.model.AuditLogEntry;
import org.bocogop.wr.persistence.dao.CustomizableSortedDAO;
import org.bocogop.wr.persistence.queryCustomization.QueryCustomization;

public interface AuditLogEntryDAO extends CustomizableSortedDAO<AuditLogEntry> {

	List<AuditLogEntry> findByCriteria(AppUser appUser, String methodNameContains, ZonedDateTime earliestDate,
			ZonedDateTime latestDate, QueryCustomization... customization);

}
