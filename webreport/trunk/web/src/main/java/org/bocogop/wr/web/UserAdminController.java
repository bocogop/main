package org.bocogop.wr.web;

import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
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
import org.bocogop.shared.model.AppUserRole;
import org.bocogop.shared.model.Permission.PermissionType;
import org.bocogop.shared.model.Role;
import org.bocogop.shared.model.Role.RoleType;
import org.bocogop.shared.persistence.AppUserDAO;
import org.bocogop.shared.persistence.AppUserDAO.QuickSearchResult;
import org.bocogop.shared.persistence.dao.RoleDAO;
import org.bocogop.shared.service.AppUserService;
import org.bocogop.shared.service.validation.ServiceValidationException;
import org.bocogop.shared.util.SecurityUtil;
import org.bocogop.shared.util.StringUtil;
import org.bocogop.shared.util.TimeZoneUtils;
import org.bocogop.shared.web.CoreAjaxRequestHandler;
import org.bocogop.wr.web.breadcrumbs.Breadcrumb;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
	private AppUserService appUserService;
	@Autowired
	private RoleDAO roleDAO;
	@Autowired
	private CoreAjaxRequestHandler coreAjaxRequestHandler;

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

	@RequestMapping("/userList.htm")
	@Breadcrumb("User List")
	public String userList(ModelMap model) {
		return "userList";
	}

	private boolean hasUMPermission() {
		return SecurityUtil.hasAllPermissions(PermissionType.USER_MANAGER);
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

	@RequestMapping(value = "/appUser/list", method = RequestMethod.GET)
	@JsonView(AppUserView.List.class)
	public @ResponseBody SortedSet<AppUser> getAppUserListWithRoles() {
		return appUserDAO.listAllWithRoles();
	}

	@RequestMapping(value = "/appUser", params = "includeRoles=true", method = RequestMethod.GET)
	@JsonView(AppUserView.Extended.class)
	public @ResponseBody Map<String, Object> getExtendedAppUserInfo(@RequestParam(required = false) Long userId,
			@RequestParam(required = false) String username) {
		return getAppUserInfo(userId, username, true);
	}

	@RequestMapping(value = "/appUser", params = "includeRoles=false", method = RequestMethod.GET)
	@JsonView(AppUserView.Basic.class)
	public @ResponseBody Map<String, Object> getBasicAppUserInfo(@RequestParam(required = false) Long userId,
			@RequestParam(required = false) String username) {
		return getAppUserInfo(userId, username, false);
	}

	@RequestMapping(value = "/appUser", params = "!includeRoles", method = RequestMethod.GET)
	@JsonView(AppUserView.Basic.class)
	public @ResponseBody Map<String, Object> getBasicAppUserInfoWithoutExtras(
			@RequestParam(required = false) Long userId, @RequestParam(required = false) String username) {
		return getAppUserInfo(userId, username, false);
	}

	private Map<String, Object> getAppUserInfo(Long userId, String username, boolean includeRoles) {
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
		results.put("updateRoles", includeRoles);

		if (includeRoles) {
			SortedSet<Role> availableRoles = roleDAO.findAllSorted(true);
			Set<AppUserRole> roles = user.getRoles();
			for (AppUserRole augr : roles) {
				Role role = augr.getRole();
				availableRoles.remove(role);
			}

			availableRoles.remove(roleDAO.findByLookup(RoleType.VOTER));

			// TODO BOCOGOP
			// if (!u.isNationalAdmin()) {
			// }
			results.put("availableRoles", availableRoles);
		}

		return results;
	}

	@RequestMapping(value = "/appUser/update", method = RequestMethod.POST)
	// @PreAuthorize("hasAuthority('" + Permission.VOTER_VIEW + "')")
	public @ResponseBody boolean processUserUpdate(@RequestParam long userId, @RequestParam boolean enabled,
			@RequestParam ZoneId timezone, @RequestParam(required = false, defaultValue = "") List<Long> roles,
			@RequestParam boolean updateRoles) throws ServiceValidationException {
		ensureUserAccess(userId, null);

		AppUser user = appUserDAO.findRequiredByPrimaryKey(userId);

		appUserService.updateUser(userId, enabled != user.isEnabled() ? enabled : null, timezone, updateRoles, roles);
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

	@RequestMapping("/appUser/saveOrUpdate")
	public @ResponseBody AppUser addAppUser(@RequestParam(required = false) Long userId, @RequestParam String username,
			@RequestParam String firstName, @RequestParam String lastName, @RequestParam String phone,
			@RequestParam String email, @RequestParam String description, @RequestParam String passwordReset,
			@RequestParam String passwordResetConfirm) throws ServiceValidationException {
		AppUser u = userId == null ? new AppUser() : appUserDAO.findRequiredByPrimaryKey(userId);
		u.setUsername(username);
		u.setFirstName(firstName);
		u.setLastName(lastName);
		u.setPhone(phone);
		u.setEmail(email);
		u.setDescription(description);
		u = appUserService.saveOrUpdate(u, passwordReset, passwordResetConfirm);
		return u;
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

}
