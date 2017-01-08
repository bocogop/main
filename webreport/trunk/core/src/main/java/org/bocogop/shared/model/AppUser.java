package org.bocogop.shared.model;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.PUBLIC_ONLY;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
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
import org.bocogop.shared.model.AppUserGlobalRole.CompareByRole;
import org.bocogop.shared.model.Role.RoleType;
import org.bocogop.shared.model.precinct.Precinct;
import org.bocogop.shared.util.StringUtil;
import org.bocogop.shared.web.conversion.ZoneIdSerializer;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Type;
import org.springframework.security.core.GrantedAuthority;

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
	private String telephoneNumber;
	private String email;
	/* Has someone completely disabled this user from logging in */
	private boolean enabled;

	private Set<AppUserGlobalRole> globalRoles;
	private Set<AppUserPrecinct> precincts;

	/*
	 * Only contains one item, lazy-loaded; mapping like this instead
	 * of @OneToOne so it can be optional and still lazy-load - CPB
	 */
	private List<AppUserPreferences> appUserPreferencesList;

	// ------------------------------------ Constructors

	public AppUser() {
	}

	public AppUser(String username) {
		this.username = username;
	}

	// ------------------------------------ Business Methods

	public void initializeAll() {
		initialize(getGlobalRoles());
		initialize(getPrecincts());
		initialize(getAppUserPreferencesList());

		for (AppUserGlobalRole augr : getGlobalRoles()) {
			augr.initializeAll();
		}

		for (AppUserPrecinct f : getPrecincts())
			f.initializeAll();
	}

	/**
	 * Returns the set of stations explicitly assigned to the user. No custom
	 * logic is performed here for National administrators, only their
	 * explicitly assigned stations will be returned (or an empty Set if none).
	 */
	@Transient
	@JsonIgnore
	public SortedSet<Precinct> getAssignedPrecincts() {
		SortedSet<Precinct> precincts = new TreeSet<>();
		for (AppUserPrecinct a : getPrecincts()) {
			Precinct assignedPrecinct = a.getPrecinct();
			precincts.add(assignedPrecinct);
		}
		return precincts;
	}

	public boolean isAssignedPrecinct(long precinctId) {
		for (AppUserPrecinct a : getPrecincts())
			if (a.getPrecinct().getId() == precinctId)
				return true;
		return false;
	}

	public AppUserPrecinct getAppUserPrecinct(long precinctId) {
		// TODO fix this very soon.
		for (AppUserPrecinct f : getPrecincts())
			if (f.getPrecinct().getId() != null && precinctId == f.getPrecinct().getId())
				return f;
		return null;
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
		return RoleType.NATIONAL_ADMIN.isGloballyAssignedToUser(this);
	}

	public boolean hasGlobalRole(RoleType type) {
		for (AppUserGlobalRole augr : getGlobalRoles())
			if (augr.getRole().getLookupType() == type)
				return true;
		return false;
	}

	public void addGlobalRole(Role role) {
		getGlobalRoles().add(new AppUserGlobalRole(this, role));
	}

	public void removeGlobalRole(Role role) {
		for (Iterator<AppUserGlobalRole> it = getGlobalRoles().iterator(); it.hasNext();) {
			if (it.next().getRole().equals(role)) {
				it.remove();
				break;
			}
		}
	}

	@Transient
	@JsonIgnore
	public SortedSet<Role> getBasicGlobalRoles() {
		Set<AppUserGlobalRole> aufrList = getGlobalRoles();
		SortedSet<Role> roles = new TreeSet<>();
		for (AppUserGlobalRole aufr : aufrList) {
			roles.add(aufr.getRole());
		}
		return roles;
	}

	@Transient
	@JsonView(AppUserView.Extended.class)
	public SortedSet<AppUserGlobalRole> getGlobalRolesSorted() {
		SortedSet<AppUserGlobalRole> results = new TreeSet<>(new CompareByRole());
		results.addAll(getGlobalRoles());
		return results;
	}

	/*
	 * Safely returning nothing here, just to satisfy UserDetails contract. The
	 * actual Security context is manually reset with the AppUser's
	 * station-specific roles as they change stations in the app. CPB
	 */
	@Override
	@Transient
	@JsonIgnore
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return new ArrayList<>();
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
	@Transient
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
	public String getTelephoneNumber() {
		return telephoneNumber;
	}

	public void setTelephoneNumber(String telephoneNumber) {
		this.telephoneNumber = telephoneNumber;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	@OneToMany(mappedBy = "appUser", fetch = FetchType.LAZY)
	@BatchSize(size = 500)
	/* In lieu of GlobalRolesSorted - CPB */
	@JsonIgnore
	public Set<AppUserGlobalRole> getGlobalRoles() {
		if (globalRoles == null)
			globalRoles = new HashSet<>();
		return globalRoles;
	}

	public void setGlobalRoles(Set<AppUserGlobalRole> globalRoles) {
		this.globalRoles = globalRoles;
	}

	@OneToMany(mappedBy = "appUser", fetch = FetchType.LAZY)
	@BatchSize(size = 500)
	@JsonView(AppUserView.Extended.class)
	public Set<AppUserPrecinct> getPrecincts() {
		if (precincts == null)
			precincts = new HashSet<>();
		return precincts;
	}

	public void setPrecincts(Set<AppUserPrecinct> precincts) {
		this.precincts = precincts;
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
