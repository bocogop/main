package org.bocogop.shared.service.impl;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.bocogop.shared.model.AppUser;
import org.bocogop.shared.model.AppUserPreferences;
import org.bocogop.shared.model.AppUserRole;
import org.bocogop.shared.model.CoreUserDetails;
import org.bocogop.shared.model.Permission;
import org.bocogop.shared.model.Permission.PermissionType;
import org.bocogop.shared.model.Role;
import org.bocogop.shared.persistence.AppUserDAO;
import org.bocogop.shared.service.AbstractAppServiceImpl;
import org.bocogop.shared.service.AppUserService;
import org.bocogop.shared.service.validation.ServiceValidationException;
import org.bocogop.shared.util.CollectionUtil;
import org.bocogop.shared.util.CollectionUtil.SynchronizeCollectionsOps;
import org.bocogop.shared.util.SecurityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(rollbackFor = ServiceValidationException.class)
public class AppUserServiceImpl extends AbstractAppServiceImpl implements AppUserService {
	private static final Logger logger = LoggerFactory.getLogger(AppUserServiceImpl.class);

	@Autowired
	private PasswordEncoder passwordEncoder;
	@Value("${userAdmin.newUserDefaultTimezone}")
	private ZoneId newUserDefaultTimezone;

	@Override
	@PreAuthorize("hasAuthority('" + Permission.USER_MANAGER + "')")
	public AppUser saveOrUpdate(AppUser appUser) {
		if (appUser.isPersistent() == false) {
			appUser.setEnabled(true);
			appUser.setTimeZone(newUserDefaultTimezone);
		}

		appUser = appUserDAO.saveOrUpdate(appUser);
		appUser = populatePreferencesIfNecessary(appUserDAO, appUser);
		return appUser;
	}

	@Override
	public AppUser updateUser(long userId, Boolean enabled, ZoneId timezone, boolean updateRoles,
			Collection<Long> roles) throws ServiceValidationException {
		CoreUserDetails<?> currentUser = SecurityUtil.getCurrentUser();
		final AppUser user = appUserDAO.findRequiredByPrimaryKey(userId);

		boolean isEditingSelf = userId == currentUser.getId();
		boolean hasUMPermission = SecurityUtil.hasAllPermissions(PermissionType.USER_MANAGER);

		boolean updatedBasics = false;

		/* Can't disable/enable or lock/unlock yourself */
		if (!isEditingSelf && hasUMPermission) {
			if (enabled != null) {
				updatedBasics = true;
				user.setEnabled(enabled);
			}
		}

		/*
		 * If editing another user, require UM permission; otherwise, allow
		 * timezone for yourself to be updated
		 */
		if (timezone != null && (isEditingSelf || hasUMPermission)) {
			user.setTimeZone(timezone);
			updatedBasics = true;
		}

		AppUser updatedUser = user;
		if (updatedBasics) {
			updatedUser = saveOrUpdate(user);
			appUserDAO.flush();
		}

		boolean userRefreshNeeded = false;

		/*
		 * Only allow for role & station changes if the user has UM permission
		 */
		if (hasUMPermission && updateRoles) {
			if (roles != null) {
				Collection<Role> newRoles = roleDAO.findByPrimaryKeys(roles).values();
				final Collection<Long> roleIdsAdded = new ArrayList<>();
				final Collection<Long> roleIdsRemoved = new ArrayList<>();
				final Collection<Long> appUserRoleIdsRemoved = new ArrayList<>();
				CollectionUtil.synchronizeCollections(user.getRoles(), newRoles,
						new SynchronizeCollectionsOps<AppUserRole, Role>() {
							@Override
							public void add(Collection<AppUserRole> coll, AppUserRole itemToAdd) {
								roleIdsAdded.add(itemToAdd.getRole().getId());
							}

							@Override
							public void remove(Iterator<AppUserRole> it, AppUserRole currentItemBeingRemoved) {
								if (currentItemBeingRemoved.isPersistent()) {
									appUserRoleIdsRemoved.add(currentItemBeingRemoved.getId());
									roleIdsRemoved.add(currentItemBeingRemoved.getRole().getId());
								}
							}

							@Override
							public AppUserRole convert(Role u) {
								return new AppUserRole(user, u);
							}
						});

				Set<Long> totalRolesModified = new HashSet<>(roleIdsAdded);
				totalRolesModified.addAll(roleIdsRemoved);

				appUserRoleDAO.bulkAdd(user.getId(), roleIdsAdded);
				appUserRoleDAO.deleteByPrimaryKeys(appUserRoleIdsRemoved);

				userRefreshNeeded = true;
			}

		}

		if (userRefreshNeeded) {
			updatedUser = appUserDAO.findRequiredByPrimaryKey(updatedUser.getId());
		}

		return updatedUser;
	}

	@Override
	public String updatePassword(long appUserId, String plaintextPassword) {
		String encodedPassword = passwordEncoder.encode(plaintextPassword);
		appUserDAO.updateFieldsWithoutVersionCheck(appUserId, false, null, null, null, encodedPassword);
		return encodedPassword;
	}

	@Override
	public void removeUser(long appUserId, Map<String, Object> userAdminCustomizationsModel) {
		appUserRoleDAO.deleteByUsers(Arrays.asList(appUserId));
		appUserDAO.delete(appUserId);
	}

	public static AppUser populatePreferencesIfNecessary(AppUserDAO appUserDAO, AppUser appUser) {
		AppUserPreferences preferences = appUser.getPreferences();
		if (preferences == null) {
			preferences = new AppUserPreferences();
			appUser.setAppUserPreferences(preferences);
			appUser = appUserDAO.saveOrUpdate(appUser);
		}
		return appUser;
	}

	public AppUser updatePreferences(long appUserId) {
		AppUser appUser = appUserDAO.findRequiredByPrimaryKey(appUserId);
		appUser = populatePreferencesIfNecessary(appUserDAO, appUser);
		appUser = appUserDAO.saveOrUpdate(appUser);
		return appUser;
	}

}
