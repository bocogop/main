package org.bocogop.wr.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import org.bocogop.wr.model.AuditLogEntry;
import org.bocogop.wr.service.audit.AuditLogEntryService;

@Service
public class AuditLogEntryServiceImpl extends AbstractServiceImpl implements AuditLogEntryService {
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(AuditLogEntryServiceImpl.class);

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public AuditLogEntry saveOrUpdate(AuditLogEntry entry) {
		return auditLogEntryDAO.saveOrUpdate(entry);
	}

}
