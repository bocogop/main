package org.bocogop.shared.model;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Immutable;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

@Entity
@Table(name = "RolePermission", schema = "Core")
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = NONE, isGetterVisibility = NONE)
@Immutable
@Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
public class RolePermission extends AbstractAuditedPersistent<RolePermission> {
	private static final long serialVersionUID = -4521010384242468103L;

	// -------------------------------------- Fields

	private Role role;
	private Permission permission;

	// -------------------------------------- Business Methods

	public void initializeAll() {
		initialize(getPermission());
		getPermission().initializeAll();
	}

	// -------------------------------------- Common Methods

	@Override
	protected boolean requiredEquals(RolePermission oo) {
		return new EqualsBuilder().append(nullSafeGetId(getRole()), nullSafeGetId(oo.getRole()))
				.append(nullSafeGetId(getPermission()), nullSafeGetId(oo.getPermission())).isEquals();
	}

	@Override
	protected int requiredHashCode() {
		return new HashCodeBuilder().append(nullSafeGetId(getRole())).append(nullSafeGetId(getPermission()))
				.toHashCode();
	}

	// -------------------------------------- Accessor Methods

	/**
	 * @return the permission
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "PermissionFK", nullable = false)
	@BatchSize(size = 500)
	public Permission getPermission() {
		return permission;
	}

	/**
	 * @param permission
	 *            the permission to set
	 */
	public void setPermission(Permission permission) {
		this.permission = permission;
	}

	/**
	 * @return the role
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "RoleFK", nullable = false)
	public Role getRole() {
		return role;
	}

	/**
	 * @param role
	 *            the role to set
	 */
	public void setRole(Role role) {
		this.role = role;
	}

}
