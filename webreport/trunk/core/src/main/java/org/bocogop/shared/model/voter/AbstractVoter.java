package org.bocogop.shared.model.voter;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.PUBLIC_ONLY;

import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;

import org.bocogop.shared.model.lookup.Gender;
import org.bocogop.shared.model.lookup.Party;
import org.bocogop.shared.model.precinct.Precinct;
import org.bocogop.shared.model.voter.Voter.VoterView;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;

@MappedSuperclass
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = PUBLIC_ONLY, isGetterVisibility = PUBLIC_ONLY)
public abstract class AbstractVoter<T extends AbstractVoter<T>> extends AbstractSimpleVoter<T> {
	private static final long serialVersionUID = 3222064615857480112L;

	// -------------------------------------- Fields

	private String driversLicense;
	private String ssn;

	private String houseNumber;
	private String houseSuffix;
	private String preDirection;
	private String streetName;
	private String streetType;
	private String postDirection;
	private String unitType;
	private String unitNumber;

	private Boolean idRequired;
	private Boolean uocava;
	private String issueMethod;

	private Precinct precinct;
	private Party party;
	private Gender gender;

	// -------------------------------------- Constructors

	protected AbstractVoter() {
	}

	protected AbstractVoter(String lastName, String firstName, String middleName, String nameSuffix) {
		super(lastName, firstName, middleName, nameSuffix);
	}

	// -------------------------------------- Business Methods

	// -------------------------------------- Common Methods

	// -------------------------------------- Accessor Methods

	@Column(length = 30)
	@JsonIgnore // until we have this data
	public String getDriversLicense() {
		return driversLicense;
	}

	public void setDriversLicense(String driversLicense) {
		this.driversLicense = driversLicense;
	}

	@Column(length = 9)
	@JsonIgnore // until we have this data
	public String getSsn() {
		return ssn;
	}

	public void setSsn(String ssn) {
		this.ssn = ssn;
	}

	@Column(length = 10)
	@JsonIgnore
	public String getHouseNumber() {
		return houseNumber;
	}

	public void setHouseNumber(String houseNumber) {
		this.houseNumber = houseNumber;
	}

	@Column(length = 20)
	@JsonIgnore
	public String getHouseSuffix() {
		return houseSuffix;
	}

	public void setHouseSuffix(String houseSuffix) {
		this.houseSuffix = houseSuffix;
	}

	@Column(length = 5)
	@JsonIgnore
	public String getPreDirection() {
		return preDirection;
	}

	public void setPreDirection(String preDirection) {
		this.preDirection = preDirection;
	}

	@Column(length = 255)
	@JsonIgnore
	public String getStreetName() {
		return streetName;
	}

	public void setStreetName(String streetName) {
		this.streetName = streetName;
	}

	@Column(length = 10)
	@JsonIgnore
	public String getStreetType() {
		return streetType;
	}

	public void setStreetType(String streetType) {
		this.streetType = streetType;
	}

	@Column(length = 255)
	@JsonIgnore
	public String getPostDirection() {
		return postDirection;
	}

	public void setPostDirection(String postDirection) {
		this.postDirection = postDirection;
	}

	@Column(length = 20)
	@JsonIgnore
	public String getUnitType() {
		return unitType;
	}

	public void setUnitType(String unitType) {
		this.unitType = unitType;
	}

	@Column(length = 15)
	@JsonIgnore
	public String getUnitNumber() {
		return unitNumber;
	}

	public void setUnitNumber(String unitNumber) {
		this.unitNumber = unitNumber;
	}

	@JsonIgnore
	public Boolean getIdRequired() {
		return idRequired;
	}

	public void setIdRequired(Boolean idRequired) {
		this.idRequired = idRequired;
	}

	@JsonIgnore
	public Boolean getUocava() {
		return uocava;
	}

	public void setUocava(Boolean uocava) {
		this.uocava = uocava;
	}

	@Column(length = 255)
	@JsonIgnore
	public String getIssueMethod() {
		return issueMethod;
	}

	public void setIssueMethod(String issueMethod) {
		this.issueMethod = issueMethod;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "PrecinctFK")
	@JsonView({ VoterView.Extended.class, VoterView.Demographics.class })
	public Precinct getPrecinct() {
		return precinct;
	}

	public void setPrecinct(Precinct precinct) {
		this.precinct = precinct;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "PartyFK")
	@JsonView({ VoterView.Extended.class, VoterView.Demographics.class })
	public Party getParty() {
		return party;
	}

	public void setParty(Party party) {
		this.party = party;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "GenderFK")
	public Gender getGender() {
		return gender;
	}

	public void setGender(Gender gender) {
		this.gender = gender;
	}

}
