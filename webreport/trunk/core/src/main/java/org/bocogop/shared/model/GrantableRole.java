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
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Immutable;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

import org.bocogop.shared.model.core.AbstractAuditedPersistent;

@Entity
@Table(name = "GRANTABLE_ROLE", schema = "CORE")
@AttributeOverrides({ @AttributeOverride(name = "id", column = @Column(name = "GRANTABLE_ROLE_ID") ) })
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = NONE, isGetterVisibility = NONE)
@Immutable
@Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
public class GrantableRole extends AbstractAuditedPersistent<GrantableRole> {
	private static final long serialVersionUID = -5252092588500543832L;

	// ----------------------------------------- Fields

	private Role role;
	private Role assignableRole;

	// ----------------------------------------- Business Methods

	public void initializeAll() {
		initialize(getRole());
		initialize(getAssignableRole());
	}

	// ----------------------------------------- Common Methods

	@Override
	protected boolean requiredEquals(GrantableRole oo) {
		return new EqualsBuilder().append(nullSafeGetId(role), nullSafeGetId(oo.getRole()))
				.append(nullSafeGetId(assignableRole), nullSafeGetId(oo.getAssignableRole())).isEquals();
	}

	@Override
	protected int requiredHashCode() {
		return new HashCodeBuilder().append(nullSafeGetId(role)).append(nullSafeGetId(assignableRole)).toHashCode();
	}

	// ----------------------------------------- Accessor Methods

	/**
	 * @return a role that can be assigned by the owner of the role field
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ROLE_BEING_GRANTED_ID", nullable = false)
	@BatchSize(size = 500)
	public Role getAssignableRole() {
		return assignableRole;
	}

	/**
	 * @param assignablerole
	 *            a role that can be assigned by the owner of the role field
	 * 
	 */
	public void setAssignableRole(Role assignableRole) {
		this.assignableRole = assignableRole;
	}

	/**
	 * @return the role belonging to some owner
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "OWNER_ROLE_ID", nullable = false)
	@BatchSize(size = 500)
	public Role getRole() {
		return role;
	}

	/**
	 * @param role
	 *            the role belonging to some owner
	 */
	public void setRole(Role role) {
		this.role = role;
	}

}
