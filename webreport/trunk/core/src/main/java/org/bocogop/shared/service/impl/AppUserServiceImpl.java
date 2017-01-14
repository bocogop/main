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
import org.bocogop.shared.model.AppUserGlobalRole;
import org.bocogop.shared.model.AppUserPreferences;
import org.bocogop.shared.model.CoreUserDetails;
import org.bocogop.shared.model.Permission;
import org.bocogop.shared.model.Permission.PermissionType;
import org.bocogop.shared.model.Role;
import org.bocogop.shared.persistence.AppUserDAO;
import org.bocogop.shared.service.AbstractAppServiceImpl;
import org.bocogop.shared.service.AppUserService;
import org.bocogop.shared.service.UserAdminCustomizations;
import org.bocogop.shared.service.validation.ServiceValidationException;
import org.bocogop.shared.util.CollectionUtil;
import org.bocogop.shared.util.CollectionUtil.SynchronizeCollectionsOps;
import org.bocogop.shared.util.SecurityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
	@Autowired(required = false)
	private UserAdminCustomizations customizations;

	@Override
	@PreAuthorize("hasAuthority('" + Permission.USER_MANAGER + "')")
	public AppUser saveOrUpdate(AppUser appUser) {
		return appUserDAO.saveOrUpdate(appUser);
	}

	@Override
	public AppUser saveOrUpdateWithoutAuthority(AppUser appUser) {
		AppUser updated = appUserDAO.saveOrUpdate(appUser);
		return updated;
	}

	@Override
	public AppUser updateUser(long userId, Boolean enabled, Boolean locked, Boolean expired, ZoneId timezone,
			boolean updateRoles, Collection<Long> globalRoles) throws ServiceValidationException {
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
			if (globalRoles != null) {
				Collection<Role> newRoles = roleDAO.findByPrimaryKeys(globalRoles).values();
				final Collection<Long> roleIdsAdded = new ArrayList<>();
				final Collection<Long> roleIdsRemoved = new ArrayList<>();
				final Collection<Long> appUserGlobalRoleIdsRemoved = new ArrayList<>();
				CollectionUtil.synchronizeCollections(user.getGlobalRoles(), newRoles,
						new SynchronizeCollectionsOps<AppUserGlobalRole, Role>() {
							@Override
							public void add(Collection<AppUserGlobalRole> coll, AppUserGlobalRole itemToAdd) {
								roleIdsAdded.add(itemToAdd.getRole().getId());
							}

							@Override
							public void remove(Iterator<AppUserGlobalRole> it,
									AppUserGlobalRole currentItemBeingRemoved) {
								if (currentItemBeingRemoved.isPersistent()) {
									appUserGlobalRoleIdsRemoved.add(currentItemBeingRemoved.getId());
									roleIdsRemoved.add(currentItemBeingRemoved.getRole().getId());
								}
							}

							@Override
							public AppUserGlobalRole convert(Role u) {
								return new AppUserGlobalRole(user, u);
							}
						});

				Set<Long> totalRolesModified = new HashSet<>(roleIdsAdded);
				totalRolesModified.addAll(roleIdsRemoved);

				appUserGlobalRoleDAO.bulkAdd(user.getId(), roleIdsAdded);
				appUserGlobalRoleDAO.deleteByPrimaryKeys(appUserGlobalRoleIdsRemoved);

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
		if (customizations != null)
			customizations.userDeletedCallback(appUserId, userAdminCustomizationsModel);

		appUserGlobalRoleDAO.deleteByUsers(Arrays.asList(appUserId));
		appUserDAO.delete(appUserId);
	}

	@Override
	public AppUser createOrRetrieveUser(String activeDirectoryName, Map<String, Object> userAdminCustomizationsModel) {
		AppUser appUser = appUserDAO.findByUsername(activeDirectoryName, false);
		if (appUser != null) {
			appUser = populatePreferencesIfNecessary(appUserDAO, appUser);

			if (customizations != null)
				appUser = customizations.userRetrievedCallback(appUser, userAdminCustomizationsModel);

			return appUser;
		}

		// TODO BOCOGOP set name here from user input?
		appUser = new AppUser();
		appUser.setEnabled(true);
		appUser = saveOrUpdateWithoutAuthority(appUser);
		appUser = populatePreferencesIfNecessary(appUserDAO, appUser);

		if (customizations != null)
			appUser = customizations.userCreatedCallback(appUser, userAdminCustomizationsModel);

		return appUser;
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
