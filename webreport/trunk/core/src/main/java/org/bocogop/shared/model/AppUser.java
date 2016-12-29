package org.bocogop.shared.model;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.PUBLIC_ONLY;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Type;
import org.springframework.security.core.GrantedAuthority;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import org.bocogop.shared.model.AppUserGlobalRole.CompareByRole;
import org.bocogop.shared.model.Permission.PermissionType;
import org.bocogop.shared.model.Role.RoleType;
import org.bocogop.shared.model.core.AbstractAuditedVersionedPersistent;
import org.bocogop.shared.model.ldap.LdapPerson;
import org.bocogop.shared.model.lookup.InactiveReason;
import org.bocogop.shared.model.lookup.sds.VAFacility;
import org.bocogop.shared.util.SecurityUtil;
import org.bocogop.shared.util.StringUtil;
import org.bocogop.shared.web.conversion.ZoneIdSerializer;

@Entity
@Table(name = "APP_USER", schema = "CORE")
@AttributeOverrides({ @AttributeOverride(name = "id", column = @Column(name = "APP_USER_ID")) })
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = PUBLIC_ONLY, isGetterVisibility = PUBLIC_ONLY)
public class AppUser extends AbstractAuditedVersionedPersistent<AppUser>
		implements CoreUserDetails, Comparable<AppUser> {
	private static final long serialVersionUID = 1L;

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
	private String title;
	private String department;
	private String description;
	private String office;
	private String telephoneNumber;
	private String email;

	/*
	 * Roles which apply to all stations that don't have an explicit set of
	 * Roles defined. These are stored in the AppUserFacility objects in the
	 * facilities Map below.
	 */
	private Set<AppUserGlobalRole> globalRoles;

	/* Key = facility ID, value = AppUserFacility */
	private Set<AppUserFacility> facilities;
	private VAFacility lastVisitedFacility;

	/* Has someone completely disabled this user from logging in */
	private boolean enabled;

	/*
	 * A count of how many times the user attempted to login incorrectly. This
	 * is often reset to 0 after a successful login.
	 */
	private Integer failedLoginCount;
	/*
	 * If non-null, the account is locked as of this date; if null, the account
	 * is not locked.
	 */
	private ZonedDateTime accountLockDate;
	/*
	 * The time of the last failed login (helpful if auto-unlocking after X
	 * minutes)
	 */
	private ZonedDateTime lastFailedLoginDate;

	/* The last time this user logged in */
	private ZonedDateTime lastSuccessfulLoginDate;

	/* Has this user been inactivated? */
	private boolean inactive = false;
	/* When were they inactivated */
	private ZonedDateTime inactiveDate = null;
	/* Why were they inactivated */
	private InactiveReason inactiveReason = null;

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

	public AppUser(LdapPerson person) {
		setFromLdapPerson(person);
		this.timeZone = ZoneId.systemDefault();
	}

	// ------------------------------------ Business Methods

	public void initializeAll() {
		initialize(getGlobalRoles());
		initialize(getFacilities());
		initialize(getLastVisitedFacility());
		initialize(getInactiveReason());
		initialize(getAppUserPreferencesList());

		for (AppUserGlobalRole augr : getGlobalRoles()) {
			augr.initializeAll();
		}

		for (AppUserFacility f : getFacilities())
			f.initializeAll();
	}

	public void lock(ZonedDateTime asOf) {
		setAccountLockDate(asOf);
	}

	public void unlock() {
		setAccountLockDate(null);
		setFailedLoginCount(0);
	}

	public void inactivate(ZonedDateTime now, InactiveReason inactiveReason) {
		setInactive(true);
		setInactiveDate(ZonedDateTime.now());
		setInactiveReason(inactiveReason);
	}

	public void activate() {
		setInactive(false);
		setInactiveDate(null);
		setInactiveReason(null);
	}

	/**
	 * Returns the set of stations explicitly assigned to the user. No custom
	 * logic is performed here for National administrators, only their
	 * explicitly assigned stations will be returned (or an empty Set if none).
	 */
	@Transient
	@JsonIgnore
	public SortedSet<VAFacility> getAssignedVAFacilities() {
		SortedSet<VAFacility> facilities = new TreeSet<>();
		for (AppUserFacility a : getFacilities()) {
			VAFacility assignedFacility = a.getFacility();
			facilities.add(assignedFacility);
		}
		return facilities;
	}

	public boolean isAssignedFacility(long facilityId) {
		for (AppUserFacility a : getFacilities())
			if (a.getFacility().getId() == facilityId)
				return true;
		return false;
	}

	public AppUserFacility getAppUserFacility(long facilityId) {
		// TODO fix this very soon.
		for (AppUserFacility f : getFacilities())
			if (f.getFacility().getId() != null && facilityId == f.getFacility().getId())
				return f;
		return null;
	}

	/**
	 * @param ldapPerson
	 *            the ldapPerson to set
	 */
	public void setFromLdapPerson(LdapPerson ldapPerson) {
		if (ldapPerson != null) {
			setLastName(ldapPerson.getLastName());
			setFirstName(ldapPerson.getFirstName());
			setMiddleName(ldapPerson.getMiddleName());
			setTitle(ldapPerson.getTitle());
			setDepartment(ldapPerson.getDepartment());
			setDescription(ldapPerson.getDescription());
			setOffice(ldapPerson.getOffice());
			setEmail(ldapPerson.getEmail());
			setTelephoneNumber(ldapPerson.getTelephoneNumber());
			setUsername(ldapPerson.getSamAccountName());
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

	@Transient
	public SortedSet<Role> getRolesForFacility(long vaFacilityId) {
		AppUserFacility auf = getAppUserFacility(vaFacilityId);
		if (auf == null)
			return new TreeSet<>();
		if (!auf.isRolesCustomizedForFacility())
			return getBasicGlobalRoles();
		return auf.getRoles();
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthoritiesAtFacility(long facilityId) {
		return SecurityUtil.getUnmodifiableCombinedAuthorities(getRolesForFacility(facilityId));
	}

	public Set<VAFacility> getFacilitiesWhereUserHasAllPermissions(PermissionType... permissionsRequired) {
		Set<VAFacility> results = new HashSet<VAFacility>();

		for (AppUserFacility auf : getFacilities()) {
			if (SecurityUtil.hasAllPermissionsAtFacility(auf.getFacility().getId(), permissionsRequired))
				results.add(auf.getFacility());
		}
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
		return !isInactive();
	}

	/* Convenience / clarity - CPB */
	@Transient
	public boolean isAccountExpired() {
		return !isAccountNonExpired();
	}

	@Override
	@Transient
	public boolean isAccountNonLocked() {
		return accountLockDate == null;
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

	@Transient
	@Override
	@JsonIgnore
	public boolean isSoundsEnabled() {
		return getPreferences().isSoundsEnabled();
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

	@Column(name = "USERNAME")
	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	@Column(name = "TIME_ZONE")
	@JsonSerialize(using = ZoneIdSerializer.class)
	public ZoneId getTimeZone() {
		return timeZone;
	}

	public void setTimeZone(ZoneId timeZone) {
		this.timeZone = timeZone;
	}

	@Column(name = "FIRST_NAME")
	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String givenName) {
		this.firstName = givenName;
	}

	@Column(name = "MIDDLE_NAME")
	public String getMiddleName() {
		return middleName;
	}

	public void setMiddleName(String middleName) {
		this.middleName = middleName;
	}

	@Column(name = "LAST_NAME")
	public String getLastName() {
		return lastName;
	}

	public void setLastName(String familyName) {
		this.lastName = familyName;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDepartment() {
		return department;
	}

	public void setDepartment(String department) {
		this.department = department;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getOffice() {
		return office;
	}

	public void setOffice(String office) {
		this.office = office;
	}

	@Column(name = "PHONE")
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
	public Set<AppUserFacility> getFacilities() {
		if (facilities == null)
			facilities = new HashSet<>();
		return facilities;
	}

	public void setFacilities(Set<AppUserFacility> facilities) {
		this.facilities = facilities;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "LAST_VISITED_FACILITY_ID")
	@BatchSize(size = 500)
	public VAFacility getLastVisitedFacility() {
		return lastVisitedFacility;
	}

	public void setLastVisitedFacility(VAFacility lastVisitedFacility) {
		this.lastVisitedFacility = lastVisitedFacility;
	}

	@Column(name = "ENABLED_IND", nullable = false)
	@Type(type = "yes_no")
	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	@Column(name = "ACCOUNT_LOCK_DATE")
	public ZonedDateTime getAccountLockDate() {
		return accountLockDate;
	}

	public void setAccountLockDate(ZonedDateTime accountLockDate) {
		this.accountLockDate = accountLockDate;
	}

	@Column(name = "FAILED_LOGIN_COUNT")
	public Integer getFailedLoginCount() {
		return failedLoginCount;
	}

	public void setFailedLoginCount(Integer failedLoginCount) {
		this.failedLoginCount = failedLoginCount;
	}

	@Column(name = "LAST_SUCCESSFUL_LOGIN_DATE")
	public ZonedDateTime getLastSuccessfulLoginDate() {
		return lastSuccessfulLoginDate;
	}

	public void setLastSuccessfulLoginDate(ZonedDateTime lastAccessedDate) {
		this.lastSuccessfulLoginDate = lastAccessedDate;
	}

	@Column(name = "INACTIVE_IND", nullable = false)
	@Type(type = "yes_no")
	public boolean isInactive() {
		return inactive;
	}

	public void setInactive(boolean inactive) {
		this.inactive = inactive;
	}

	@Column(name = "INACTIVE_DATE")
	public ZonedDateTime getInactiveDate() {
		return inactiveDate;
	}

	public void setInactiveDate(ZonedDateTime inactiveDate) {
		this.inactiveDate = inactiveDate;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "STD_INACTIVE_REASON_ID")
	public InactiveReason getInactiveReason() {
		return inactiveReason;
	}

	public void setInactiveReason(InactiveReason inactiveReason) {
		this.inactiveReason = inactiveReason;
	}

	@Column(name = "LAST_FAILED_LOGIN_DATE")
	public ZonedDateTime getLastFailedLoginDate() {
		return lastFailedLoginDate;
	}

	public void setLastFailedLoginDate(ZonedDateTime lastFailedLoginDate) {
		this.lastFailedLoginDate = lastFailedLoginDate;
	}

	@Column(name = "PASSWORD_HASH", insertable = false, updatable = false)
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
