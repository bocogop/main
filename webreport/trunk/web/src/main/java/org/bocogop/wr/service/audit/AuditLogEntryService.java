package org.bocogop.wr.service.audit;

import org.bocogop.wr.model.AuditLogEntry;

public interface AuditLogEntryService {

	AuditLogEntry saveOrUpdate(AuditLogEntry entry);

}
