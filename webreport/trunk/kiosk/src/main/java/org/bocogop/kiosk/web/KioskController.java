package org.bocogop.kiosk.web;

import static org.bocogop.kiosk.config.WebSecurityConfig.URI_LOGIN;

import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.ValidationException;

import org.apache.commons.lang3.StringUtils;
import org.bocogop.kiosk.config.WebSecurityConfig;
import org.bocogop.shared.model.Event;
import org.bocogop.shared.model.lookup.State;
import org.bocogop.shared.model.lookup.TemplateType;
import org.bocogop.shared.model.voter.MultiVoterTempUserDetails;
import org.bocogop.shared.model.voter.Voter;
import org.bocogop.shared.service.VelocityService;
import org.bocogop.shared.service.validation.ServiceValidationException;
import org.bocogop.shared.util.SecurityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
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
import org.springframework.web.util.WebUtils;

@Controller
@SessionAttributes(value = { "command" })
public class KioskController extends AbstractKioskController {
	private static final Logger log = LoggerFactory.getLogger(KioskController.class);

	public static final String SESSION_ATTR_EVENT_ID = "eventId";
	public static final String COOKIE_EVENT_ID = "eventId";
	private static final String VIEW_EVENT_CHANGE = "eventChange";
	public static final String BREADCRUMB_HOME = "Home";
	public static final String URL_HOME = "/home.htm";
	public static final String VIEW_HOME = "home";

	@Autowired
	private VelocityService velocityService;
	@Autowired
	private AuthenticationManager authenticationManager;

	// @Autowired
	// private LocaleResolver localeResolver;
	@Autowired
	private VoterValidator voterValidator;

	private boolean isEventManager(HttpServletRequest r) {
		HttpServletRequestWrapper requestWrapper = WebUtils.getNativeRequest(r, HttpServletRequestWrapper.class);
		if (requestWrapper != null) {
			HttpServletRequest sr = (HttpServletRequest) requestWrapper.getRequest();
			return sr.isUserInRole("BOCOGOPEventManager");
		}
		return false;
	}

	@RequestMapping(value = URI_LOGIN, method = RequestMethod.GET)
	public String loginPage(@RequestParam(required = false) String error, @RequestParam(required = false) Long eventId,
			@RequestParam(required = false) Boolean noUserFound,
			@CookieValue(required = false, name = COOKIE_EVENT_ID) Long cookieEventId, ModelMap model,
			HttpServletRequest request, HttpServletResponse response) {
		String locale = LocaleContextHolder.getLocale().getLanguage();

		// if we didn't specify an event in the URL
		if (eventId == null) {
			/*
			 * look up the cookie.
			 */
			if (cookieEventId == null) {
				/*
				 * If the cookie is missing, send them to the selectEvent page.
				 */
				return "redirect:/selectEvent.htm";
			} else {
				/*
				 * We have a cookie - make sure it still points to a valid event
				 */
				if (eventDAO.findByPrimaryKey(cookieEventId) == null) {
					return "eventMissing";
				} else {
					/*
					 * send the user back to this same page with the event ID
					 * explicitly provided
					 */
					return "redirect:/login.htm?eventId=" + cookieEventId //
							+ (error != null ? "&error=" + error : "") //
							+ (noUserFound != null ? "&noUserFound=" + noUserFound : "");
				}
			}
		} else {
			/* We have an event ID explicitly stated. */
			if (cookieEventId == null || (!cookieEventId.equals(eventId))) {
				/*
				 * Check permissions to set the cookie. This isn't "real"
				 * security since they could just spoof the cookie but assuming
				 * a real kiosk would be locked down and filesystem cookies
				 * inaccessible.
				 */
				if (!isEventManager(request))
					throw new SecurityException("Please have a BOCOGOP administrator select the event.");

				/*
				 * Save a cookie so that we don't have to pass it thru the
				 * spring login page, and so that we can retrieve it if they log
				 * out. - CPB
				 */
				response.addCookie(getCookie(eventId));
			}
		}

		Event event = eventDAO.findByPrimaryKey(eventId);
		if (event == null) {
			return "eventMissing";
		}
		/*
		 * Once we're logged in, we rely on the event context, but on the login
		 * page we don't have a session context yet, so set it manually
		 */
		model.put("event", event);

		model.put("isEventManager", isEventManager(request));

		if (StringUtils.isNotBlank(error)) {
			model.addAttribute("errorMessage",
					velocityService.mergeTemplateIntoString("login.error." + error + "." + locale));
		}

		if (noUserFound != null && noUserFound) {
			model.addAttribute("noUserFound", true);
		}

		model.addAttribute("globalIntroText",
				velocityService.mergeTemplateIntoString("kiosk.globalIntroText" + "." + locale));
		return "login";
	}

