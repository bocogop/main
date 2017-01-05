package org.bocogop.shared.util;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.lang3.StringUtils;
import org.bocogop.shared.model.CoreUserDetails;
import org.bocogop.shared.model.Permission.PermissionType;
import org.bocogop.shared.model.Role;
import org.hibernate.Hibernate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtil {

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
		if (permissionType != null && !hasAnyPermission(false, permissions))
			throw new AccessDeniedException("Permission " + permissionType.getName() + " needed for this operation");
	}

	/**
	 * Returns true iff the currently authenticated user has any of the
	 * specified permissions (or roles with the usedAsPermission flag set to
	 * true) at the precinct with the specified precinctId
	 */
	public static boolean hasAnyPermission(PermissionType... permissions) {
		return hasAnyPermission(false, permissions);
	}

	/**
	 * Throws an AccessDeniedException if the currently authenticated user does
	 * not have any of the specified permissions (or roles with the
	 * usedAsPermission flag set to true)
	 */
	public static void ensureAnyPermission(PermissionType... permissions) {
		hasAnyPermission(true, permissions);
	}

	/**
	 * Throws an AccessDeniedException if the currently authenticated user does
	 * not have any of the specified permissions (or roles with the
	 * usedAsPermission flag set to true) at the precinct with the specified
	 * precinctId
	 */
	public static void ensureAnyPermissionAtPrecinct(long precinctId, PermissionType... permissions) {
		hasAnyPermission(true, permissions);
	}

	private static boolean hasAnyPermission(boolean throwExceptionOnFailure, PermissionType... permissionsRequired) {
		Collection<? extends GrantedAuthority> permissionsAssigned = SecurityContextHolder.getContext()
				.getAuthentication().getAuthorities();

		for (GrantedAuthority authority : permissionsAssigned) {
			String a = authority.getAuthority();
			for (PermissionType permission : permissionsRequired) {
				if (permission.getName().equals(a)) {
					return true;
				}
			}
		}

		if (throwExceptionOnFailure) {
			throw new AccessDeniedException("The user is not assigned any of the permissions {"
					+ StringUtils.join(permissionsRequired, ", ") + "}");
		} else {
			return false;
		}
	}

	/**
	 * Returns true iff the currently authenticated user has all of the
	 * specified permissions (or roles with the usedAsPermission flag set to
	 * true)
	 */
	public static boolean hasAllPermissions(PermissionType... permissions) {
		return hasAllPermissions(false, permissions);
	}

	/**
	 * Throws an AccessDeniedException if the currently authenticated user does
	 * not have all of the specified permissions (or roles with the
	 * usedAsPermission flag set to true) at the precinct with the specified
	 * precinctId
	 */
	public static void ensureAllPermissions(PermissionType... permissions) {
		hasAllPermissions(true, permissions);
	}

	private static boolean hasAllPermissions(boolean throwExceptionOnFailure, PermissionType... permissionsRequired) {
		Collection<? extends GrantedAuthority> permissionsAssigned = SecurityContextHolder.getContext()
				.getAuthentication().getAuthorities();

		outer: //
		for (PermissionType permission : permissionsRequired) {
			for (GrantedAuthority authority : permissionsAssigned) {
				String a = authority.getAuthority();
				if (permission.getName().equals(a)) {
					continue outer;
				}
			}
			if (throwExceptionOnFailure) {
				throw new AccessDeniedException("The user is not assigned the permission '" + permission.getName());
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
	public static <T extends CoreUserDetails<T>> T getCurrentUserAs(Class<T> clazz) {
		return (T) getCurrentUser();
	}

	/**
	 * Returns the current user as the specified class, or null if there is no
	 * current user or if the current user cannot be cast to the specified
	 * class.
	 */
	@SuppressWarnings("unchecked")
	public static <T extends CoreUserDetails<T>> T getCurrentUserAsOrNull(Class<T> clazz) {
		CoreUserDetails<?> d = getCurrentUser();
		if (d == null)
			return null;
		if (clazz.isAssignableFrom(Hibernate.getClass(d)))
			return (T) d;
		return null;
	}

	public static CoreUserDetails<?> getCurrentUser() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null)
			return null;

		Object principal = authentication.getPrincipal();
		if (principal instanceof CoreUserDetails)
			return (CoreUserDetails<?>) principal;

		return null;
	}

	public static String getCurrentUserName() {
		CoreUserDetails<?> currentUser = getCurrentUser();
		return currentUser != null ? currentUser.getUsername() : null;
	}

}