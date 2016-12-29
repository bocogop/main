package org.bocogop.wr.model.facility;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.PUBLIC_ONLY;

import javax.persistence.DiscriminatorColumn;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;

import org.bocogop.shared.model.lookup.sds.State;
import org.bocogop.wr.model.volunteer.VolunteerAssignment.VolunteerAssignmentView;

@Entity
@Inheritance
@Table(name = "Facility", schema = "wr")
@DiscriminatorColumn(name = "Type")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = PUBLIC_ONLY, isGetterVisibility = PUBLIC_ONLY)
public abstract class AbstractUpdateableLocation<T extends AbstractUpdateableLocation<T>> extends AbstractLocation {
	private static final long serialVersionUID = 3810180355498406255L;

	// ---------------------------------------- Constructors

	protected AbstractUpdateableLocation() {
	}

	protected AbstractUpdateableLocation(String name, String addressLine1, String addressLine2, String city,
			State state, String zip, AbstractUpdateableLocation<?> parent) {
		super(name, addressLine1, addressLine2, city, state, zip, parent);
	}

	// ---------------------------------------- Business Methods

	/**
	 * Walk up the tree and get the nearest Facility ancestor - CPB
	 */
	@Transient
	@JsonIgnore
	public abstract Facility getFacility();

	@JsonView(BasicLocationView.Extended.class)
	@Transient
	public String getStationNumber() {
		Facility f = getFacility();
		return f == null ? null : f.getStationNumber();
	}

	@Transient
	@JsonView(BasicLocationView.Basic.class)
	public long getRootFacilityId() {
		return getFacility().getId();
	}

	@Transient
	@JsonView(VolunteerAssignmentView.Search.class)
	public String getRootFacilityDisplayName() {
		return getFacility().getDisplayName();
	}

}
