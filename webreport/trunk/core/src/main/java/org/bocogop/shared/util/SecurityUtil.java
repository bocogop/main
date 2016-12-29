package org.bocogop.shared.util;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.Hibernate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import org.bocogop.shared.model.Permission.PermissionType;
import org.bocogop.shared.model.Role;
import org.bocogop.shared.model.CoreUserDetails;

public class SecurityUtil {

	public static void resetUserAuthoritiesForFacility(long facilityId) {
		CoreUserDetails user = getCurrentUser();
		Collection<? extends GrantedAuthority> authoritiesForFacility = user.getAuthoritiesAtFacility(facilityId);
		SecurityContext context = SecurityContextHolder.getContext();
		Authentication auth = context.getAuthentication();
		context.setAuthentication(new UsernamePasswordAuthenticationToken(auth.getPrincipal(), auth.getCredentials(),
				authoritiesForFacility));
	}

	// private static Set<? extends GrantedAuthority>
	// getAuthoritiesAtFacility(AppUser appUser, long facilityId) {
	// appUser.getRolesForFacility(facilityId);
	// AppUserFacility auf = appUser.getAppUserFacility(facilityId);
	// if (auf == null)
	// return new HashSet<>();
	//
	// if (auf.isRolesCustomizedForFacility())
	// return getUnmodifiableCombinedAuthorities(auf.getRoles());
	//
	// return getUnmodifiableCombinedAuthorities(appUser.getBasicGlobalRoles());
	// }
	//
	public static Set<? extends GrantedAuthority> getUnmodifiableCombinedAuthorities(Collection<Role> roles) {
		SortedSet<GrantedAuthority> results = new TreeSet<>();
		for (Role role : roles) {
			if (role.isUsedAsPermission())
				results.add(role);
			results.addAll(role.getPermissions());
		}
		return Collections.unmodifiableSortedSet(results);
	}

	public static void assertPermission(PermissionType permissionType) {
		PermissionType[] permissions = { permissionType };
		if (permissionType != null && !hasAnyPermission(false, null, permissions))
			throw new AccessDeniedException("Permission " + permissionType.getName() + " needed for this operation");
	}

	/**
	 * Returns true iff the currently authenticated user has any of the
	 * specified permissions (or roles with the usedAsPermission flag set to
	 * true) at the facility with the specified vaFacilityId
	 */
	public static boolean hasAnyPermissionAtFacility(long vaFacilityId, PermissionType... permissions) {
		return hasAnyPermission(false, vaFacilityId, permissions);
	}

	/**
	 * Returns true iff the currently authenticated user has all of the
	 * specified permissions (or roles with the usedAsPermission flag set to
	 * true)
	 */
	public static boolean hasAnyPermissionAtCurrentFacility(PermissionType... permissions) {
		return hasAnyPermission(false, null, permissions);
	}

	/**
	 * Throws an AccessDeniedException if the currently authenticated user does
	 * not have any of the specified permissions (or roles with the
	 * usedAsPermission flag set to true)
	 */
	public static void ensureAnyPermissionAtCurrentFacility(PermissionType... permissions) {
		hasAnyPermission(true, null, permissions);
	}

	/**
	 * Throws an AccessDeniedException if the currently authenticated user does
	 * not have any of the specified permissions (or roles with the
	 * usedAsPermission flag set to true) at the facility with the specified
	 * vaFacilityId
	 */
	public static void ensureAnyPermissionAtFacility(long vaFacilityId, PermissionType... permissions) {
		hasAnyPermission(true, vaFacilityId, permissions);
	}

	private static boolean hasAnyPermission(boolean throwExceptionOnFailure, Long vaFacilityId,
			PermissionType... permissionsRequired) {
		Collection<? extends GrantedAuthority> permissionsAssigned = null;
		if (vaFacilityId == null) {
			permissionsAssigned = SecurityContextHolder.getContext().getAuthentication().getAuthorities();
		} else {
			permissionsAssigned = getCurrentUser().getAuthoritiesAtFacility(vaFacilityId);
		}

		for (GrantedAuthority authority : permissionsAssigned) {
			String a = authority.getAuthority();
			for (PermissionType permission : permissionsRequired) {
				if (permission.getName().equals(a)) {
					return true;
				}
			}
		}

		if (throwExceptionOnFailure) {
			throw new AccessDeniedException(
					"The user is not assigned any of the permissions {" + StringUtils.join(permissionsRequired, ", ")
							+ "} at " + (vaFacilityId == null ? "the current station" : "station ID " + vaFacilityId));
		} else {
			return false;
		}
	}

