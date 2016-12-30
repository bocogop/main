package org.bocogop.shared.web;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.bocogop.shared.model.AppUser;
import org.bocogop.shared.model.AppUser.AppUserView;
import org.bocogop.shared.model.AppUserGlobalRole;
import org.bocogop.shared.model.AppUserPrecinct;
import org.bocogop.shared.model.Permission.PermissionType;
import org.bocogop.shared.model.Role;
import org.bocogop.shared.persistence.AppUserDAO;
import org.bocogop.shared.persistence.AppUserDAO.QuickSearchResult;
import org.bocogop.shared.persistence.AppUserPrecinctDAO;
import org.bocogop.shared.persistence.lookup.RoleDAO;
import org.bocogop.shared.service.AppUserService;
import org.bocogop.shared.service.UserAdminCustomizations;
import org.bocogop.shared.service.validation.ServiceValidationException;
import org.bocogop.shared.util.PersistenceUtil;
import org.bocogop.shared.util.SecurityUtil;
import org.bocogop.shared.util.StringUtil;
import org.bocogop.shared.util.TimeZoneUtils;
import org.bocogop.wr.model.precinct.Precinct;
import org.bocogop.wr.persistence.dao.precinct.PrecinctDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.fasterxml.jackson.annotation.JsonView;

@Controller
public class UserAdminController {
	private static final Logger log = LoggerFactory.getLogger(UserAdminController.class);

	@Autowired
	private AppUserDAO appUserDAO;
	@Autowired
	private AppUserPrecinctDAO appUserPrecinctDAO;
	@Autowired
	private AppUserService appUserService;
	@Autowired
	private RoleDAO roleDAO;
	@Autowired
	private PrecinctDAO precinctDAO;

	/* Framework injection, if desired - CPB */
	@Autowired(required = false)
	private UserAdminCustomizations userAdminCustomizations;
	@Autowired
	private CoreAjaxRequestHandler coreAjaxRequestHandler;
	@Value("${ldapIgnoreConnectivityErrors}")
	private boolean ignoreConnectivityErrors;

	@ExceptionHandler(Throwable.class)
	public ModelAndView processError(Throwable ex, HttpServletRequest request, HttpServletResponse response) {
		if (CoreAjaxRequestHandler.isAjax(request)) {
			/*
			 * Necessary to trigger the jQuery error() handler as opposed to the
			 * success() handler - CPB
			 */
			response.setStatus(HttpStatus.BAD_REQUEST.value());
			return coreAjaxRequestHandler.getExceptionModelAndView(ex, request);
		} else {
			return new ModelAndView("error", "exceptionStackTrace", ExceptionUtils.getStackTrace(ex));
		}
	}

	@RequestMapping(value = "/userAdmin.htm", method = RequestMethod.GET)
	public String userAdmin(ModelMap modelMap) {
		modelMap.put("allTimeZones", TimeZoneUtils.TIME_ZONES);
		return "userAdmin";
	}

	private boolean hasUMPermission() {
		return SecurityUtil.hasAllPermissionsAtCurrentPrecinct(PermissionType.USER_MANAGER);
	}

	private void ensureUserAccess(Long userId, String username) {
		if (userId != null) {
			long myUserId = SecurityUtil.getCurrentUser().getId();
			if (!hasUMPermission() && userId != myUserId)
				throw new AccessDeniedException("The user with the specified ID is not available");
		}

		if (username != null) {
			String myUsername = SecurityUtil.getCurrentUserName();
			if (!hasUMPermission() && !myUsername.equals(username))
				throw new AccessDeniedException("The user with the specified username is not available");
		}
	}

	@RequestMapping(value = "/appUser", params = "includeRolesAndPrecincts=true", method = RequestMethod.GET)
	@JsonView(AppUserView.Extended.class)
	public @ResponseBody Map<String, Object> getExtendedAppUserInfo(@RequestParam(required = false) Long userId,
			@RequestParam(required = false) String username) {
		return getAppUserInfo(userId, username, true);
	}

	@RequestMapping(value = "/appUser", params = "includeRolesAndPrecincts=false", method = RequestMethod.GET)
	@JsonView(AppUserView.Basic.class)
	public @ResponseBody Map<String, Object> getBasicAppUserInfo(@RequestParam(required = false) Long userId,
			@RequestParam(required = false) String username) {
		return getAppUserInfo(userId, username, false);
	}

