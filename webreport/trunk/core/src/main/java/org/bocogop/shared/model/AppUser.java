package org.bocogop.shared.model;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.PUBLIC_ONLY;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.bocogop.shared.model.AppUserRole.CompareByRole;
import org.bocogop.shared.model.Role.RoleType;
import org.bocogop.shared.util.StringUtil;
import org.bocogop.shared.web.conversion.ZoneIdSerializer;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Type;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@Entity
@Table(name = "AppUser", schema = "Core")
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = PUBLIC_ONLY, isGetterVisibility = PUBLIC_ONLY)
public class AppUser extends AbstractAuditedVersionedPersistent<AppUser>
		implements CoreUserDetails<AppUser>, Comparable<AppUser> {
	private static final long serialVersionUID = 3675278963814073675L;

	/**
	 * See @JsonView documentation - CPB
	 */
	public static class AppUserView {
		public interface Basic {
		}

		public interface Extended extends Basic {
		}
	}

	// ------------------------------------ Fields

	private String username;
	private String password;
	private ZoneId timeZone;

	private String firstName;
	private String middleName;
	private String lastName;
	private String description;
	private String phone;
	private String email;
	/* Has someone completely disabled this user from logging in */
	private boolean enabled;

	private Set<AppUserRole> roles;

	/*
	 * Only contains one item, lazy-loaded; mapping like this instead
	 * of @OneToOne so it can be optional and still lazy-load - CPB
	 */
	private List<AppUserPreferences> appUserPreferencesList;

	/* Transient security fields */
	private Set<GrantedAuthority> authoritiesCache = null;

	// ------------------------------------ Constructors

	public AppUser() {
	}

	public AppUser(String username) {
		this.username = username;
	}

	// ------------------------------------ Business Methods

	public void initializeAll() {
		initialize(getRoles());
		initialize(getAppUserPreferencesList());

		for (AppUserRole augr : getRoles()) {
			augr.initializeAll();
		}
	}

	@Transient
	public String getDisplayName() {
		String name = StringUtil.getDisplayName(true, firstName, middleName, lastName, null);

		if (StringUtils.isEmpty(name)) {
			return getUsername();
		} else {
			return name;
		}
	}

	@Transient
	public boolean isNationalAdmin() {
		return RoleType.NATIONAL_ADMIN.isAssignedToUser(this);
	}

	public boolean hasRole(RoleType type) {
		for (AppUserRole augr : getRoles())
			if (augr.getRole().getLookupType() == type)
				return true;
		return false;
	}

	public void addRole(Role role) {
		getRoles().add(new AppUserRole(this, role));
	}

	public void removeRole(Role role) {
		for (Iterator<AppUserRole> it = getRoles().iterator(); it.hasNext();) {
			if (it.next().getRole().equals(role)) {
				it.remove();
				break;
			}
		}
	}

	@Transient
	@JsonIgnore
	public SortedSet<Role> getBasicRoles() {
		Set<AppUserRole> aufrList = getRoles();
		SortedSet<Role> roles = new TreeSet<>();
		for (AppUserRole aufr : aufrList) {
			roles.add(aufr.getRole());
		}
		return roles;
	}

	@Transient
	@JsonView(AppUserView.Extended.class)
	public SortedSet<AppUserRole> getRolesSorted() {
		SortedSet<AppUserRole> results = new TreeSet<>(new CompareByRole());
		results.addAll(getRoles());
		return results;
	}

	@Override
	@Transient
	public boolean isAccountNonExpired() {
		return true;
	}

	/* Convenience / clarity - CPB */
	@Transient
	public boolean isAccountExpired() {
		return !isAccountNonExpired();
	}

	@Override
	@Transient
	public boolean isAccountNonLocked() {
		return true;
	}

	/* Convenience / clarity - CPB */
	@Transient
	public boolean isLocked() {
		return !isAccountNonLocked();
	}

	/* Expiration of credentials is managed external to the app */
	@Override
	@Transient
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Transient
	@JsonIgnore
	public AppUserPreferences getPreferences() {
		List<AppUserPreferences> l = getAppUserPreferencesList();
		return l.isEmpty() ? null : l.get(0);
	}

	public void setAppUserPreferences(AppUserPreferences preferences) {
		getAppUserPreferencesList().clear();
		if (preferences != null) {
			getAppUserPreferencesList().add(preferences);
			preferences.setAppUser(this);
		}
	}

	@Override
	@Transient
	@JsonIgnore
	public synchronized Collection<? extends GrantedAuthority> getAuthorities() {
		if (authoritiesCache == null) {
			Set<GrantedAuthority> authorities = new HashSet<GrantedAuthority>();
			for (Role r : getBasicRoles()) {
				if (r.isUsedAsPermission())
					authorities.add(new SimpleGrantedAuthority(r.getName()));
				for (Permission p : r.getPermissions())
					authorities.add(new SimpleGrantedAuthority(p.getName()));
			}
			this.authoritiesCache = Collections.unmodifiableSet(authorities);
		}

		return authoritiesCache;
	}

	// ------------------------------------ Common Methods

	@Override
	protected boolean requiredEquals(AppUser oo) {
		return new EqualsBuilder().append(username, oo.username).isEquals();
	}

	@Override
	protected int requiredHashCode() {
		return new HashCodeBuilder().append(username).toHashCode();
	}

	public int compareTo(AppUser u) {
		if (equals(u))
			return 0;
		return new CompareToBuilder().append(getDisplayName(), u.getDisplayName()).toComparison() > 0 ? 1 : -1;
	}

	@Override
	public String toString() {
		return getDisplayName();
	}

	// ------------------------------------ Accessor Methods

	@Column(nullable = false)
	@NotNull
	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	@JsonSerialize(using = ZoneIdSerializer.class)
	public ZoneId getTimeZone() {
		return timeZone;
	}

	public void setTimeZone(ZoneId timeZone) {
		this.timeZone = timeZone;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String givenName) {
		this.firstName = givenName;
	}

	public String getMiddleName() {
		return middleName;
	}

	public void setMiddleName(String middleName) {
		this.middleName = middleName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String familyName) {
		this.lastName = familyName;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Column(name = "Phone")
	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	@OneToMany(mappedBy = "appUser", fetch = FetchType.LAZY)
	@BatchSize(size = 500)
	@JsonIgnore
	public Set<AppUserRole> getRoles() {
		if (roles == null)
			roles = new HashSet<>();
		return roles;
	}

	public void setRoles(Set<AppUserRole> roles) {
		this.roles = roles;
	}

	@Column(name = "EnabledInd", nullable = false)
	@Type(type = "yes_no")
	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	@Column(name = "PasswordHash", insertable = false, updatable = false)
	@JsonIgnore
	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY, mappedBy = "appUser")
	@BatchSize(size = 500)
	@JsonIgnore
	public List<AppUserPreferences> getAppUserPreferencesList() {
		if (appUserPreferencesList == null)
			appUserPreferencesList = new ArrayList<>();
		return appUserPreferencesList;
	}

	@SuppressWarnings("unused")
	private void setAppUserPreferencesList(List<AppUserPreferences> appUserPreferencesList) {
		this.appUserPreferencesList = appUserPreferencesList;
	}

}
