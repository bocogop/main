package org.bocogop.kiosk.web;

import static org.bocogop.kiosk.config.WebSecurityConfig.URI_LOGIN;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.ValidationException;

import org.apache.commons.lang3.StringUtils;
import org.bocogop.kiosk.config.WebSecurityConfig;
import org.bocogop.shared.model.Event;
import org.bocogop.shared.model.lookup.TemplateType;
import org.bocogop.shared.model.voter.MultiVoterTempUserDetails;
import org.bocogop.shared.model.voter.Voter;
import org.bocogop.shared.service.VelocityService;
import org.bocogop.shared.service.validation.ServiceValidationException;
import org.bocogop.shared.util.SecurityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;

@Controller
@SessionAttributes(value = { "command" })
public class KioskController extends AbstractKioskController {
	private static final Logger log = LoggerFactory.getLogger(KioskController.class);

	public static final String SESSION_ATTR_EVENT_ID = "eventId";
	private static final String VIEW_EVENT_CHANGE = "eventChange";
	public static final String BREADCRUMB_HOME = "Home";
	public static final String URL_HOME = "/home.htm";
	public static final String VIEW_HOME = "home";

	@Autowired
	private VelocityService velocityService;

	// @Autowired
	// private LocaleResolver localeResolver;
	@Autowired
	private VoterValidator voterValidator;

	@RequestMapping(value = URI_LOGIN, method = RequestMethod.GET)
	public String loginPage(@RequestParam(required = false) String error, @RequestParam(required = false) Long eventId,
			@RequestParam(required = false) Boolean thankYou,
			@CookieValue(required = false, name = "eventId") Long cookieEventId, ModelMap model,
			HttpServletResponse response) {
		String locale = LocaleContextHolder.getLocale().getLanguage();

		if (eventId == null) {
			if (cookieEventId == null) {
				return "redirect:/selectEvent.htm";
			} else {
				if (eventDAO.findByPrimaryKey(cookieEventId) == null) {
					return "eventMissing";
				} else {
					return "redirect:/login.htm?eventId=" + cookieEventId //
							+ (error != null ? "&error=" + error : "") //
							+ (thankYou != null ? "&thankYou=" + thankYou : "");
				}
			}
		} else {
			if (cookieEventId == null || cookieEventId.equals(eventId) == false) {
				/*
				 * Save a cookie so that we don't have to pass it thru the
				 * spring login page, and so that we can retrieve it if they log
				 * out - CPB
				 */
				response.addCookie(getCookie(eventId));
			}
		}

		Event event = eventDAO.findByPrimaryKey(eventId);
		if (event == null) {
			return "eventMissing";
		}

		if (StringUtils.isNotBlank(error)) {
			model.addAttribute("errorMessage",
					velocityService.mergeTemplateIntoString("login.error." + error + "." + locale));
		}

		model.addAttribute("globalIntroText",
				velocityService.mergeTemplateIntoString("kiosk.globalIntroText" + "." + locale));
		return "login";
	}

	public Cookie getCookie(Long eventId) {
		Cookie cookie = new Cookie("eventId", String.valueOf(eventId));
		cookie.setHttpOnly(true);
		// 10 years ought to do it
		cookie.setMaxAge(60 * 60 * 24 * 365 * 10);
		return cookie;
	}

