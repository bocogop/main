package org.bocogop.wr.web.voter;

import java.time.LocalDate;
import java.util.SortedSet;

import org.bocogop.wr.model.voter.Voter;

public class VoterSearchResults {

	public static class Parameters {
		public String firstName;
		public String lastName;
		public String code;
		public String email;
		public LocalDate dob;
		public String scope;
		public boolean includeInactive;

		public Parameters(String firstName, String lastName, String code, String email, LocalDate dob, String scope,
				boolean includeInactive) {
			this.firstName = firstName;
			this.lastName = lastName;
			this.code = code;
			this.email = email;
			this.dob = dob;
			this.scope = scope;
			this.includeInactive = includeInactive;
		}

	}

	public SortedSet<Voter> voters;
	public Parameters params;

	public VoterSearchResults(SortedSet<Voter> voters, Parameters params) {
		this.voters = voters;
		this.params = params;
	}

}