	/**
	 * Returns true iff the currently authenticated user has all of the
	 * specified permissions (or roles with the usedAsPermission flag set to
	 * true)
	 */
	public static boolean hasAllPermissionsAtCurrentFacility(PermissionType... permissions) {
		return hasAllPermissions(false, null, permissions);
	}

	/**
	 * Returns true iff the currently authenticated user has all of the
	 * specified permissions (or roles with the usedAsPermission flag set to
	 * true) at the facility with the specified vaFacilityId
	 */
	public static boolean hasAllPermissionsAtFacility(long vaFacilityId, PermissionType... permissions) {
		return hasAllPermissions(false, vaFacilityId, permissions);
	}

	/**
	 * Throws an AccessDeniedException if the currently authenticated user does
	 * not have all of the specified permissions (or roles with the
	 * usedAsPermission flag set to true) at the facility with the specified
	 * vaFacilityId
	 */
	public static void ensureAllPermissionsAtFacility(long vaFacilityId, PermissionType... permissions) {
		hasAllPermissions(true, vaFacilityId, permissions);
	}

	/**
	 * Throws an AccessDeniedException if the currently authenticated user does
	 * not have all of the specified permissions (or roles with the
	 * usedAsPermission flag set to true)
	 */
	public static void ensureAllPermissionsAtCurrentFacility(PermissionType... permissions) {
		hasAllPermissions(true, null, permissions);
	}

	private static boolean hasAllPermissions(boolean throwExceptionOnFailure, Long vaFacilityId,
			PermissionType... permissionsRequired) {

		Collection<? extends GrantedAuthority> permissionsAssigned = null;
		if (vaFacilityId == null) {
			permissionsAssigned = SecurityContextHolder.getContext().getAuthentication().getAuthorities();
		} else {
			permissionsAssigned = getCurrentUser().getAuthoritiesAtFacility(vaFacilityId);
		}

		outer: //
		for (PermissionType permission : permissionsRequired) {
			for (GrantedAuthority authority : permissionsAssigned) {
				String a = authority.getAuthority();
				if (permission.getName().equals(a)) {
					continue outer;
				}
			}
			if (throwExceptionOnFailure) {
				throw new AccessDeniedException("The user is not assigned the permission '" + permission.getName()
						+ " at " + (vaFacilityId == null ? "the current station" : "station ID " + vaFacilityId));
			} else {
				return false;
			}
		}

		return true;
	}

	/**
	 * Returns the current user as the specified class, or null if there is no
	 * current user. Throws a ClassCastException if the current user exists but
	 * is not an instance of the specified class.
	 */
	@SuppressWarnings("unchecked")
	public static <T extends CoreUserDetails> T getCurrentUserAs(Class<T> clazz) {
		return (T) getCurrentUser();
	}

	/**
	 * Returns the current user as the specified class, or null if there is no
	 * current user or if the current user cannot be cast to the specified
	 * class.
	 */
	@SuppressWarnings("unchecked")
	public static <T extends CoreUserDetails> T getCurrentUserAsOrNull(Class<T> clazz) {
		CoreUserDetails d = getCurrentUser();
		if (d == null)
			return null;
		if (clazz.isAssignableFrom(Hibernate.getClass(d)))
			return (T) d;
		return null;
	}

	public static CoreUserDetails getCurrentUser() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null)
			return null;

		Object principal = authentication.getPrincipal();
		if (principal instanceof CoreUserDetails)
			return (CoreUserDetails) principal;

		return null;
	}

	public static String getCurrentUserName() {
		CoreUserDetails currentUser = getCurrentUser();
		return currentUser != null ? currentUser.getUsername() : null;
	}

}