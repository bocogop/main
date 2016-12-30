package org.bocogop.wr.web;

import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
import org.bocogop.wr.config.WebSecurityConfig;
import org.bocogop.wr.model.AppUser;
import org.bocogop.wr.model.Permission.PermissionType;
import org.bocogop.wr.model.lookup.TemplateType;
import org.bocogop.wr.model.precinct.Precinct;
import org.bocogop.wr.persistence.dao.precinct.PrecinctDAO;
import org.bocogop.wr.persistence.impl.AbstractAppDAOImpl;
import org.bocogop.wr.service.VelocityService;
import org.bocogop.wr.web.breadcrumbs.Breadcrumb;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class CommonController extends AbstractAppController {
	private static final Logger log = LoggerFactory.getLogger(CommonController.class);

	private static final String VIEW_STATION_CHANGE = "stationChange";

	public static final String BREADCRUMB_HOME = "Home";
	public static final String URL_HOME = "/home.htm";
	public static final String VIEW_HOME = "home";

	@Autowired
	private VelocityService velocityService;
	@Value("${notification.maxResults}")
	private int notificationMaxResults;
	@Value("${authProvider.activeDirectory.active}")
	private boolean adAuthActive;
	@Value("${logout.postRedirectUrl}")
	private String logoutPostRedirectUrl;
	@Value("${timeout.postRedirectUrl}")
	private String timeoutPostRedirectUrl;
	@Value("${userError.postRedirectUrl}")
	private String userErrorPostRedirectUrl;

	@RequestMapping(value = "/processAuthorizationException.htm", method = RequestMethod.GET)
	public String authError() {
		return "authorizationException";
	}

	@RequestMapping(value = WebSecurityConfig.URI_LOGIN, method = RequestMethod.GET)
	public String loginPage(@RequestParam(required = false) String error, @RequestParam Map<String, String> allParams,
			ModelMap model) {
		AppUser u = getCurrentUser();
		if (u != null) {
			log.debug("loginPage - redirecting to index since user context already exists");
			return "redirect:/index.htm";
		}

		if (StringUtils.isNotBlank(error)) {
			model.addAttribute("errorMessage", velocityService.mergeTemplateIntoString("login.error." + error));
		}
		if (allParams.containsKey("logout"))
			model.addAttribute("userLoggedOut", true);

		if (adAuthActive) {
			return "login";
		} else {
			model.put("userErrorPostRedirectUrl", userErrorPostRedirectUrl);
			return "userError";
		}
	}

	@RequestMapping(value = WebSecurityConfig.URI_LOGOUT, method = RequestMethod.GET)
	public String logoutPage(HttpServletRequest request, HttpServletResponse response,
			@RequestParam(required = false) String timeout) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth != null) {
			new SecurityContextLogoutHandler().logout(request, response, auth);
		}

		if (timeout != null) {
			log.debug("logoutPage - redirecting to {}", timeoutPostRedirectUrl);
			return "redirect:" + timeoutPostRedirectUrl;
		} else {
			log.debug("logoutPage - redirecting to {}", logoutPostRedirectUrl);
			return "redirect:" + logoutPostRedirectUrl;
		}
	}

	@RequestMapping("/index.htm")
	public String index(ModelMap modelMap, HttpSession session) {
		return "redirect:" + URL_HOME;
	}

	@RequestMapping(URL_HOME)
	@Breadcrumb(BREADCRUMB_HOME)
	public String home(ModelMap model) {
		model.put("homepageContent", velocityService.mergeTemplateIntoString(TemplateType.HOMEPAGE_CONTENT.getName()));
		model.put("homepageAnnouncement",
				velocityService.mergeTemplateIntoString(TemplateType.HOMEPAGE_ANNOUNCEMENT.getName()));
		model.put("notificationMaxResults", notificationMaxResults);

		// Already a station in session. Do nothing, redirect to sitemap.
		return VIEW_HOME;
	}

	/**
	 * Renew the session, so it will not timeout, ajax call
	 */
	@RequestMapping("/keepAlive")
	public @ResponseBody boolean keepAlive() {
		log.debug("Received request to keep-alive the session...");
		return true;
	}

	private boolean isPrecinctAvailableToUser(AppUser user, Precinct newPrecinct) {
		return user.getAppUserPrecinct(newPrecinct.getId()) != null;
	}

	public static void populatePrecinctList(AppUser user, ModelMap model, PrecinctDAO precinctDAO) {
		model.put("precinctList", user.getAssignedPrecincts());
	}

	@RequestMapping(value = "/selectStation.htm", method = RequestMethod.GET)
	public String selectStation(ModelMap model) {
		populateStationChangeModel(model);
		model.put("cancelAllowed", false);
		return VIEW_STATION_CHANGE;
	}

	@RequestMapping("/changeStation.htm")
	public String changeStation(ModelMap model) {
		populateStationChangeModel(model);
		model.put("cancelAllowed", true);
		return VIEW_STATION_CHANGE;
	}

	private void populateStationChangeModel(ModelMap model) {
		AppUser user = getCurrentUser();

		Precinct currentPrecinct = user.getLastVisitedPrecinct();
		if (currentPrecinct != null) {
			model.put("currentStationName", currentPrecinct.getName());
			model.put("currentStationId", currentPrecinct.getId());
		}

		populatePrecinctList(user, model, precinctDAO);
	}

	private void setNewDutyStation(Precinct newDutyStation) {
		AppUser user = getCurrentUser();
		user.setLastVisitedPrecinct(newDutyStation);
		appUserService.updateFieldsWithoutVersionCheck(user.getId(), false, newDutyStation.getId(), false, null, null);
	}

	@RequestMapping(value = "/selectStation.htm", method = RequestMethod.POST)
	public String processSelectStation(@RequestParam Long stationCode) {
		Precinct f = precinctDAO.findByPrimaryKey(stationCode);
		setNewDutyStation(f);
		return "redirect:" + URL_HOME;
	}

	@RequestMapping("/updatePreferences")
	public @ResponseBody boolean updatePreferences() {
		AppUser user = getCurrentUser();
		appUserService.updatePreferences(user.getId());
		return true;
	}

	@RequestMapping("/getPrecinctsWithUserPermission")
	public @ResponseBody Set<Precinct> getPrecinctsWithUserPermission(@RequestParam PermissionType permission,
			@RequestParam(required = false) Boolean activeStatus) {
		Set<Precinct> s = getCurrentUser().getPrecinctsWhereUserHasAllPermissions(permission);
		return s;
	}

	@RequestMapping("/flushEveryOp")
	public @ResponseBody boolean flushEveryOperationTrue() {
		AbstractAppDAOImpl.FLUSH_EVERY_OPERATION = true;
		return true;
	}

	@RequestMapping("/noFlushEveryOp")
	public @ResponseBody boolean flushEveryOperationFalse() {
		AbstractAppDAOImpl.FLUSH_EVERY_OPERATION = false;
		return true;
	}

}