	@RequestMapping(value = WebSecurityConfig.URI_LOGOUT, method = RequestMethod.GET)
	public String logoutPage(@RequestParam(required = false) Boolean thankYou, HttpServletRequest request,
			HttpServletResponse response) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth != null) {
			new SecurityContextLogoutHandler().logout(request, response, auth);
		}

		return "redirect:/login.htm?logout" + (thankYou != null && thankYou ? "&thankYou=true" : "");
	}

	@RequestMapping("/index.htm")
	public String index(ModelMap model, HttpServletRequest request, HttpServletResponse response, HttpSession session,
			@CookieValue(name = "eventId") long cookieEventId) {
		Event event = eventDAO.findRequiredByPrimaryKey(cookieEventId);
		setEventContext(event);
		
		MultiVoterTempUserDetails multiMatch = SecurityUtil.getCurrentUserAsOrNull(MultiVoterTempUserDetails.class);
		if (multiMatch != null) {
			return "redirect:/refineUser.htm";
		}

		// Voter v =
		// voterDAO.findRequiredByPrimaryKey(getCurrentUser().getId());
		// Language preferredLanguage = v.getPreferredLanguage();
		// if (preferredLanguage != null)
		// localeResolver.setLocale(request, response,
		// Locale.forLanguageTag(preferredLanguage.getCulture()));

		return "redirect:" + URL_HOME;
	}

	@RequestMapping("/refineUser.htm")
	public String refineUser(ModelMap model) {
		MultiVoterTempUserDetails multiMatch = SecurityUtil.getCurrentUserAsOrNull(MultiVoterTempUserDetails.class);
		if (multiMatch == null)
			return "redirect:/index.htm";
		
		return "refineUser";
	}

	/**
	 * Renew the session, so it will not timeout, ajax call
	 */
	@RequestMapping("/keepAlive")
	public @ResponseBody boolean keepAlive() {
		log.debug("Received request to keep-alive the session...");
		return true;
	}

	@RequestMapping(URL_HOME)
	public String home(ModelMap model) {
		Voter v = getCurrentUser();
		
		participationService.logParticipation(v.getId(), getRequiredEventContextId());
		
		String locale = LocaleContextHolder.getLocale().getLanguage();
		model.put("homepageContent",
				velocityService.mergeTemplateIntoString("kiosk." + TemplateType.HOMEPAGE_CONTENT.getName() + "." + locale));
		model.put("homepageAnnouncement",
				velocityService.mergeTemplateIntoString("kiosk." + TemplateType.HOMEPAGE_ANNOUNCEMENT.getName() + "." + locale));

		// Already a station in session. Do nothing, redirect to sitemap.
		return VIEW_HOME;
	}

	@RequestMapping(value = "/selectEvent.htm", method = RequestMethod.GET)
	public String selectStation(ModelMap model) {
		model.put("eventList", eventDAO.findAllSorted());
		model.put("cancelAllowed", false);
		return VIEW_EVENT_CHANGE;
	}

	@RequestMapping(value = "/selectEvent.htm", method = RequestMethod.POST)
	public String processSelectStation(@RequestParam long eventId, HttpServletResponse response) {
		return "redirect:" + URI_LOGIN + "?eventId=" + eventId;
	}

	@RequestMapping("/voterEdit.htm")
	public String voterEdit(ModelMap model, HttpServletRequest request, HttpSession session) {
		Long voterId = getCurrentUser().getId();
		Voter voter = voterDAO.findRequiredByPrimaryKey(voterId);

		VoterCommand command = new VoterCommand(voter);
		model.put(DEFAULT_COMMAND_NAME, command);
		createReferenceData(model);

		return "editVoter";
	}

	private void createReferenceData(ModelMap model) {
		model.put("allGenders", genderDAO.findAllSorted());
		model.put("allStates", stateDAO.findAllSortedByCountry());
	}

	@RequestMapping("/voterSubmit.htm")
	public String voterSubmit(@ModelAttribute(DEFAULT_COMMAND_NAME) VoterCommand command, BindingResult result,
			SessionStatus status, ModelMap model, HttpServletRequest request, HttpServletResponse response)
			throws ValidationException {
		Voter voter = command.getVoter();

		voterValidator.validate(command, result, false, "voter");
		boolean hasErrors = result.hasErrors();

		if (!hasErrors) {
			try {
				voter = voterService.saveOrUpdate(voter, true, true);
				// if voter is terminated, logout
				userNotifier.notifyUserOnceWithMessage(request, getMessage("voter.update.success"));
			} catch (ServiceValidationException e) {
				webValidationService.handle(e, result);
				hasErrors = true;
			}
		}

		if (hasErrors) {
			createReferenceData(model);
			return "editVoter";
		} else {
			status.setComplete();
			return "redirect:/voterEdit.htm?id=" + voter.getId();
		}
	}

}
