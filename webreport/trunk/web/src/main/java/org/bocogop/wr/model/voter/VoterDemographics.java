package org.bocogop.wr.model.voter;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.PUBLIC_ONLY;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = PUBLIC_ONLY, isGetterVisibility = PUBLIC_ONLY)
public class VoterDemographics extends AbstractSimpleVoter<VoterDemographics> {
	private static final long serialVersionUID = -556040665741732416L;

	// -------------------------------------------- Fields

	// add fields here specific to the demographics if needed - CPB

	public VoterDemographics() {
	}

	// -------------------------------------------- Business Methods

	// -------------------------------------------- Accessor Methods

}
