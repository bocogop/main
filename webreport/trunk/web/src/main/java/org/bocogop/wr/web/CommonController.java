package org.bocogop.wr.web;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.annotation.JsonView;

import org.bocogop.shared.model.AppUser;
import org.bocogop.shared.model.Permission.PermissionType;
import org.bocogop.shared.model.lookup.sds.VAFacility;
import org.bocogop.shared.persistence.impl.AbstractAppDAOImpl;
import org.bocogop.shared.persistence.lookup.sds.VAFacilityDAO;
import org.bocogop.shared.service.validation.ServiceValidationException;
import org.bocogop.wr.config.WebSecurityConfig;
import org.bocogop.wr.model.donation.DonationLog;
import org.bocogop.wr.model.facility.Facility;
import org.bocogop.wr.model.lookup.TemplateType;
import org.bocogop.wr.model.notification.Notification.NotificationView;
import org.bocogop.wr.service.NotificationService.NotificationSearchResult;
import org.bocogop.wr.service.VelocityService;
import org.bocogop.wr.util.DateUtil;
import org.bocogop.wr.web.breadcrumbs.Breadcrumb;

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
		AppUser au = getCurrentUser();
		Facility facilityContext = getFacilityContext();
		if (au != null && facilityContext == null)
			return "redirect:/selectStation.htm";

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
		AppUser user = getCurrentUser();
		appUserService.logApplicationAccess(user.getUsername(), ZonedDateTime.now());
		resetSiteContextIfNecessary();
		VAFacility dutyStationToSet = getSiteContext();
		log.debug("index - found duty station was {}", dutyStationToSet);

		if (dutyStationToSet == null) {
			if (user.getFacilities().size() > 1) {
				log.debug("user has multiple facilities");
				VAFacility primaryFacility = appUserFacilityDAO.findPrimaryFacilityForUser(user.getId());
				if (primaryFacility == null) {
					log.debug("redirecting to /selectStation.htm since no primary facility is selected");
					return "redirect:/selectStation.htm";
				} else {
					log.debug("auto-selecting primary facility {}", primaryFacility);
					setNewDutyStation(primaryFacility);
				}
			} else if (user.getFacilities().size() == 1) {
				VAFacility facility = user.getFacilities().iterator().next().getFacility();
				setNewDutyStation(facility);
				log.debug("set duty station to {}", facility);
			}
		}

		log.debug("redirecting to {}", URL_HOME);
		return "redirect:" + URL_HOME;
	}

	@RequestMapping(URL_HOME)
	@Breadcrumb(BREADCRUMB_HOME)
	public String home(ModelMap model) {
		if (getSiteContext() == null)
			return "redirect:/selectStation.htm";

		model.put("homepageContent", velocityService.mergeTemplateIntoString(TemplateType.HOMEPAGE_CONTENT.getName()));
		model.put("homepageAnnouncement",
				velocityService.mergeTemplateIntoString(TemplateType.HOMEPAGE_ANNOUNCEMENT.getName()));
		model.put("notificationMaxResults", notificationMaxResults);

		// Already a station in session. Do nothing, redirect to sitemap.
		return VIEW_HOME;
	}

	@RequestMapping("/notification")
	@JsonView(NotificationView.NotificationsForUser.class)
	public @ResponseBody Map<String, Object> getNotifications() {
		NotificationSearchResult searchResult = notificationService.getNotificationsForFacility(getFacilityContextId());
		Map<String, Object> results = new HashMap<>();
		results.put("notifications", searchResult.getNotifications());
		results.put("hitMaxResults", searchResult.isHitMaxResults());
		return results;
	}

	@RequestMapping("/notification/clear")
	@JsonView(NotificationView.NotificationsForUser.class)
	public @ResponseBody boolean clearNotification(long notificationId) throws ServiceValidationException {
		notificationService.delete(notificationId);
		return true;
	}

	/**
	 * Renew the session, so it will not timeout, ajax call
	 */
	@RequestMapping("/keepAlive")
	public @ResponseBody boolean keepAlive() {
		log.debug("Received request to keep-alive the session...");
		return true;
	}

	private void resetSiteContextIfNecessary() {
		VAFacility siteContext = getSiteContext();
		AppUser user = getCurrentUser();
		if (user == null || siteContext == null)
			return;

		if (!isFacilityAvailableToUser(user, siteContext)) {
			sessionUtil.setFacilityContext(null, null);
		}
	}

	private boolean isFacilityAvailableToUser(AppUser user, VAFacility newFacility) {
		return user.getAppUserFacility(newFacility.getId()) != null;
	}

	public static void populateFacilityList(AppUser user, ModelMap model, VAFacilityDAO vaFacilityDAO) {
		model.put("facilityList", user.getAssignedVAFacilities());
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

		VAFacility currentFacility = user.getLastVisitedFacility();
		if (currentFacility != null) {
			model.put("currentStationName", currentFacility.getName());
			model.put("currentStationId", currentFacility.getId());
		}

		populateFacilityList(user, model, vaFacilityDAO);
		if (currentFacility != null) {
			VAFacility visn = currentFacility.getVisn();
			if (visn != null && visn.getId() != null) {
				VAFacility attachedVisn = vaFacilityDAO.findRequiredByPrimaryKey(visn.getId());
				model.put("currentStationVisnName", attachedVisn.getDisplayName());
			}

			VAFacility parent = currentFacility.getParent();
			if (parent != null && parent.getId() != null) {
				VAFacility attachedParent = vaFacilityDAO.findRequiredByPrimaryKey(parent.getId());
				model.put("currentStationParentName", attachedParent.getDisplayName());
			}
		}
	}

	private void setNewDutyStation(VAFacility newDutyStation) {
		AppUser user = getCurrentUser();
		user.setLastVisitedFacility(newDutyStation);
		appUserService.updateFieldsWithoutVersionCheck(user.getId(), false, newDutyStation.getId(), false, null, null);
		Facility f = facilityDAO.findByVAFacility(newDutyStation.getId());
		sessionUtil.setFacilityContext(newDutyStation, f);
	}

	@RequestMapping(value = "/selectStation.htm", method = RequestMethod.POST)
	public String processSelectStation(@RequestParam Long stationCode) {
		VAFacility f = vaFacilityDAO.findByPrimaryKey(stationCode);
		setNewDutyStation(f);
		return "redirect:" + URL_HOME;
	}

	@RequestMapping("/updatePreferences")
	public @ResponseBody boolean updatePreferences(@RequestParam(required = false) Boolean soundsEnabled) {
		AppUser user = getCurrentUser();
		appUserService.updatePreferences(user.getId(), soundsEnabled);
		if (soundsEnabled != null)
			user.getPreferences().setSoundsEnabled(soundsEnabled);
		return true;
	}

	@RequestMapping("/getFacilitiesWithUserPermission")
	public @ResponseBody SortedSet<Facility> getFacilitiesWithUserPermission(@RequestParam PermissionType permission,
			@RequestParam(required = false) Boolean activeStatus) {
		Set<VAFacility> s = getCurrentUser().getFacilitiesWhereUserHasAllPermissions(permission);
		Map<Long, Facility> i = facilityDAO.findByVAFacilities(s);
		return i.values().stream().filter(p -> activeStatus == null || p.isActive() == activeStatus)
				.collect(Collectors.toCollection(TreeSet::new));
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

	// TODO move this to Jean's DonationLog controller once she checks it in -
	// CPB
	@RequestMapping("/donationLogImport")
	public @ResponseBody String donationLogImport(
			@RequestParam(required = false) @DateTimeFormat(pattern = DateUtil.DATE_ONLY) LocalDate reprocessDate)
			throws Exception {
		Map<LocalDate, List<DonationLog>> list = donationLogService.updateExternalDonations(reprocessDate);
		StringBuilder sb = new StringBuilder();
		sb.append("Donation log import report: ");
		for (Entry<LocalDate, List<DonationLog>> entry : list.entrySet()) {
			sb.append(entry.getKey().format(DateUtil.DATE_ONLY_FORMAT)).append(": ")
					.append(entry.getValue() != null ? entry.getValue().size() : "0").append(" donations imported; ");
		}
		return sb.toString();
	}

}
