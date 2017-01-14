package org.bocogop.wr.web;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
import org.bocogop.shared.model.AppUser;
import org.bocogop.shared.model.lookup.TemplateType;
import org.bocogop.shared.persistence.impl.AbstractAppDAOImpl;
import org.bocogop.shared.service.VelocityService;
import org.bocogop.shared.web.AbstractAppController;
import org.bocogop.wr.config.WebSecurityConfig;
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

	public static final String BREADCRUMB_HOME = "Home";
	public static final String URL_HOME = "/home.htm";
	public static final String VIEW_HOME = "home";

	@Autowired
	private VelocityService velocityService;
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

	@RequestMapping("/updatePreferences")
	public @ResponseBody boolean updatePreferences() {
		AppUser user = getCurrentUser();
		appUserService.updatePreferences(user.getId());
		return true;
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
