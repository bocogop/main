package org.bocogop.shared.model.voter;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.PUBLIC_ONLY;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = PUBLIC_ONLY, isGetterVisibility = PUBLIC_ONLY)
public class VoterDemographics extends AbstractSimpleVoter<VoterDemographics> {
	private static final long serialVersionUID = -556040665741732416L;

	// -------------------------------------------- Fields

	private String precinct;
	private String party;
	private String gender;

	// add fields here specific to the demographics if needed - CPB

	public VoterDemographics() {
	}

	// -------------------------------------------- Business Methods

	// -------------------------------------------- Accessor Methods

	public String getPrecinct() {
		return precinct;
	}

	public void setPrecinct(String precinct) {
		this.precinct = precinct;
	}

	public String getParty() {
		return party;
	}

	public void setParty(String party) {
		this.party = party;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

}
