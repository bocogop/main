package org.bocogop.wr.web.interceptor;

import java.net.URL;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import org.bocogop.shared.model.Permission;
import org.bocogop.shared.model.Permission.PermissionType;
import org.bocogop.shared.model.Role.RoleType;
import org.bocogop.shared.model.lookup.sds.VAFacility.VAFacilityValue;
import org.bocogop.shared.persistence.AppUserDAO;
import org.bocogop.shared.persistence.lookup.sds.StateDAO;
import org.bocogop.shared.util.WebUtil;
import org.bocogop.wr.config.CommonWebConfig;
import org.bocogop.wr.model.facility.Facility;
import org.bocogop.wr.model.facility.Facility.FacilityValue;
import org.bocogop.wr.persistence.dao.facility.FacilityDAO;
import org.bocogop.wr.persistence.dao.facility.KioskDAO;
import org.bocogop.wr.service.VelocityService;
import org.bocogop.wr.util.DateUtil;
import org.bocogop.wr.util.context.SessionUtil;
import org.bocogop.wr.web.UserNotifier;
import org.bocogop.wr.web.conversion.interceptor.AbstractInterceptor;

public abstract class AbstractReferenceDataInterceptor extends AbstractInterceptor {
	private static final Logger log = LoggerFactory.getLogger(AbstractReferenceDataInterceptor.class);

	public static final String ONE_TIME_USER_NOTIFICATION = "oneTimeUserNotification";

	@Autowired
	protected AppUserDAO appUserDAO;
	@Autowired
	protected FacilityDAO facilityDAO;
	@Autowired
	protected KioskDAO kioskDAO;
	@Autowired
	protected VelocityService velocityService;
	@Autowired
	protected StateDAO stateDAO;

	@Value("${session.pollingIntervalSeconds}")
	private int sessionPollingIntervalSeconds;
	@Value("${session.failedRequestsCount}")
	private int sessionFailedRequestsCount;
	@Value("${session.heartBeatTimeoutMillis}")
	private int sessionHeartBeatTimeoutMillis;
	@Value("${protocolHostnamePortOverride}")
	private String protocolHostnamePortOverride;

	@Value("${solr.baseURL}")
	private String solrBaseURL;
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
		WebUtil.addEnumToModel(VAFacilityValue.class, model);
		WebUtil.addEnumToModel(FacilityValue.class, model);

		/*
		 * Add all date util constants as they are defined (without adding
		 * "DATE_UTIL_" before their names) - CPB
		 */
		WebUtil.addClassConstantsToModel(DateUtil.class, model, false);

		/* Add constants from WebUtils for global error display */
		WebUtil.addClassConstantsToModel(WebUtil.class, model);

		model.put("AJAX_CONTEXT_PATH_PREFIX", CommonWebConfig.AJAX_CONTEXT_PATH_PREFIX);

		Facility facility = SessionUtil.getFacilityContext();
		model.put("facilityContextId", facility == null ? null : facility.getId());
		model.put("facilityContextName", SessionUtil.getFacilityContextName());
		model.put("facilityContextNumMeals", SessionUtil.getFacilityContextNumMeals());
		model.put("facilityContextIsCentralOffice", SessionUtil.isFacilityContextCentralOffice());

		model.put("useMinifiedDependencies", useMinifiedDependencies);

		model.put("appVersionNumber", appVersion);
		model.put("allStates", stateDAO.findAllSorted());

		int port = request.getServerPort();
		if (request.getScheme().equals("http") && port == 80) {
			port = -1;
		} else if (request.getScheme().equals("https") && port == 443) {
			port = -1;
		}

		String protocolHostnamePort = StringUtils.isNotBlank(protocolHostnamePortOverride)
				? protocolHostnamePortOverride
				: new URL(request.getScheme().replaceAll("[^A-Za-z0-9-\\.]", ""), request.getServerName(), port, "")
						.toString();
		model.put("protocolHostnamePort", protocolHostnamePort);
		model.put("maxGetRequestLength", maxGetRequestLength);
		
		addAdditionalReferenceData(request, response, handler, modelAndView);
	}

}
