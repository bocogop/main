package org.bocogop.wr.model.views;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.PUBLIC_ONLY;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.hibernate.annotations.Immutable;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

import org.bocogop.shared.model.AppUser;
import org.bocogop.shared.model.Role;
import org.bocogop.shared.model.core.AbstractDerivedPersistent;
import org.bocogop.shared.model.lookup.sds.VAFacility;
import org.bocogop.wr.model.facility.Facility;

@Entity
@Immutable
@Table(name = "UserFacilityRole", schema = "dbo")
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = PUBLIC_ONLY, isGetterVisibility = PUBLIC_ONLY)
public class UserFacilityRole extends AbstractDerivedPersistent<UserFacilityRole> {
	private static final long serialVersionUID = 3254887002195956807L;

	// --------------------------------------------- Fields

	private String syntheticPK;

	private AppUser user;
	private VAFacility vaFacility;
	private Role role;
	private Facility facility;

	// ---------------------------------------- Business Methods

	// ---------------------------------------- Common Methods

	@Override
	protected boolean requiredEquals(UserFacilityRole oo) {
		return new EqualsBuilder().append(getSyntheticPK(), oo.getSyntheticPK()).isEquals();
	}

	@Override
	protected int requiredHashCode() {
		return new HashCodeBuilder().append(getSyntheticPK()).toHashCode();
	}

	// --------------------------------------------- Accessor Methods

	@Id
	public String getSyntheticPK() {
		return syntheticPK;
	}

	public void setSyntheticPK(String syntheticPK) {
		this.syntheticPK = syntheticPK;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "APP_USER_ID", nullable = false)
	public AppUser getUser() {
		return user;
	}

	public void setUser(AppUser user) {
		this.user = user;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "FACILITY_ID", nullable = false)
	public VAFacility getVaFacility() {
		return vaFacility;
	}

	public void setVaFacility(VAFacility facility) {
		this.vaFacility = facility;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "WR_FACILITY_ID")
	public Facility getFacility() {
		return facility;
	}

	public void setFacility(Facility facility) {
		this.facility = facility;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ROLE_ID", nullable = false)
	public Role getRole() {
		return role;
	}

	public void setRole(Role role) {
		this.role = role;
	}

}
