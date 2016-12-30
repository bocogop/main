package org.bocogop.shared.service;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Map;

import org.bocogop.shared.model.AppUser;
import org.bocogop.shared.service.validation.ServiceValidationException;

public interface AppUserService {

	int updateFieldsWithoutVersionCheck(long appUserID, boolean incrementVersion, Long lastVisitedDutyStationNumber,
			boolean setAccountLockDate, ZonedDateTime accountLockDate, Integer failedLoginCount);

	void logApplicationAccess(String activeDirectoryName, ZonedDateTime now);

	AppUser saveOrUpdate(AppUser appUser);

	/* For application-internal code only */
	AppUser saveOrUpdateWithoutAuthority(AppUser appUser);

	AppUser updateUser(long userId, Boolean enabled, Boolean locked, Boolean expired, ZoneId timezone,
			boolean updateRolesAndPrecincts, Long defaultPrecinctId, Collection<Long> globalRoles,
			Collection<Long> precinctIds) throws ServiceValidationException;

	String updatePassword(long appUserId, String plaintextPassword);

	/**
	 * Removes the specified user from the system. Returns true if the user was
	 * successfully removed; false if the user could not be removed due to
	 * app-specific foreign key constraints - CPB
	 */
	void removeUser(long appUserId, Map<String, Object> userAdminCustomizationsModel);

	AppUser createOrRetrieveUser(String activeDirectoryName, Map<String, Object> userAdminCustomizationsModel);

	AppUser updatePreferences(long appUserId, Boolean soundsEnabled);

}
