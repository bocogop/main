package org.bocogop.wr.service.impl;

import org.bocogop.wr.model.CoreUserDetails;
import org.bocogop.wr.persistence.AppUserDAO;
import org.bocogop.wr.persistence.dao.ApplicationParametersDAO;
import org.bocogop.wr.persistence.dao.TemplateDAO;
import org.bocogop.wr.persistence.dao.audit.AuditLogEntryDAO;
import org.bocogop.wr.persistence.dao.precinct.PrecinctDAO;
import org.bocogop.wr.persistence.dao.voter.VoterDAO;
import org.bocogop.wr.persistence.lookup.GenderDAO;
import org.bocogop.wr.persistence.lookup.PermissionDAO;
import org.bocogop.wr.persistence.lookup.RoleDAO;
import org.bocogop.wr.service.email.EmailService;
import org.bocogop.wr.service.validation.ServiceValidationException;
import org.bocogop.wr.util.DateUtil;
import org.bocogop.wr.util.SecurityUtil;
import org.bocogop.wr.util.context.SessionUtil;
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
	protected SessionUtil sessionUtil;
	@Autowired
	protected DateUtil dateUtil;
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
