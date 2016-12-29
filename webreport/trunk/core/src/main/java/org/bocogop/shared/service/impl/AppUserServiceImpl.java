package org.bocogop.shared.service.impl;

import static org.bocogop.shared.model.lookup.InactiveReason.InactiveReasonType.LACK_OF_ACTIVITY;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.bocogop.shared.model.AppUser;
import org.bocogop.shared.model.AppUserFacility;
import org.bocogop.shared.model.AppUserGlobalRole;
import org.bocogop.shared.model.AppUserPreferences;
import org.bocogop.shared.model.Permission;
import org.bocogop.shared.model.Permission.PermissionType;
import org.bocogop.shared.model.Role;
import org.bocogop.shared.model.Role.RoleType;
import org.bocogop.shared.model.CoreUserDetails;
import org.bocogop.shared.model.ldap.LdapPerson;
import org.bocogop.shared.model.lookup.InactiveReason;
import org.bocogop.shared.model.lookup.sds.VAFacility;
import org.bocogop.shared.model.lookup.sds.VAFacility.VAFacilityValue;
import org.bocogop.shared.persistence.AppUserDAO;
import org.bocogop.shared.service.AppUserService;
import org.bocogop.shared.service.UserAdminCustomizations;
import org.bocogop.shared.service.validation.ServiceValidationException;
import org.bocogop.shared.util.CollectionUtil;
import org.bocogop.shared.util.CollectionUtil.SynchronizeCollectionsOps;
import org.bocogop.shared.util.SecurityUtil;

@Service
@Transactional(rollbackFor = ServiceValidationException.class)
public class AppUserServiceImpl extends AbstractAppServiceImpl implements AppUserService {
	private static final Logger logger = LoggerFactory.getLogger(AppUserServiceImpl.class);

	@Autowired
	private PasswordEncoder passwordEncoder;
	@Autowired(required = false)
	private UserAdminCustomizations customizations;

	@Override
	public int updateFieldsWithoutVersionCheck(long appUserId, boolean incrementVersion, Long lastVisitedVAFacilityId,
			boolean setAccountLockDate, ZonedDateTime accountLockDate, Integer failedLoginCount) {
		return appUserDAO.updateFieldsWithoutVersionCheck(appUserId, incrementVersion, lastVisitedVAFacilityId,
				accountLockDate, failedLoginCount, null);
	}

	@Override
	public void logApplicationAccess(String activeDirectoryName, ZonedDateTime dateOfNewAccess) {
		AppUser user = appUserDAO.findByUsername(activeDirectoryName, false);

		if (user.isInactive()) {
			logger.warn("Inactive user " + activeDirectoryName + " accessed application");
			return;
		}
		user.setLastSuccessfulLoginDate(dateOfNewAccess);
		user = appUserDAO.saveOrUpdate(user);
	}

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
	public AppUser updateUserFromLDAP(long userId, boolean checkAuthority) {
		AppUser appUser = appUserDAO.findRequiredByPrimaryKey(userId);
		LdapPerson ldapPerson = ldapPersonDAO.findBySAMAccountName(appUser.getUsername());
		appUser.setFromLdapPerson(ldapPerson);
		if (checkAuthority) {
			// TODO - unused for now - CPB
		} else {
			appUser = saveOrUpdateWithoutAuthority(appUser);
		}
		return appUser;
	}

	private boolean hasGrantableRoles(AppUser user, Collection<Long> roleIdsToBeAssigned) {
		AppUser currentUser = SecurityUtil.getCurrentUserAs(AppUser.class);
		if (currentUser.isNationalAdmin())
			return true;

		boolean hasRoles = false;

		// First check if the existing roles of the user being updated are all
		// covered under the grantable roles
		// of the current user
		List<Role> grantableRoles = grantableRoleDAO.findAllGrantableRolesForUser(currentUser.getId());
		SortedSet<Role> userRoles = user.getBasicGlobalRoles();
		hasRoles = grantableRoles.containsAll(userRoles);

		if (hasRoles) {
			// if the above check passed, then check to see if the roles to be
			// assigned are covered
			// under the current user's grantable roles
			List<Long> grantableRoleIds = new ArrayList<Long>();
			for (Role role : grantableRoles) {
				grantableRoleIds.add(role.getId());
			}
			hasRoles = grantableRoleIds.containsAll(roleIdsToBeAssigned);
		}

		return hasRoles;
	}

	private boolean hasFacilities(AppUser user, Collection<Long> facilityIdsToBeModified) {
		// First check if the existing facilities of the user being updated are
		// all
		// covered under the grantable roles
		// of the current user
		AppUser currentUser = SecurityUtil.getCurrentUserAs(AppUser.class);
		if (currentUser.isNationalAdmin())
			return true;

		Set<Long> userFacilityIds = appUserFacilityDAO.findByUserSorted(currentUser.getId()).stream()
				.map(p -> p.getFacility().getId()).collect(Collectors.toSet());
		return userFacilityIds.containsAll(facilityIdsToBeModified);
	}

