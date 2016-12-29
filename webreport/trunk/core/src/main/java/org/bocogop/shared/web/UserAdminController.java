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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.ldap.CommunicationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.fasterxml.jackson.annotation.JsonView;

import org.bocogop.shared.model.AppUser;
import org.bocogop.shared.model.AppUser.AppUserView;
import org.bocogop.shared.model.AppUserFacility;
import org.bocogop.shared.model.AppUserGlobalRole;
import org.bocogop.shared.model.Permission;
import org.bocogop.shared.model.Permission.PermissionType;
import org.bocogop.shared.model.Role;
import org.bocogop.shared.model.ldap.LdapPerson;
import org.bocogop.shared.model.lookup.sds.VAFacility;
import org.bocogop.shared.persistence.AppUserDAO;
import org.bocogop.shared.persistence.AppUserDAO.QuickSearchResult;
import org.bocogop.shared.persistence.AppUserFacilityDAO;
import org.bocogop.shared.persistence.GrantableRoleDAO;
import org.bocogop.shared.persistence.LdapPersonDAO;
import org.bocogop.shared.persistence.lookup.RoleDAO;
import org.bocogop.shared.persistence.lookup.sds.VAFacilityDAO;
import org.bocogop.shared.service.AppUserService;
import org.bocogop.shared.service.UserAdminCustomizations;
import org.bocogop.shared.service.validation.ServiceValidationException;
import org.bocogop.shared.util.PersistenceUtil;
import org.bocogop.shared.util.SecurityUtil;
import org.bocogop.shared.util.StringUtil;
import org.bocogop.shared.util.TimeZoneUtils;

@Controller
public class UserAdminController {
	private static final Logger log = LoggerFactory.getLogger(UserAdminController.class);

	@Autowired
	private AppUserDAO appUserDAO;
	@Autowired
	private AppUserFacilityDAO appUserFacilityDAO;
	@Autowired
	private AppUserService appUserService;
	@Autowired
	private GrantableRoleDAO grantableRoleDAO;
	@Autowired
	private LdapPersonDAO ldapPersonDAO;
	@Autowired
	private RoleDAO roleDAO;
	@Autowired
	private VAFacilityDAO vaFacilityDAO;

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
		return SecurityUtil.hasAllPermissionsAtCurrentFacility(PermissionType.USER_MANAGER);
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

	@RequestMapping(value = "/appUser", params = "includeRolesAndFacilities=true", method = RequestMethod.GET)
	@JsonView(AppUserView.Extended.class)
	public @ResponseBody Map<String, Object> getExtendedAppUserInfo(@RequestParam(required = false) Long userId,
			@RequestParam(required = false) String username) {
		return getAppUserInfo(userId, username, true);
	}

	@RequestMapping(value = "/appUser", params = "includeRolesAndFacilities=false", method = RequestMethod.GET)
	@JsonView(AppUserView.Basic.class)
	public @ResponseBody Map<String, Object> getBasicAppUserInfo(@RequestParam(required = false) Long userId,
			@RequestParam(required = false) String username) {
		return getAppUserInfo(userId, username, false);
	}

	@RequestMapping(value = "/appUser", params = "!includeRolesAndFacilities", method = RequestMethod.GET)
	@JsonView(AppUserView.Basic.class)
	public @ResponseBody Map<String, Object> getBasicAppUserInfoWithoutExtras(
			@RequestParam(required = false) Long userId, @RequestParam(required = false) String username) {
		return getAppUserInfo(userId, username, false);
	}

	private Map<String, Object> getAppUserInfo(Long userId, String username, boolean includeRolesAndFacilities) {
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
		results.put("updateRolesAndFacilities", includeRolesAndFacilities);

		VAFacility primaryFacility = null;

		if (includeRolesAndFacilities) {
			SortedSet<VAFacility> availableFacilities = new TreeSet<>();

			SortedSet<VAFacility> facilities = userAdminCustomizations.getAssignableFacilities();
			if (facilities == null)
				facilities = vaFacilityDAO.findAllSorted();

			availableFacilities.addAll(facilities);

			List<AppUserFacility> appUserFacilityList = appUserFacilityDAO.findByUserSorted(user.getId());
			for (AppUserFacility facility : appUserFacilityList) {
				if (facility.isPrimaryFacility())
					primaryFacility = facility.getFacility();
				availableFacilities.remove(facility.getFacility());
			}

			if (!u.isNationalAdmin())
				availableFacilities.retainAll(u.getAssignedVAFacilities());
			results.put("availableFacilities", availableFacilities);

			results.put("appUserFacilities", appUserFacilityList);

			SortedSet<Role> availableRoles = roleDAO.findAllSorted(true);
			Set<AppUserGlobalRole> globalRoles = user.getGlobalRoles();
			for (AppUserGlobalRole augr : globalRoles) {
				Role role = augr.getRole();
				availableRoles.remove(role);
			}

			if (!u.isNationalAdmin()) {
				List<Role> allGrantableRoles = grantableRoleDAO.findAllGrantableRolesForUser(u.getId());
				availableRoles.retainAll(allGrantableRoles);
			}
			results.put("availableRoles", availableRoles);

			populateModelForSummaryTable(user, results);
		} else {
			primaryFacility = appUserFacilityDAO.findPrimaryFacilityForUser(user.getId());
		}
		results.put("defaultFacility", primaryFacility);

		return results;
	}

