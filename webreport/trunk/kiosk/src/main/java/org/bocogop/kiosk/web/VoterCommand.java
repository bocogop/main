package org.bocogop.kiosk.web;

import org.bocogop.shared.model.voter.Voter;

public class VoterCommand {

	// -------------------------------- Fields

	private Voter voter;

	// -------------------------------- Constructors

	public VoterCommand() {
	}

	public VoterCommand(Voter voter) {
		this.voter = voter;
	}

	// -------------------------------- Business Methods

	// -------------------------------- Accessor Methods

	public Voter getVoter() {
		return voter;
	}

	public void setVoter(Voter voter) {
		this.voter = voter;
	}

}
