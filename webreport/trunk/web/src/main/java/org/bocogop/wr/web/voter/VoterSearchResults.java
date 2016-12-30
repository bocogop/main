package org.bocogop.wr.web.voter;

import java.util.SortedSet;

import org.bocogop.wr.model.voter.Voter;

public class VoterSearchResults {

	public static class Parameters {
		public String voterId;
		public String firstName;
		public String lastName;
		public String email;
		public Integer birthYear;

		public Parameters(String voterId, String firstName, String lastName, String email, Integer birthYear) {
			this.voterId = voterId;
			this.firstName = firstName;
			this.lastName = lastName;
			this.email = email;
			this.birthYear = birthYear;
		}

	}

	public SortedSet<Voter> voters;
	public Parameters params;

	public VoterSearchResults(SortedSet<Voter> voters, Parameters params) {
		this.voters = voters;
		this.params = params;
	}

}
