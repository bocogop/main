package org.bocogop.wr.web.volunteer;

import java.time.LocalDate;
import java.util.SortedSet;

import org.bocogop.wr.model.volunteer.Volunteer;

public class VolunteerSearchResults {

	public static class Parameters {
		public String firstName;
		public String lastName;
		public String code;
		public String email;
		public LocalDate dob;
		public String scope;
		public Long facilityId;
		public boolean includeInactive;

		public Parameters(String firstName, String lastName, String code, String email, LocalDate dob, String scope,
				Long facilityId, boolean includeInactive) {
			this.firstName = firstName;
			this.lastName = lastName;
			this.code = code;
			this.email = email;
			this.dob = dob;
			this.scope = scope;
			this.facilityId = facilityId;
			this.includeInactive = includeInactive;
		}

	}

	public SortedSet<Volunteer> volunteers;
	public long facilityId;
	public Parameters params;

	public VolunteerSearchResults(SortedSet<Volunteer> volunteers, long facilityId, Parameters params) {
		this.volunteers = volunteers;
		this.facilityId = facilityId;
		this.params = params;
	}

}
