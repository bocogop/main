package org.bocogop.shared.service.impl;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.bocogop.shared.model.AppUser;
import org.bocogop.shared.model.AppUserGlobalRole;
import org.bocogop.shared.model.AppUserPrecinct;
import org.bocogop.shared.model.AppUserPreferences;
import org.bocogop.shared.model.CoreUserDetails;
import org.bocogop.shared.model.Permission;
import org.bocogop.shared.model.Permission.PermissionType;
import org.bocogop.shared.model.Role;
import org.bocogop.shared.model.Role.RoleType;
import org.bocogop.shared.model.precinct.Precinct;
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

	private boolean hasPrecincts(AppUser user, Collection<Long> precinctIdsToBeModified) {
		// First check if the existing precincts of the user being updated are
		// all
		// covered under the grantable roles
		// of the current user
		AppUser currentUser = SecurityUtil.getCurrentUserAs(AppUser.class);
		if (currentUser.isNationalAdmin())
			return true;

		Set<Long> userPrecinctIds = appUserPrecinctDAO.findByUserSorted(currentUser.getId()).stream()
				.map(p -> p.getPrecinct().getId()).collect(Collectors.toSet());
		return userPrecinctIds.containsAll(precinctIdsToBeModified);
	}

	@Override
	public AppUser updateUser(long userId, Boolean enabled, Boolean locked, Boolean expired, ZoneId timezone,
			boolean updateRolesAndPrecincts, Long defaultPrecinctId, Collection<Long> globalRoles,
			Collection<Long> precinctIds) throws ServiceValidationException {
		CoreUserDetails currentUser = SecurityUtil.getCurrentUser();
		final AppUser user = appUserDAO.findRequiredByPrimaryKey(userId);

		boolean isEditingSelf = userId == currentUser.getId();
		boolean hasUMPermission = SecurityUtil.hasAllPermissionsAtCurrentPrecinct(PermissionType.USER_MANAGER);

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

		long nationalAdminID = RoleType.NATIONAL_ADMIN.getId();

		/*
		 * Only allow for role & station changes if the user has UM permission
		 */
		if (hasUMPermission && updateRolesAndPrecincts) {

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

			if (precinctIds != null) {
				Collection<Precinct> newPrecincts = precinctDAO.findRequiredByPrimaryKeys(precinctIds).values();
				final Collection<Long> precinctIdsAdded = new ArrayList<>();
				final Collection<Long> precinctIdsRemoved = new ArrayList<>();
				CollectionUtil.synchronizeCollections(user.getPrecincts(), newPrecincts,
						new SynchronizeCollectionsOps<AppUserPrecinct, Precinct>() {

							@Override
							public void add(Collection<AppUserPrecinct> coll, AppUserPrecinct itemToAdd) {
								precinctIdsAdded.add(itemToAdd.getPrecinct().getId());
							}

							@Override
							public void remove(Iterator<AppUserPrecinct> it, AppUserPrecinct currentItemBeingRemoved) {
								if (currentItemBeingRemoved.isPersistent())
									precinctIdsRemoved.add(currentItemBeingRemoved.getPrecinct().getId());
							}

							@Override
							public AppUserPrecinct convert(Precinct u) {
								return new AppUserPrecinct(user, u);
							}
						});

				Set<Long> totalPrecinctsModified = new HashSet<>(precinctIdsAdded);
				totalPrecinctsModified.addAll(precinctIdsRemoved);
				if (!hasPrecincts(user, totalPrecinctsModified)) {
					throw new ServiceValidationException("appUser.update.notModifiablePrecinct");
				}

				appUserPrecinctDAO.bulkAdd(user.getId(), precinctIdsAdded);
				appUserPrecinctDAO.deleteByPrecinctIDs(userId, precinctIdsRemoved);

				userRefreshNeeded = true;
			}
		}

		/*
		 * Allow changing your own primary precinct; otherwise, require UM
		 * permission
		 */
		if ((isEditingSelf || hasUMPermission) && defaultPrecinctId != null) {
			appUserPrecinctDAO.savePrimaryPrecinctForUser(updatedUser.getId(), defaultPrecinctId);
			userRefreshNeeded = true;
		}

		if (userRefreshNeeded) {
			updatedUser = appUserDAO.findRequiredByPrimaryKey(updatedUser.getId());
		}

		/* Extra security check */
		if (defaultPrecinctId != null && !updatedUser.isAssignedPrecinct(defaultPrecinctId))
			throw new IllegalArgumentException(
					"The user is not assigned to the precinct with the specified defaultPrecinctId");

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

		appUserPrecinctDAO.deleteByUsers(Arrays.asList(appUserId));
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
