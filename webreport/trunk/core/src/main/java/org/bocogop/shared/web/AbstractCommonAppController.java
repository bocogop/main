package org.bocogop.shared.web;

import java.util.Locale;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.bocogop.shared.model.Permission.PermissionType;
import org.bocogop.shared.persistence.AppUserDAO;
import org.bocogop.shared.persistence.dao.ApplicationParametersDAO;
import org.bocogop.shared.persistence.dao.CountryDAO;
import org.bocogop.shared.persistence.dao.EventDAO;
import org.bocogop.shared.persistence.dao.GenderDAO;
import org.bocogop.shared.persistence.dao.ParticipationDAO;
import org.bocogop.shared.persistence.dao.PartyDAO;
import org.bocogop.shared.persistence.dao.StateDAO;
import org.bocogop.shared.persistence.dao.precinct.PrecinctDAO;
import org.bocogop.shared.persistence.dao.voter.VoterDAO;
import org.bocogop.shared.persistence.dao.voter.VoterHistoryEntryDAO;
import org.bocogop.shared.persistence.dao.voter.demographics.VoterDemographicsDAO;
import org.bocogop.shared.service.AppUserService;
import org.bocogop.shared.service.EventService;
import org.bocogop.shared.service.ParticipationService;
import org.bocogop.shared.service.PrecinctService;
import org.bocogop.shared.service.email.EmailService;
import org.bocogop.shared.service.voter.VoterService;
import org.bocogop.shared.util.SecurityUtil;
import org.bocogop.shared.util.ServletUtil;
import org.bocogop.shared.util.context.SessionUtil;
import org.bocogop.shared.web.ajax.AjaxRequestHandler;
import org.bocogop.shared.web.validation.WebValidationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
	protected AppUserDAO appUserDAO;
	@Autowired
	protected CountryDAO countryDAO;
	@Autowired
	protected EventDAO eventDAO;
	@Autowired
	protected GenderDAO genderDAO;
	@Autowired
	protected ParticipationDAO participationDAO;
	@Autowired
	protected PartyDAO partyDAO;
	@Autowired
	protected PrecinctDAO precinctDAO;
	@Autowired
	protected StateDAO stateDAO;
	@Autowired
	protected VoterDAO voterDAO;
	@Autowired
	protected VoterDemographicsDAO voterDemographicsDAO;
	@Autowired
	protected VoterHistoryEntryDAO voterHistoryEntryDAO;

	// -------------------------- Services

	@Autowired
	protected AppUserService appUserService;
	@Autowired
	protected EmailService emailService;
	@Autowired
	protected EventService eventService;
	@Autowired
	protected ParticipationService participationService;
	@Autowired
	protected PrecinctService precinctService;
	@Autowired
	protected VoterService voterService;

	// -------------------------- Others

	@Autowired
	protected AjaxRequestHandler ajaxRequestHandler;
	@Autowired
	@Qualifier("coreSessionUtil")
	protected SessionUtil sessionUtil;
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

	protected void requirePermissionsAtPrecinct(PermissionType... requiredPermissionsAtStation) {
		if (ArrayUtils.isNotEmpty(requiredPermissionsAtStation)) {
			SecurityUtil.ensureAllPermissions(requiredPermissionsAtStation);
		}
	}

	protected void requirePermissionsAtCurrentPrecinct(PermissionType... requiredPermissionsAtPrecinct) {
		if (ArrayUtils.isNotEmpty(requiredPermissionsAtPrecinct)) {
			SecurityUtil.ensureAllPermissions(requiredPermissionsAtPrecinct);
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
		boolean userHasPermissions = SecurityUtil.hasAllPermissions(permissions);
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

	/**
	 * @param cookieName
	 *            The cookie to delete; if null, all cookies will be deleted
	 * @param req
	 * @param resp
	 */
	protected void eraseCookie(String cookieName, HttpServletRequest req, HttpServletResponse resp) {
		Cookie[] cookies = req.getCookies();
		if (cookies == null)
			return;

		for (int i = 0; i < cookies.length; i++) {
			if (cookieName != null && cookies[i].getName().equals(cookieName))
				continue;

			cookies[i].setValue("");
			cookies[i].setPath("/");
			cookies[i].setMaxAge(0);
			resp.addCookie(cookies[i]);
		}
	}

}
