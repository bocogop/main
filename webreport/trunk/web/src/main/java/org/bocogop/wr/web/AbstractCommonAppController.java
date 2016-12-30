package org.bocogop.wr.web;

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.bocogop.shared.model.Permission.PermissionType;
import org.bocogop.shared.persistence.AppUserDAO;
import org.bocogop.shared.persistence.AppUserPrecinctDAO;
import org.bocogop.shared.persistence.lookup.sds.GenderDAO;
import org.bocogop.shared.persistence.lookup.sds.StateDAO;
import org.bocogop.shared.service.AppUserService;
import org.bocogop.shared.util.SecurityUtil;
import org.bocogop.shared.util.ServletUtil;
import org.bocogop.wr.persistence.dao.ApplicationParametersDAO;
import org.bocogop.wr.persistence.dao.precinct.PrecinctDAO;
import org.bocogop.wr.persistence.dao.voter.VoterDAO;
import org.bocogop.wr.persistence.dao.voter.VoterHistoryEntryDAO;
import org.bocogop.wr.persistence.dao.voter.demographics.VolDemoDAO;
import org.bocogop.wr.service.PrecinctService;
import org.bocogop.wr.service.email.EmailService;
import org.bocogop.wr.service.voter.VoterService;
import org.bocogop.wr.util.DateUtil;
import org.bocogop.wr.web.ajax.AjaxRequestHandler;
import org.bocogop.wr.web.validation.WebValidationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.support.RequestContextUtils;

public abstract class AbstractCommonAppController {
	private static final Logger log = LoggerFactory.getLogger(AbstractCommonAppController.class);

	// ----------------------------------------- Static Fields and Constants

	public static final String DEFAULT_COMMAND_NAME = "command";

	public static final String FORM_READ_ONLY = "FORM_READ_ONLY";

	// ----------------------------------------- Fields

	// -------------------------- DAOs

	@Autowired
	protected ApplicationParametersDAO applicationParameterDAO;
	@Autowired
	protected AppUserPrecinctDAO appUserPrecinctDAO;
	@Autowired
	protected AppUserDAO appUserDAO;
	@Autowired
	protected GenderDAO genderDAO;
	@Autowired
	protected StateDAO stateDAO;
	@Autowired
	protected PrecinctDAO precinctDAO;
	@Autowired
	protected VoterDAO voterDAO;
	@Autowired
	protected VolDemoDAO voterDemographicsDAO;
	@Autowired
	protected VoterHistoryEntryDAO voterHistoryEntryDAO;

	// -------------------------- Services

	@Autowired
	protected AppUserService appUserService;
	@Autowired
	protected EmailService emailService;
	@Autowired
	protected PrecinctService precinctService;
	@Autowired
	protected VoterService voterService;

	// -------------------------- Others

	@Autowired
	protected AjaxRequestHandler ajaxRequestHandler;
	@Autowired
	protected DateUtil dateUtil;
	@Autowired
	protected org.bocogop.wr.util.context.SessionUtil sessionUtil;
	@Autowired
	protected Environment env;
	@Autowired
	protected MessageSource messageSource;
	@Autowired
	protected WebValidationService webValidationService;

	@Autowired
	protected UserNotifier userNotifier;

	protected boolean isAjax(HttpServletRequest r) {
		return AjaxRequestHandler.isAjax(r);
	}

	protected String getMessage(String code) {
		return getMessage(code, new Object[] {});
	}

	protected String getMessage(String code, Object[] args) {
		return messageSource.getMessage(code, args, Locale.getDefault());
	}

	@ExceptionHandler(Throwable.class)
	public ModelAndView processError(Throwable ex, HttpServletRequest request, HttpServletResponse response) {
		if (isAjax(request)) {
			/*
			 * Necessary to trigger the jQuery error() handler as opposed to the
			 * success() handler - CPB
			 */
			response.setStatus(HttpStatus.BAD_REQUEST.value());
			return ajaxRequestHandler.getExceptionModelAndView(ex, request);
		} else {
			return new ModelAndView("error", "exceptionStackTrace", ExceptionUtils.getFullStackTrace(ex));
		}
	}

	// ----------------------------------------- Utility Methods

	protected void requirePermissionsAtPrecinct(long precinctId, PermissionType... requiredPermissionsAtStation) {
		if (ArrayUtils.isNotEmpty(requiredPermissionsAtStation)) {
			SecurityUtil.ensureAllPermissionsAtPrecinct(precinctId, requiredPermissionsAtStation);
		}
	}

	protected void requirePermissionsAtCurrentPrecinct(PermissionType... requiredPermissionsAtPrecinct) {
		if (ArrayUtils.isNotEmpty(requiredPermissionsAtPrecinct)) {
			SecurityUtil.ensureAllPermissionsAtCurrentPrecinct(requiredPermissionsAtPrecinct);
		}
	}

	protected void setFormAsReadOnly(ModelMap model, boolean readOnly) {
		model.put(FORM_READ_ONLY, readOnly);
	}

	protected boolean formIsReadOnly(ModelMap model) {
		Boolean b = (Boolean) model.get(FORM_READ_ONLY);
		return b != null && b;
	}

	protected void setFormAsReadOnlyUnlessUserHasPermissions(ModelMap model, PermissionType... permissions) {
		boolean userHasPermissions = SecurityUtil.hasAllPermissionsAtCurrentPrecinct(permissions);
		setFormAsReadOnly(model, !userHasPermissions);
	}

	protected Locale getLocale(HttpServletRequest portletRequest) {
		HttpServletRequest request = ServletUtil.getThreadBoundServletRequest();
		Locale locale = request == null ? Locale.getDefault() : RequestContextUtils.getLocale(request);
		return locale;
	}

	protected String getCurrentUserName() {
		return SecurityUtil.getCurrentUserName();
	}

}
