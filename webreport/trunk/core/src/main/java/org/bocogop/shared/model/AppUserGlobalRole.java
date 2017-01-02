package org.bocogop.shared.model;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.PUBLIC_ONLY;

import java.util.Comparator;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.bocogop.shared.model.lookup.Lookup;
import org.bocogop.shared.model.lookup.LookupContainer;
import org.bocogop.shared.model.lookup.LookupType;
import org.hibernate.annotations.BatchSize;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "AppUserGlobalRole", schema = "Core")
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = PUBLIC_ONLY, isGetterVisibility = PUBLIC_ONLY)
public class AppUserGlobalRole extends AbstractAuditedPersistent<AppUserGlobalRole> implements LookupContainer<Role> {
	private static final long serialVersionUID = 1L;

	public static class CompareByRole implements Comparator<AppUserGlobalRole> {
		@Override
		public int compare(AppUserGlobalRole o1, AppUserGlobalRole o2) {
			if (o1.equals(o2))
				return 0;
			return new CompareToBuilder().append(o1 == null ? null : o1.getRole(), o2 == null ? null : o2.getRole())
					.toComparison() > 0 ? 1 : -1;
		}
	}

	private AppUser appUser;
	private Role role;

	// -------------------------------------- Constructors

	public AppUserGlobalRole() {
	}

	public AppUserGlobalRole(AppUser appUser, Role role) {
		this.appUser = appUser;
		this.role = role;
	}

	// -------------------------------------- Business Methods

	public void initializeAll() {
		initialize(getRole());
		getRole().initializeAll();
	}

	@Override
	@Transient
	public Lookup<Role, ? extends LookupType> getLookup() {
		return getRole();
	}

	// -------------------------------------- Common Methods

	@Override
	protected boolean requiredEquals(AppUserGlobalRole oo) {
		return new EqualsBuilder().append(nullSafeGetId(appUser), nullSafeGetId(oo.getAppUser()))
				.append(nullSafeGetId(role), nullSafeGetId(oo.getRole())).isEquals();
	}

	@Override
	protected int requiredHashCode() {
		return new HashCodeBuilder().append(nullSafeGetId(appUser)).append(nullSafeGetId(role)).toHashCode();
	}

	@Override
	public String toString() {
		return "AppUserGlobalRole(User ID " + nullSafeGetId(appUser) + ", Role ID " + nullSafeGetId(role) + ")";
	}

	// -------------------------------------- Accessor Methods

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "AppUserFK", nullable = false)
	@JsonIgnore
	public AppUser getAppUser() {
		return appUser;
	}

	public void setAppUser(AppUser appUser) {
		this.appUser = appUser;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "RoleFK", nullable = false)
	@BatchSize(size = 500)
	public Role getRole() {
		return role;
	}

	public void setRole(Role role) {
		this.role = role;
	}

}
