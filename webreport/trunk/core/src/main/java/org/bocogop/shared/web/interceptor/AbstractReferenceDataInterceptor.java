package org.bocogop.shared.web.interceptor;

import java.net.URL;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
import org.bocogop.shared.config.CommonWebConfig;
import org.bocogop.shared.model.Permission;
import org.bocogop.shared.model.Permission.PermissionType;
import org.bocogop.shared.model.Role.RoleType;
import org.bocogop.shared.persistence.AppUserDAO;
import org.bocogop.shared.persistence.dao.EventDAO;
import org.bocogop.shared.persistence.dao.precinct.PrecinctDAO;
import org.bocogop.shared.service.VelocityService;
import org.bocogop.shared.util.DateUtil;
import org.bocogop.shared.util.WebUtil;
import org.bocogop.shared.web.UserNotifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

public abstract class AbstractReferenceDataInterceptor extends AbstractInterceptor {
	private static final Logger log = LoggerFactory.getLogger(AbstractReferenceDataInterceptor.class);

	public static final String ONE_TIME_USER_NOTIFICATION = "oneTimeUserNotification";

	@Autowired
	protected AppUserDAO appUserDAO;
	@Autowired
	protected EventDAO eventDAO;
	@Autowired
	protected PrecinctDAO precinctDAO;
	@Autowired
	protected VelocityService velocityService;

	@Value("${session.pollingIntervalSeconds}")
	private int sessionPollingIntervalSeconds;
	@Value("${session.failedRequestsCount}")
	private int sessionFailedRequestsCount;
	@Value("${session.heartBeatTimeoutMillis}")
	private int sessionHeartBeatTimeoutMillis;

	@Value("${web.version}")
	private String appVersion;
	@Value("${useMinifiedDependencies}")
	private String useMinifiedDependencies;
	@Value("${maxGetRequestLength}")
	private int maxGetRequestLength;

	protected abstract void addAdditionalReferenceData(HttpServletRequest request, HttpServletResponse response,
			Object handler, ModelAndView modelAndView) throws Exception;

	@Override
	public final void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
			ModelAndView modelAndView) throws Exception {
		if (isAjax(request)) {
			log.debug("Request was an AJAX request, no need to populate reference data");
			return;
		}

		if (modelAndView == null) {
			log.debug("No modelAndView returned, the controller must have dealt with the response manually.");
			return;
		}

		Map<String, Object> model = modelAndView.getModel();
		if (model == null) {
			log.error(
					"No model in the ModelAndView, that is unexpected. Returning without adding common reference data.");
			return;
		}

		if (modelAndView.getView() instanceof RedirectView
				|| (modelAndView.isReference() && modelAndView.getViewName().startsWith("redirect:"))) {
			log.debug("View is null or a redirect, no need to populate reference data");
			return;
		}

		HttpSession session = request.getSession();

		session.setAttribute("sessionPollingIntervalSeconds", new Integer(sessionPollingIntervalSeconds));
		session.setAttribute("sessionFailedRequestsCount", new Integer(sessionFailedRequestsCount));
		session.setAttribute("sessionHeartBeatTimeoutMillis", new Integer(sessionHeartBeatTimeoutMillis));

		String val = UserNotifier.getAndRemoveAttributeFromSession(session);
		if (val != null)
			model.put(ONE_TIME_USER_NOTIFICATION, val);

		/*
		 * Add all roles and permissions to model for standard use in
		 * <security:authorize> JSP tags - CPB
		 */
		WebUtil.addClassConstantsToModel(Permission.class, model, true);
		WebUtil.addEnumToModel(PermissionType.class, model);
		WebUtil.addEnumToModel(RoleType.class, model);

		/*
		 * Add all date util constants as they are defined (without adding
		 * "DATE_UTIL_" before their names) - CPB
		 */
		WebUtil.addClassConstantsToModel(DateUtil.class, model, false);

		/* Add constants from WebUtils for global error display */
		WebUtil.addClassConstantsToModel(WebUtil.class, model);

		model.put("AJAX_CONTEXT_PATH_PREFIX", CommonWebConfig.AJAX_CONTEXT_PATH_PREFIX);

		model.put("useMinifiedDependencies", useMinifiedDependencies);

		model.put("appVersionNumber", appVersion);

		int port = request.getServerPort();
		if (request.getScheme().equals("http") && port == 80) {
			port = -1;
		} else if (request.getScheme().equals("https") && port == 443) {
			port = -1;
		}

		model.put("maxGetRequestLength", maxGetRequestLength);

		addAdditionalReferenceData(request, response, handler, modelAndView);
	}

}
