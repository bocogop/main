package org.bocogop.wr.model.requirement;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.PUBLIC_ONLY;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

import org.bocogop.wr.model.facility.Facility;

@Entity
@DiscriminatorValue("G")
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = PUBLIC_ONLY, isGetterVisibility = PUBLIC_ONLY)
public class GlobalRequirement extends AbstractRequirement {
	private static final long serialVersionUID = 5406082811830107442L;

	@Override
	@Transient
	public Facility getFacilityScope() {
		return null;
	}

	@Override
	@Transient
	public RequirementApplicationType getApplicationType() {
		return RequirementApplicationType.ALL_VOLUNTEERS;
	}
	
}
