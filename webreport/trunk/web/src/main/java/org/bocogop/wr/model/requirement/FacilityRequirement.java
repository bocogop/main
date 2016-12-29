package org.bocogop.wr.model.requirement;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.PUBLIC_ONLY;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

import org.bocogop.wr.model.facility.Facility;

@Entity
@DiscriminatorValue("F")
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = PUBLIC_ONLY, isGetterVisibility = PUBLIC_ONLY)
public class FacilityRequirement extends AbstractRequirement {
	private static final long serialVersionUID = 5406082811830107442L;

	// ----------------------------- Fields

	private Facility facility;

	// ----------------------------- Business Methods

	@Override
	@Transient
	public Facility getFacilityScope() {
		return getFacility();
	}

	@Override
	@Transient
	public RequirementApplicationType getApplicationType() {
		return RequirementApplicationType.ALL_VOLUNTEERS;
	}
	
	// ----------------------------- Accessor Methods

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "FacilityFK")
	public Facility getFacility() {
		return facility;
	}

	public void setFacility(Facility facility) {
		this.facility = facility;
	}

}