	@RequestMapping(value = "/appUser", params = "!includeRolesAndPrecincts", method = RequestMethod.GET)
	@JsonView(AppUserView.Basic.class)
	public @ResponseBody Map<String, Object> getBasicAppUserInfoWithoutExtras(
			@RequestParam(required = false) Long userId, @RequestParam(required = false) String username) {
		return getAppUserInfo(userId, username, false);
	}

	private Map<String, Object> getAppUserInfo(Long userId, String username, boolean includeRolesAndPrecincts) {
		ensureUserAccess(userId, username);
		AppUser u = SecurityUtil.getCurrentUserAs(AppUser.class);

		Map<String, Object> results = new HashMap<>();

		AppUser user = null;
		if (userId != null) {
			user = appUserDAO.findRequiredByPrimaryKey(userId);
		} else if (username != null) {
			user = appUserDAO.findByUsername(username, false);
		} else {
			throw new IllegalArgumentException("Either the userId or the username must be specified");
		}

		results.put("user", user);
		results.put("updateRolesAndPrecincts", includeRolesAndPrecincts);

		Precinct primaryPrecinct = null;

		if (includeRolesAndPrecincts) {
			SortedSet<Precinct> availablePrecincts = new TreeSet<>();

			SortedSet<Precinct> precincts = userAdminCustomizations.getAssignablePrecincts();
			if (precincts == null)
				precincts = precinctDAO.findAllSorted();

			availablePrecincts.addAll(precincts);

			List<AppUserPrecinct> appUserPrecinctList = appUserPrecinctDAO.findByUserSorted(user.getId());
			for (AppUserPrecinct precinct : appUserPrecinctList) {
				if (precinct.isPrimaryPrecinct())
					primaryPrecinct = precinct.getPrecinct();
				availablePrecincts.remove(precinct.getPrecinct());
			}

			if (!u.isNationalAdmin())
				availablePrecincts.retainAll(u.getAssignedPrecincts());
			results.put("availablePrecincts", availablePrecincts);

			results.put("appUserPrecincts", appUserPrecinctList);

			SortedSet<Role> availableRoles = roleDAO.findAllSorted(true);
			Set<AppUserGlobalRole> globalRoles = user.getGlobalRoles();
			for (AppUserGlobalRole augr : globalRoles) {
				Role role = augr.getRole();
				availableRoles.remove(role);
			}

			// TODO BOCOGOP
			// if (!u.isNationalAdmin()) {
			// }
			results.put("availableRoles", availableRoles);

			populateModelForSummaryTable(user, results);
		} else {
			primaryPrecinct = appUserPrecinctDAO.findPrimaryPrecinctForUser(user.getId());
		}
		results.put("defaultPrecinct", primaryPrecinct);

		return results;
	}

	private void populateModelForSummaryTable(AppUser user, Map<String, Object> results) {
		SortedSet<Role> allRoles = new TreeSet<>();
		allRoles.addAll(user.getBasicGlobalRoles());

		Map<Long, Boolean> falseRoleMap = new HashMap<>();
		Map<Long, Role> roleInfoMap = new LinkedHashMap<>();
		for (Role r : allRoles) {
			falseRoleMap.put(r.getId(), false);
			roleInfoMap.put(r.getId(), r);
		}

		List<StationAndRoles> stationAndRoles = new ArrayList<>();

		for (Precinct f : user.getAssignedPrecincts()) {
			Long precinctId = f.getId();
			Map<Long, Boolean> roleMap = new HashMap<>(falseRoleMap);

			stationAndRoles.add(new StationAndRoles(f.getId(), roleMap));
		}
		results.put("stationAndRoles", stationAndRoles);
		results.put("roleInfoMap", roleInfoMap);
	}

