package org.bocogop.shared.persistence.dao.audit;

import java.time.ZonedDateTime;
import java.util.List;

import org.bocogop.shared.model.AppUser;
import org.bocogop.shared.model.AuditLogEntry;
import org.bocogop.shared.persistence.dao.CustomizableSortedDAO;
import org.bocogop.shared.persistence.queryCustomization.QueryCustomization;

public interface AuditLogEntryDAO extends CustomizableSortedDAO<AuditLogEntry> {

	List<AuditLogEntry> findByCriteria(AppUser appUser, String methodNameContains, ZonedDateTime earliestDate,
			ZonedDateTime latestDate, QueryCustomization... customization);

}
