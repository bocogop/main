package org.bocogop.wr.web.voter;

import java.io.Serializable;

public class VoterSearchParams implements Serializable {
	private static final long serialVersionUID = 2786795572960254973L;

	// ----------------------------------- Fields

	private String voterId;
	private String lastName;
	private String firstName;
	private Integer birthYear;
	private String email;

	// ----------------------------------- Constructors

	public VoterSearchParams() {
	}

	public VoterSearchParams(String voterId, String lastName, String firstName, Integer birthYear, String email) {
		this.voterId = voterId;
		this.lastName = lastName;
		this.firstName = firstName;
		this.birthYear = birthYear;
		this.email = email;
	}

	// ----------------------------------- Accessor Methods

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getVoterId() {
		return voterId;
	}

	public void setVoterId(String voterId) {
		this.voterId = voterId;
	}

	public Integer getBirthYear() {
		return birthYear;
	}

	public void setBirthYear(Integer birthYear) {
		this.birthYear = birthYear;
	}

}