	@RequestMapping(value = "/appUser/update", method = RequestMethod.POST)
	// @PreAuthorize("hasAuthority('" + Permission.VOTER_VIEW + "')")
	public @ResponseBody boolean processUserUpdate(@RequestParam long userId, @RequestParam boolean enabled,
			@RequestParam boolean expired, @RequestParam boolean locked, @RequestParam ZoneId timezone,
			@RequestParam(required = false, defaultValue = "") List<Long> globalRoles,
			@RequestParam(required = false, defaultValue = "") List<Long> precinctsToAdd,
			@RequestParam(required = false, defaultValue = "") List<Long> precinctsToRemove,
			@RequestParam Long defaultPrecinctId, @RequestParam boolean updateRolesAndPrecincts)
			throws ServiceValidationException {
		ensureUserAccess(userId, null);

		AppUser user = appUserDAO.findRequiredByPrimaryKey(userId);

		Set<Long> precincts = null;
		if (updateRolesAndPrecincts) {
			precincts = new HashSet<>(PersistenceUtil.translateObjectsToIds(user.getAssignedPrecincts()));
			for (Long l : precinctsToRemove)
				precincts.remove(l);
			for (Long l : precinctsToAdd)
				precincts.add(l);
		}

		appUserService.updateUser(userId, enabled != user.isEnabled() ? enabled : null,
				locked != user.isLocked() ? locked : null, expired != user.isAccountExpired() ? expired : null,
				timezone, updateRolesAndPrecincts, defaultPrecinctId, globalRoles, precincts);
		return true;
	}

	@RequestMapping("/appUser/find")
	// @PreAuthorize("hasAuthority('" + Permission.VOTER_VIEW + "')")
	public @ResponseBody Collection<AppUser> findAppUsers(@RequestParam(required = false) String name,
			@RequestParam(required = false) String activeDirectoryName, @RequestParam boolean includeLDAP,
			@RequestParam boolean includeLocalDB) {
		if (StringUtils.isBlank(name) && StringUtils.isBlank(activeDirectoryName))
			throw new IllegalArgumentException("Please specify at least one piece of search criteria");

		if (StringUtils.isNotBlank(activeDirectoryName))
			activeDirectoryName = activeDirectoryName.toLowerCase();

		SortedMap<String, AppUser> results = new TreeMap<>();

		if (includeLocalDB) {
			Collection<AppUser> searchResults = null;
			if (StringUtils.isNotBlank(activeDirectoryName)) {
				List<String> usernames = StringUtils.isNotBlank(activeDirectoryName)
						? Arrays.asList(activeDirectoryName) : null;

				searchResults = appUserDAO.findByCriteria(usernames, null, false, null, false, null, false);
			} else if (StringUtils.isNotBlank(name)) {
				String[] nameComponents = StringUtil.parseNameComponents(name);
				String lastName = nameComponents[0];
				String firstName = nameComponents[1];
				searchResults = appUserDAO.findByCriteria(null, null, false, lastName, true, firstName, true);
			}

			if (searchResults != null)
				for (AppUser u : searchResults) {
					results.put(StringUtils.lowerCase(u.getUsername()), u);
				}
		}

		return results.values();
	}

	/**
	 * Powers the jQuery user selection table - CPB
	 * 
	 * @param draw
	 * @param start
	 * @param length
	 * @param searchValue
	 * @param searchIsRegex
	 * @param allParams
	 * @return
	 */
	@RequestMapping("/appUser/quickSearch")
	public @ResponseBody Map<String, Object> findAppUserByNameOrUsername(@RequestParam int draw,
			@RequestParam int start, @RequestParam int length, @RequestParam(name = "search[value]") String searchValue,
			@RequestParam(name = "search[regex]") boolean searchIsRegex) {
		Map<String, Object> resultMap = new HashMap<>();

		SortedSet<QuickSearchResult> results = null;
		if (StringUtils.isNotBlank(searchValue)) {
			results = new TreeSet<>(appUserDAO.findByNameOrUsername(searchValue, length));
		} else {
			results = new TreeSet<>();
		}
		resultMap.put("data", results);

		resultMap.put("draw", draw);

		return resultMap;
	}

	@RequestMapping("/appUser/add")
	public @ResponseBody AppUser addAppUser(@RequestParam String activeDirectoryName) {
		if (StringUtils.isBlank(activeDirectoryName))
			throw new IllegalArgumentException("Active directory name is required");

		AppUser appUser = appUserService.createOrRetrieveUser(activeDirectoryName, null);
		return appUser;
	}

	@RequestMapping("/appUser/remove")
	public @ResponseBody boolean removeAppUser(@RequestParam long appUserId) {
		try {
			appUserService.removeUser(appUserId, null);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public static class StationAndRoles {
		public long precinctId;
		public Map<Long, Boolean> roleMap;

		public StationAndRoles(long precinctId, Map<Long, Boolean> roleMap) {
			this.precinctId = precinctId;
			this.roleMap = roleMap;
		}
	}
}