	public Cookie getCookie(Long eventId) {
		Cookie cookie = new Cookie(COOKIE_EVENT_ID, String.valueOf(eventId));
		cookie.setHttpOnly(true);
		// 10 years ought to do it
		cookie.setMaxAge(60 * 60 * 24 * 365 * 10);
		return cookie;
	}

	@RequestMapping(value = WebSecurityConfig.URI_LOGOUT, method = RequestMethod.GET)
	public String logoutPage(@RequestParam(required = false) Boolean noUserFound, HttpServletRequest request,
			HttpServletResponse response) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth != null) {
			new SecurityContextLogoutHandler().logout(request, response, auth);
		}

		return "redirect:/login.htm?logout" + (noUserFound != null && noUserFound ? "&noUserFound=true" : "");
	}

	@RequestMapping("/index.htm")
	public String index(ModelMap model, HttpServletRequest request, HttpServletResponse response, HttpSession session,
			@CookieValue(name = COOKIE_EVENT_ID) long cookieEventId) {
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
	public String refineUser(ModelMap model, @RequestParam(required = false) Long id, HttpServletRequest request,
			HttpServletResponse response) {
		MultiVoterTempUserDetails multiMatch = SecurityUtil.getCurrentUserAsOrNull(MultiVoterTempUserDetails.class);
		if (multiMatch == null)
			return "redirect:/index.htm";
		if (id != null) {

			if (id.equals(-1L)) {
				Authentication auth = SecurityContextHolder.getContext().getAuthentication();
				if (auth != null) {
					new SecurityContextLogoutHandler().logout(request, response, auth);
				}
				return "redirect:/login.htm?logout&noUserFound=true";
			}

			Voter match = multiMatch.getMatches().stream().filter(p -> p.getId().equals(id)).findFirst().orElse(null);
			if (match != null) {
				Authentication auth = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
						match.getVoterId() + "||", String.valueOf(match.getBirthYear())));
				SecurityContextHolder.getContext().setAuthentication(auth);
				return "redirect:" + URL_HOME;
			} else
				throw new IllegalArgumentException("The specified ID is invalid");
		}

		model.put("multiMatch", multiMatch);
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
	public String home(ModelMap model, @RequestParam(required = false) Long id) {
		Voter v = id == null ? getCurrentUser() : voterDAO.findRequiredByPrimaryKey(id);
		model.put("voter", v);

		participationService.logParticipation(v.getId(), getRequiredEventContextId());

		String locale = LocaleContextHolder.getLocale().getLanguage();
		model.put("homepageContent", velocityService
				.mergeTemplateIntoString("kiosk." + TemplateType.HOMEPAGE_CONTENT.getName() + "." + locale));
		model.put("homepageAnnouncement", velocityService
				.mergeTemplateIntoString("kiosk." + TemplateType.HOMEPAGE_ANNOUNCEMENT.getName() + "." + locale));

		// Already a station in session. Do nothing, redirect to sitemap.
		return VIEW_HOME;
	}

	@RequestMapping(value = "/selectEvent.htm", method = RequestMethod.GET)
	public String selectEvent(ModelMap model, HttpServletRequest request, HttpServletResponse response) {
		eraseCookie(COOKIE_EVENT_ID, request, response);
		if (!isEventManager(request)) {
			throw new SecurityException("Please login as an event manager to select an event.");
		}

		model.put("eventList", eventDAO.findAllSorted());
		model.put("cancelAllowed", false);
		return VIEW_EVENT_CHANGE;
	}

	@RequestMapping("/voterEdit.htm")
	public String voterEdit(ModelMap model, HttpServletRequest request, HttpSession session) {
		Long voterId = getCurrentUser().getId();
		Voter voter = voterDAO.findRequiredByPrimaryKey(voterId);

		if (StringUtils.isBlank(voter.getUserProvidedEmail()))
			voter.setUserProvidedEmail(voter.getEmail());
		if (StringUtils.isBlank(voter.getUserProvidedPhone()))
			voter.setUserProvidedPhone(voter.getPhone());
		if (StringUtils.isBlank(voter.getNickname()))
			voter.setNickname(voter.getFirstName());

		VoterCommand command = new VoterCommand(voter);
		model.put(DEFAULT_COMMAND_NAME, command);
		createReferenceData(model);

		return "editVoter";
	}

	private void createReferenceData(ModelMap model) {
		model.put("allGenders", genderDAO.findAllSorted());
		List<State> allStates = stateDAO.findAllSortedByCountry();
		model.put("allStates", allStates);
		model.put("stateMap", allStates.stream().collect(Collectors.toMap(a -> a.getCode(), a -> a)));
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
				voter = voterService.saveOrUpdate(voter);
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
			return "redirect:" + URL_HOME + "?id=" + voter.getId();
		}
	}

}