	private void populateModelForSummaryTable(AppUser user, Map<String, Object> results) {
		SortedSet<Role> allRoles = new TreeSet<>();
		allRoles.addAll(user.getBasicGlobalRoles());
		for (AppUserFacility f : user.getFacilities())
			allRoles.addAll(f.getRoles());

		Map<Long, Boolean> falseRoleMap = new HashMap<>();
		Map<Long, Role> roleInfoMap = new LinkedHashMap<>();
		for (Role r : allRoles) {
			falseRoleMap.put(r.getId(), false);
			roleInfoMap.put(r.getId(), r);
		}

		List<StationAndRoles> stationAndRoles = new ArrayList<>();

		for (VAFacility f : user.getAssignedVAFacilities()) {
			Long vaFacilityId = f.getId();
			SortedSet<Role> rolesForFacility = user.getRolesForFacility(vaFacilityId);
			Map<Long, Boolean> roleMap = new HashMap<>(falseRoleMap);

			for (Role r : rolesForFacility)
				roleMap.put(r.getId(), true);
			stationAndRoles.add(new StationAndRoles(f.getId(), roleMap));
		}
		results.put("stationAndRoles", stationAndRoles);
		results.put("roleInfoMap", roleInfoMap);
	}

	@RequestMapping(value = "/appUser/update", method = RequestMethod.POST)
	// @PreAuthorize("hasAuthority('" + Permission.VOLUNTEER_VIEW + "')")
	public @ResponseBody boolean processUserUpdate(@RequestParam long userId, @RequestParam boolean enabled,
			@RequestParam boolean expired, @RequestParam boolean locked, @RequestParam ZoneId timezone,
			@RequestParam(required = false, defaultValue = "") List<Long> globalRoles,
			@RequestParam(required = false, defaultValue = "") List<Long> vaFacilitiesToAdd,
			@RequestParam(required = false, defaultValue = "") List<Long> vaFacilitiesToRemove,
			@RequestParam Long defaultFacilityId, @RequestParam boolean updateRolesAndFacilities)
			throws ServiceValidationException {
		ensureUserAccess(userId, null);

		AppUser user = appUserDAO.findRequiredByPrimaryKey(userId);

		Set<Long> vaFacilities = null;
		if (updateRolesAndFacilities) {
			vaFacilities = new HashSet<>(PersistenceUtil.translateObjectsToIds(user.getAssignedVAFacilities()));
			for (Long l : vaFacilitiesToRemove)
				vaFacilities.remove(l);
			for (Long l : vaFacilitiesToAdd)
				vaFacilities.add(l);
		}

		appUserService.updateUser(userId, enabled != user.isEnabled() ? enabled : null,
				locked != user.isLocked() ? locked : null, expired != user.isAccountExpired() ? expired : null,
				timezone, updateRolesAndFacilities, defaultFacilityId, globalRoles, vaFacilities);
		return true;
	}

	@RequestMapping(value = "/appUser/customize", method = RequestMethod.POST)
	@PreAuthorize("hasAuthority('" + Permission.USER_MANAGER + "')")
	public @ResponseBody boolean processUserCustomize(@RequestParam long userId,
			@RequestParam(required = false, defaultValue = "") List<Long> roles,
			@RequestParam(required = false, defaultValue = "") List<Long> vaFacilities)
			throws ServiceValidationException {
		appUserService.customizeUser(userId, roles, vaFacilities);
		return true;
	}

	@RequestMapping("/appUser/find")
	// @PreAuthorize("hasAuthority('" + Permission.VOLUNTEER_VIEW + "')")
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

		if (includeLDAP) {
			try {
				if (StringUtils.isNotBlank(activeDirectoryName) && !results.containsKey(activeDirectoryName)) {
					LdapPerson person = ldapPersonDAO.findBySAMAccountName(activeDirectoryName);
					if (person != null) {
						AppUser appUser = new AppUser(person);
						results.put(activeDirectoryName, appUser);
					}
				} else if (StringUtils.isNotBlank(name)) {
					String[] nameComponents = StringUtil.parseNameComponents(name);
					String lastName = nameComponents[0];
					boolean wildcardLastName = lastName.contains("*");
					lastName = lastName.replaceAll("\\W", "");

					String firstName = nameComponents[1];
					boolean wildcardFirstName = firstName.contains("*");
					firstName = firstName.replaceAll("\\W", "");

					List<LdapPerson> ldapResults = ldapPersonDAO.findByName(firstName, wildcardFirstName, lastName,
							wildcardLastName);
					for (LdapPerson p : ldapResults) {
						AppUser value = new AppUser(p);
						if (results.containsKey(StringUtils.lowerCase(value.getUsername())))
							continue;
						results.put(StringUtils.lowerCase(value.getUsername()), value);
					}
				}
			} catch (CommunicationException e) {
				if (ignoreConnectivityErrors) {
					log.warn("LDAP offline; restricting user searches to database only");
				} else
					throw e;
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
		public long facilityId;
		public Map<Long, Boolean> roleMap;

		public StationAndRoles(long facilityId, Map<Long, Boolean> roleMap) {
			this.facilityId = facilityId;
			this.roleMap = roleMap;
		}
	}
}
