package org.bocogop.shared.service.audit;

import org.bocogop.shared.model.AuditLogEntry;

public interface AuditLogEntryService {

	AuditLogEntry saveOrUpdate(AuditLogEntry entry);

}
