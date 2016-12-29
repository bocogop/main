package org.bocogop.wr.web.volunteer;

import java.io.Serializable;
import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import org.bocogop.wr.util.DateUtil;

public class VolunteerSearchParams implements Serializable {
	private static final long serialVersionUID = 2786795572960254973L;
	
	// ----------------------------------- Fields
	
	private String lastName;
	private String firstName;
	private String code;
	@DateTimeFormat(pattern = DateUtil.TWO_DIGIT_DATE_ONLY)
	private LocalDate dob;
	private String email;

	// ----------------------------------- Constructors

	public VolunteerSearchParams() {
	}

	public VolunteerSearchParams(String lastName, String firstName, String code, LocalDate dob, String email) {
		this.lastName = lastName;
		this.firstName = firstName;
		this.code = code;
		this.dob = dob;
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
	
	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public LocalDate getDob() {
		return dob;
	}

	public void setDob(LocalDate dob) {
		this.dob = dob;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}


}
