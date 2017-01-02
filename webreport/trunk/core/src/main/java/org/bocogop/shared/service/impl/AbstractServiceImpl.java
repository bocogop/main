package org.bocogop.shared.service.impl;

import org.bocogop.shared.model.CoreUserDetails;
import org.bocogop.shared.persistence.AppUserDAO;
import org.bocogop.shared.persistence.dao.ApplicationParametersDAO;
import org.bocogop.shared.persistence.dao.EventDAO;
import org.bocogop.shared.persistence.dao.GenderDAO;
import org.bocogop.shared.persistence.dao.PermissionDAO;
import org.bocogop.shared.persistence.dao.RoleDAO;
import org.bocogop.shared.persistence.dao.TemplateDAO;
import org.bocogop.shared.persistence.dao.audit.AuditLogEntryDAO;
import org.bocogop.shared.persistence.dao.precinct.PrecinctDAO;
import org.bocogop.shared.persistence.dao.voter.VoterDAO;
import org.bocogop.shared.service.email.EmailService;
import org.bocogop.shared.service.validation.ServiceValidationException;
import org.bocogop.shared.util.SecurityUtil;
import org.bocogop.shared.util.context.SessionUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;

@Transactional(rollbackFor = ServiceValidationException.class)
public class AbstractServiceImpl {

	@Autowired
	protected ApplicationParametersDAO applicationParametersDAO;
	@Autowired
	protected AppUserDAO appUserDAO;
	@Autowired
	protected AuditLogEntryDAO auditLogEntryDAO;
	@Autowired
	protected EventDAO eventDAO;
	@Autowired
	protected GenderDAO genderDAO;
	@Autowired
	protected PermissionDAO permissionDAO;
	@Autowired
	protected RoleDAO roleDAO;
	@Autowired
	protected TemplateDAO templateDAO;
	@Autowired
	protected PrecinctDAO precinctDAO;
	@Autowired
	protected VoterDAO voterDAO;

	@Autowired
	@Qualifier("coreSessionUtil")
	protected SessionUtil sessionUtil;
	@Autowired
	protected EmailService emailService;
	@Autowired
	protected MessageSource messageSource;
	@Autowired
	@Qualifier("transactionManager")
	protected PlatformTransactionManager tm;

	/**
	 * Returns the current AppUser, or null if there is no current user or the
	 * current user is not an AppUser (e.g. is a background daemon user). See
	 * SecurityUtil.getCurrentUserAsOrNull for details. CPB
	 */
	protected CoreUserDetails getCurrentUser() {
		return SecurityUtil.getCurrentUserAsOrNull(CoreUserDetails.class);
	}

	protected <T extends CoreUserDetails> T getCurrentUserAsOrNull(Class<T> c) {
		return SecurityUtil.getCurrentUserAsOrNull(c);
	}

}
