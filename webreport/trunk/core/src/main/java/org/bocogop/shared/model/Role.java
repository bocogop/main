package org.bocogop.shared.model;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.bocogop.shared.model.Role.RoleType;
import org.bocogop.shared.model.lookup.AbstractLookup;
import org.bocogop.shared.model.lookup.LookupType;
import org.bocogop.shared.util.LookupUtil;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.Type;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

@Entity
@Table(name = "Role", schema = "Core")
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = NONE, isGetterVisibility = NONE)
@Immutable
@Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
public class Role extends AbstractLookup<Role, RoleType> implements GrantedAuthority {
	private static final long serialVersionUID = -4464647987438818049L;

	// ---------------------------------------- Fields

	private boolean usedAsPermission = false;
	private Set<RolePermission> internalPermissions = null;

	// ---------------------------------------- Business Methods

	public void initializeAll() {
		initialize(getInternalPermissions());

		for (RolePermission rp : getInternalPermissions())
			rp.initializeAll();
	}

	@Transient
	@Override
	public String getAuthority() {
		if (isUsedAsPermission())
			return getName();
		return null;
	}

	/**
	 * @return the internalPermissions
	 */
	@Transient
	public Set<Permission> getPermissions() {
		Set<Permission> permissions = new HashSet<Permission>();
		for (RolePermission rp : getInternalPermissions())
			permissions.add(rp.getPermission());
		return permissions;
	}

	/**
	 * @param permissions
	 *            the internalPermissions to set
	 */
	public void setNewPermissions(Set<Permission> permissions) {
		Set<Permission> newSet = new HashSet<Permission>();
		newSet.addAll(permissions);

		// old permission map
		Map<Permission, RolePermission> oldMap = new HashMap<Permission, RolePermission>();
		for (RolePermission rolePermission : getInternalPermissions()) {
			oldMap.put(rolePermission.getPermission(), rolePermission);
		}

		// retain the existing ones and remove the deleted ones
		for (Permission permission : oldMap.keySet()) {
			if (newSet.contains(permission)) {
				// an existing permission
				newSet.remove(permission);
			} else {
				// deleted permission
				RolePermission delRolePermission = oldMap.get(permission);
				getInternalPermissions().remove(delRolePermission);
				delRolePermission.setRole(null);
			}
		}

		// add the new permissions
		for (Permission permission : newSet) {
			RolePermission rolePermission = new RolePermission();
			rolePermission.setPermission(permission);
			rolePermission.setRole(this);
			getInternalPermissions().add(rolePermission);
		}
	}

	@Column(name = "UsedAsPermissionInd")
	@Type(type = "yes_no")
	public boolean isUsedAsPermission() {
		return usedAsPermission;
	}

	public void setUsedAsPermission(boolean usedAsPermission) {
		this.usedAsPermission = usedAsPermission;
	}

	/**
	 * @return the internalPermissions
	 */
	@OneToMany(mappedBy = "role", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@BatchSize(size = 500)
	private Set<RolePermission> getInternalPermissions() {
		if (this.internalPermissions == null)
			this.internalPermissions = new HashSet<RolePermission>();
		return internalPermissions;
	}

	/**
	 * @param internalPermissions
	 *            the internalPermissions to set
	 */
	@SuppressWarnings("unused")
	private void setInternalPermissions(Set<RolePermission> internalPermissions) {
		this.internalPermissions = internalPermissions;
	}

	public static enum RoleType implements LookupType {
		NATIONAL_ADMIN(100, "National_Administrator"), //
		USER(101, "User"), //
		VOTER(150, "Voter"), //
		;

		private long id;
		private String name;

		private RoleType(long id, String name) {
			this.id = id;
			this.name = name;
		}

		public long getId() {
			return id;
		}

		public String getName() {
			return name;
		}

		public boolean isAssignedToUser(AppUser user) {
			return LookupUtil.isTypeInLookups(this, user.getBasicGlobalRoles());
		}

		public boolean isApproved(AppUser user, long precinctId) {
			return isAssignedToUser(user);
		}

		public static RoleType getById(long id) {
			for (RoleType type : values())
				if (type.getId() == id)
					return type;
			return null;
		}

		public static Collection<GrantedAuthority> getAllAsGrantedAuthorities() {
			return Arrays.asList(RoleType.values()).stream().map(p -> new SimpleGrantedAuthority(p.getName()))
					.collect(Collectors.toList());
		}
	}
}
