package org.bocogop.shared.model;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.PUBLIC_ONLY;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
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

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Type;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;

import org.bocogop.shared.model.core.AbstractAuditedPersistent;
import org.bocogop.shared.model.lookup.sds.VAFacility;

@Entity
@Table(name = "APP_USER_FACILITY", schema = "CORE")
@AttributeOverrides({ @AttributeOverride(name = "id", column = @Column(name = "APP_USER_FACILITY_ID") ) })
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = PUBLIC_ONLY, isGetterVisibility = PUBLIC_ONLY)
public class AppUserFacility extends AbstractAuditedPersistent<AppUserFacility> {
	private static final long serialVersionUID = 1L;

	/*
	 * Exposing this functionality on a per-request basis since the facility
	 * field may be lazy-loaded - CPB
	 */
	public static class CompareByVAFacility implements Comparator<AppUserFacility> {
		@Override
		public int compare(AppUserFacility o1, AppUserFacility o2) {
			if (o1.equals(o2))
				return 0;
			return new CompareToBuilder()
					.append(o1 == null ? null : o1.getFacility(), o2 == null ? null : o2.getFacility())
					.toComparison() > 0 ? 1 : -1;
		}
	}

	// ------------------------------------- Fields

	private AppUser appUser;
	private VAFacility facility;
	private boolean primaryFacility;
	private boolean rolesCustomizedForFacility;
	private List<AppUserFacilityRole> appUserFacilityRoles;

	// ------------------------------------- Constructors

	public AppUserFacility() {
	}

	public AppUserFacility(AppUser user, VAFacility w) {
		this.appUser = user;
		this.facility = w;
	}

	// ------------------------------------- Business Methods

	public void initializeAll() {
		/*
		 * This call relies on L2 cache to be performant - Hibernate doesn't
		 * Batch-load all children here for some reason - CPB
		 */
		initialize(getFacility());

		initialize(getAppUserFacilityRoles());
		for (AppUserFacilityRole aufr : getAppUserFacilityRoles())
			aufr.initializeAll();
	}

	@Transient
	public SortedSet<Role> getRoles() {
		List<AppUserFacilityRole> aufrList = getAppUserFacilityRoles();
		SortedSet<Role> roles = new TreeSet<>();
		for (AppUserFacilityRole aufr : aufrList) {
			roles.add(aufr.getRole());
		}
		return roles;
	}

	// ------------------------------------- Common Methods

	@Override
	protected boolean requiredEquals(AppUserFacility oo) {
		return new EqualsBuilder().append(nullSafeGetId(getAppUser()), nullSafeGetId(oo.getAppUser()))
				.append(nullSafeGetId(getFacility()), nullSafeGetId(oo.getFacility())).isEquals();
	}

	@Override
	protected int requiredHashCode() {
		return new HashCodeBuilder().append(nullSafeGetId(getAppUser())).append(nullSafeGetId(getFacility()))
				.toHashCode();
	}

	// ------------------------------------- Accessor Methods

	@Column(name = "ROLES_CUSTOMIZED_IND")
	@Type(type = "yes_no")
	public boolean isRolesCustomizedForFacility() {
		return rolesCustomizedForFacility;
	}

	public void setRolesCustomizedForFacility(boolean rolesCustomizedForFacility) {
		this.rolesCustomizedForFacility = rolesCustomizedForFacility;
	}

	@Column(name = "PRIMARY_FACILITY_IND")
	@Type(type = "yes_no")
	public boolean isPrimaryFacility() {
		return primaryFacility;
	}

	public void setPrimaryFacility(boolean primaryFacility) {
		this.primaryFacility = primaryFacility;
	}

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "APP_USER_ID", nullable = false)
	@JsonIgnore
	public AppUser getAppUser() {
		return appUser;
	}

	public void setAppUser(AppUser appUser) {
		this.appUser = appUser;
	}

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "FACILITY_ID", updatable = false)
	@BatchSize(size = 500)
	public VAFacility getFacility() {
		return facility;
	}

	public void setFacility(VAFacility facility) {
		this.facility = facility;
	}

	@OneToMany(mappedBy = "appUserFacility", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
	@BatchSize(size = 2000)
	public List<AppUserFacilityRole> getAppUserFacilityRoles() {
		if (appUserFacilityRoles == null)
			appUserFacilityRoles = new ArrayList<>();
		return appUserFacilityRoles;
	}

	public void setAppUserFacilityRoles(List<AppUserFacilityRole> appUserFacilityRoles) {
		this.appUserFacilityRoles = appUserFacilityRoles;
	}

}
