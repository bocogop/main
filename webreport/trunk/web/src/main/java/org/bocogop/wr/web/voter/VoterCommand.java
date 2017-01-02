package org.bocogop.wr.web.voter;

import org.bocogop.shared.model.voter.Voter;

public class VoterCommand {

	// -------------------------------- Fields

	private Voter voter;
	private String fromPage;

	// -------------------------------- Constructors

	public VoterCommand() {
	}

	public VoterCommand(Voter voter, String fromPage) {
		this.voter = voter;
		this.fromPage = fromPage;
	}

	// -------------------------------- Business Methods

	public String getTitleStatus() {
		return null;
	}

	// -------------------------------- Accessor Methods

	public Voter getVoter() {
		return voter;
	}

	public void setVoter(Voter voter) {
		this.voter = voter;
	}

	public String getFromPage() {
		return fromPage;
	}

	public void setFromPage(String fromPage) {
		this.fromPage = fromPage;
	}

}
