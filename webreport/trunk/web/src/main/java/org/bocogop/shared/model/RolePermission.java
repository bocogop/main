package org.bocogop.shared.model;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.bocogop.shared.model.core.AbstractAuditedPersistent;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Immutable;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

@Entity
@Table(name = "ROLE_PERMISSION", schema = "CORE")
@AttributeOverrides({ @AttributeOverride(name = "id", column = @Column(name = "ROLE_PERMISSION_ID") ) })
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
		return new EqualsBuilder().append(nullSafeGetId(role), nullSafeGetId(oo.getRole()))
				.append(nullSafeGetId(permission), nullSafeGetId(oo.getPermission())).isEquals();
	}

	@Override
	protected int requiredHashCode() {
		return new HashCodeBuilder().append(nullSafeGetId(role)).append(nullSafeGetId(permission)).toHashCode();
	}

	// -------------------------------------- Accessor Methods

	/**
	 * @return the permission
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "PERMISSION_ID", nullable = false)
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
	@JoinColumn(name = "ROLE_ID", nullable = false)
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
