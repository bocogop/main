package org.bocogop.shared.model;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.PUBLIC_ONLY;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.annotations.BatchSize;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;

import org.bocogop.shared.model.core.AbstractAuditedPersistent;
import org.bocogop.shared.model.lookup.Lookup;
import org.bocogop.shared.model.lookup.LookupContainer;
import org.bocogop.shared.model.lookup.LookupType;

@Entity
@Table(name = "APP_USER_FACILITY_ROLE", schema = "CORE")
@AttributeOverrides({ @AttributeOverride(name = "id", column = @Column(name = "APP_USER_FACILITY_ROLE_ID") ) })
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = PUBLIC_ONLY, isGetterVisibility = PUBLIC_ONLY)
public class AppUserFacilityRole extends AbstractAuditedPersistent<AppUserFacilityRole>
		implements LookupContainer<Role> {
	private static final long serialVersionUID = 1L;

	private AppUserFacility appUserFacility;
	private Role role;

	// ---------------------------------------- Business Methods

	public void initializeAll() {
		initialize(getRole());
		getRole().initializeAll();
	}

	@Override
	@Transient
	public Lookup<Role, ? extends LookupType> getLookup() {
		return getRole();
	}

	// ---------------------------------------- Common Methods

	@Override
	protected boolean requiredEquals(AppUserFacilityRole oo) {
		return new EqualsBuilder().append(nullSafeGetId(appUserFacility), nullSafeGetId(oo.getAppUserFacility()))
				.append(nullSafeGetId(role), nullSafeGetId(oo.getRole())).isEquals();
	}

	@Override
	protected int requiredHashCode() {
		return new HashCodeBuilder().append(nullSafeGetId(appUserFacility)).append(nullSafeGetId(role)).toHashCode();
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "APP_USER_FACILITY_ID", nullable = false)
	@BatchSize(size = 500)
	@JsonIgnore
	public AppUserFacility getAppUserFacility() {
		return appUserFacility;
	}

	public void setAppUserFacility(AppUserFacility appUserFacility) {
		this.appUserFacility = appUserFacility;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ROLE_ID", nullable = false)
	@BatchSize(size = 500)
	public Role getRole() {
		return role;
	}

	public void setRole(Role role) {
		this.role = role;
	}

}
