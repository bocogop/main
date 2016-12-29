package org.bocogop.wr.web.donation;

import java.io.Serializable;

import org.bocogop.shared.model.lookup.sds.State;

public class DonorSearchParams implements Serializable {

	// ----------------------------------- Fields

	/**
	 * 
	 */
	private static final long serialVersionUID = 5376374285038354043L;
	
	private long donorType;
	private String lastName;
	private String firstName;
	private String orgName;
	private String city;
	private State state;
	private String zip;
	private String email;
	private String phone;

	// ----------------------------------- Constructors

	public DonorSearchParams() {
	}

	public DonorSearchParams(long donorType, String lastName, String firstName, String orgName, String city,
			State state, String zip, String email, String phone) {
		this.donorType = donorType;
		this.lastName = lastName;
		this.firstName = firstName;
		this.orgName = orgName;
		this.city = city;
		this.state = state;
		this.zip = zip;
		this.email = email;
		this.phone = phone;
	}

	// ----------------------------------- Accessor Methods

	public long getDonorType() {
		return donorType;
	}

	public void setDonorType(long donorType) {
		this.donorType = donorType;
	}

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
	
	public String getOrgName() {
		return orgName;
	}

	public void setOrgName(String orgName) {
		this.orgName = orgName;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public State getState() {
		return state;
	}

	public void setState(State state) {
		this.state = state;
	}

	public String getZip() {
		return zip;
	}

	public void setZip(String zip) {
		this.zip = zip;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}



}