	@Override
	public AppUser updateUser(long userId, Boolean enabled, Boolean locked, Boolean expired, ZoneId timezone,
			boolean updateRolesAndFacilities, Long defaultFacilityId, Collection<Long> globalRoles,
			Collection<Long> vaFacilityIds) throws ServiceValidationException {
		CoreUserDetails currentUser = SecurityUtil.getCurrentUser();
		final AppUser user = appUserDAO.findRequiredByPrimaryKey(userId);

		boolean isEditingSelf = userId == currentUser.getId();
		boolean hasUMPermission = SecurityUtil.hasAllPermissionsAtCurrentFacility(PermissionType.USER_MANAGER);

		boolean updatedBasics = false;

		/* Can't disable/enable or lock/unlock yourself */
		if (!isEditingSelf && hasUMPermission) {
			if (enabled != null) {
				updatedBasics = true;
				user.setEnabled(enabled);
			}

			if (locked != null) {
				boolean userWasLocked = user.isLocked();

				if (locked && !userWasLocked) {
					user.lock(ZonedDateTime.now());
					updatedBasics = true;
				} else if (userWasLocked && !locked) {
					user.unlock();
					updatedBasics = true;
				}
			}

			if (expired != null) {
				boolean userWasExpired = user.isAccountExpired();

				if (expired && !userWasExpired) {
					InactiveReason inactiveReason = inactiveReasonDAO.findByLookup(LACK_OF_ACTIVITY);
					user.inactivate(ZonedDateTime.now(), inactiveReason);
				} else if (userWasExpired && !expired) {
					user.activate();
					user.setLastSuccessfulLoginDate(null);
				}
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

		long centralOfficeID = VAFacilityValue.CENTRAL_OFFICE.getId();
		long nationalAdminID = RoleType.NATIONAL_ADMIN.getId();
		long nationalSpecialistID = RoleType.NATIONAL_SPECIALIST.getId();
		long nationalUserID = RoleType.NATIONAL_USER.getId();

		/*
		 * Only allow for role & station changes if the user has UM permission
		 */
		if (hasUMPermission && updateRolesAndFacilities) {

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
				if (!hasGrantableRoles(user, totalRolesModified)) {
					throw new ServiceValidationException("appUser.update.notGrantableRole");
				}

				appUserGlobalRoleDAO.bulkAdd(user.getId(), roleIdsAdded);
				appUserGlobalRoleDAO.deleteByPrimaryKeys(appUserGlobalRoleIdsRemoved);

				/*
				 * Not as clean as checking the complete set of facilities
				 * during each call to this method, but that would require
				 * fetching all children for the AppUser every time, so this is
				 * a compromise - CPB
				 */
				List<Long> centralOfficeIDList = Arrays.asList(centralOfficeID);
				if ((roleIdsAdded.contains(nationalAdminID) || roleIdsAdded.contains(nationalSpecialistID)
						|| roleIdsAdded.contains(nationalUserID))
						&& updatedUser.getAppUserFacility(centralOfficeID) == null) {
					appUserFacilityDAO.bulkAdd(user.getId(), centralOfficeIDList, false);
				}

				userRefreshNeeded = true;
			}

			if (vaFacilityIds != null) {
				Collection<VAFacility> newFacilities = vaFacilityDAO.findRequiredByPrimaryKeys(vaFacilityIds).values();
				final Collection<Long> facilityIdsAdded = new ArrayList<>();
				final Collection<Long> facilityIdsRemoved = new ArrayList<>();
				CollectionUtil.synchronizeCollections(user.getFacilities(), newFacilities,
						new SynchronizeCollectionsOps<AppUserFacility, VAFacility>() {

							@Override
							public void add(Collection<AppUserFacility> coll, AppUserFacility itemToAdd) {
								facilityIdsAdded.add(itemToAdd.getFacility().getId());
							}

							@Override
							public void remove(Iterator<AppUserFacility> it, AppUserFacility currentItemBeingRemoved) {
								if (currentItemBeingRemoved.isPersistent())
									facilityIdsRemoved.add(currentItemBeingRemoved.getFacility().getId());
							}

							@Override
							public AppUserFacility convert(VAFacility u) {
								return new AppUserFacility(user, u);
							}
						});

				Set<Long> totalFacilitiesModified = new HashSet<>(facilityIdsAdded);
				totalFacilitiesModified.addAll(facilityIdsRemoved);
				if (!hasFacilities(user, totalFacilitiesModified)) {
					throw new ServiceValidationException("appUser.update.notModifiableFacility");
				}

				appUserFacilityDAO.bulkAdd(user.getId(), facilityIdsAdded, false);
				appUserFacilityRoleDAO.deleteByVAFacilityIDs(userId, facilityIdsRemoved);
				appUserFacilityDAO.deleteByVAFacilityIDs(userId, facilityIdsRemoved);

				/*
				 * Not as clean as checking the complete set of facilities
				 * during each call to this method, but that would require
				 * fetching all children for the AppUser every time, so this is
				 * a compromise - CPB
				 */
				List<Long> nationalAdminIDList = Arrays.asList(nationalAdminID, nationalSpecialistID, nationalUserID);
				if (facilityIdsRemoved.contains(centralOfficeID)) {
					appUserGlobalRoleDAO.deleteByRoleIds(user.getId(), nationalAdminIDList);
				}

				userRefreshNeeded = true;
			}
		}

		/*
		 * Allow changing your own primary facility; otherwise, require UM
		 * permission
		 */
		if ((isEditingSelf || hasUMPermission) && defaultFacilityId != null) {
			appUserFacilityDAO.savePrimaryFacilityForUser(updatedUser.getId(), defaultFacilityId);
			userRefreshNeeded = true;
		}

		if (userRefreshNeeded) {
			updatedUser = appUserDAO.findRequiredByPrimaryKey(updatedUser.getId());
		}

		/* Extra security check */
		if (defaultFacilityId != null && !updatedUser.isAssignedFacility(defaultFacilityId))
			throw new IllegalArgumentException(
					"The user is not assigned to the facility with the specified defaultFacilityId");

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

		appUserFacilityRoleDAO.deleteByUsers(Arrays.asList(appUserId));
		appUserFacilityDAO.deleteByUsers(Arrays.asList(appUserId));
		appUserGlobalRoleDAO.deleteByUsers(Arrays.asList(appUserId));
		appUserDAO.delete(appUserId);
	}

	@Override
	@PreAuthorize("hasAuthority('" + Permission.USER_MANAGER + "')")
	public AppUser customizeUser(long userId, Collection<Long> roles, Collection<Long> vaFacilities)
			throws ServiceValidationException {
		/* Don't let them customize the central office - CPB */
		vaFacilities.remove(VAFacilityValue.CENTRAL_OFFICE.getId());

		AppUser user = appUserDAO.findRequiredByPrimaryKey(userId);

		if (!hasGrantableRoles(user, roles)) {
			throw new ServiceValidationException("appUser.update.notGrantableRole");
		}

		VAFacility primaryFacilityForUser = appUserFacilityDAO.findPrimaryFacilityForUser(userId);

		Set<Long> specifiedRoles = new HashSet<>(roles);

		Set<Long> globalRoleIDs = new HashSet<>();
		user.getBasicGlobalRoles().forEach(gr -> {
			/* Don't let them customize the national admin - CPB */
			if (RoleType.NATIONAL_ADMIN.getId() != gr.getId())
				globalRoleIDs.add(gr.getId());
		});

		appUserFacilityRoleDAO.deleteByVAFacilityIDs(userId, vaFacilities);
		appUserFacilityDAO.deleteByVAFacilityIDs(userId, vaFacilities);

		if (specifiedRoles.equals(globalRoleIDs)) {
			appUserFacilityDAO.bulkAdd(user.getId(), vaFacilities, false);
		} else {
			appUserFacilityDAO.bulkAdd(user.getId(), vaFacilities, true);
			appUserFacilityRoleDAO.bulkAdd(user.getId(), specifiedRoles, vaFacilities);
		}

		if (primaryFacilityForUser != null) {
			Long primaryFacilityId = primaryFacilityForUser.getId();
			if (vaFacilities.contains(primaryFacilityId))
				appUserFacilityDAO.savePrimaryFacilityForUser(userId, primaryFacilityId);
		}

		user = appUserDAO.findRequiredByPrimaryKey(user.getId());
		return user;
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

		LdapPerson person = ldapPersonDAO.findBySAMAccountName(activeDirectoryName);
		if (person == null)
			throw new IllegalArgumentException(
					"No user found with active directory name '" + activeDirectoryName + "'");
		appUser = new AppUser(person);
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

	public AppUser updatePreferences(long appUserId, Boolean soundsEnabled) {
		AppUser appUser = appUserDAO.findRequiredByPrimaryKey(appUserId);
		appUser = populatePreferencesIfNecessary(appUserDAO, appUser);
		AppUserPreferences preferences = appUser.getPreferences();
		if (soundsEnabled != null) {
			preferences.setSoundsEnabled(soundsEnabled);
		}
		appUser = appUserDAO.saveOrUpdate(appUser);
		return appUser;
	}

}
