package org.bocogop.shared.model.voter;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.PUBLIC_ONLY;

import java.time.LocalDate;

import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

import org.apache.commons.lang.WordUtils;
import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.bocogop.shared.model.AbstractAuditedVersionedPersistent;
import org.bocogop.shared.model.lookup.Gender;
import org.bocogop.shared.model.voter.Voter.VoterView;
import org.bocogop.shared.util.StringUtil;
import org.hibernate.validator.constraints.NotBlank;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonView;

@MappedSuperclass
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = PUBLIC_ONLY, isGetterVisibility = PUBLIC_ONLY)
public abstract class AbstractSimpleVoter<T extends AbstractSimpleVoter<T>>
		extends AbstractAuditedVersionedPersistent<Voter> implements Comparable<AbstractSimpleVoter<T>> {
	private static final long serialVersionUID = 3222064615857480112L;

	public static String getDisplayName(String firstName, String middleName, String lastName, String suffix,
			boolean lastFirst) {
		return WordUtils.capitalizeFully(StringUtil.getDisplayName(lastFirst, firstName, middleName, lastName, suffix));
	}

	public static String getDisplayName(String firstName, String middleName, String lastName, String suffix) {
		return getDisplayName(firstName, middleName, lastName, suffix, true);
	}

	// -------------------------------------- Fields

	private String voterId;

	@NotBlank
	private String firstName;
	private String middleName;
	@NotBlank
	private String lastName;
	private String suffix;
	private Gender gender;
	
	private String driversLicense;
	private String ssn;

	private LocalDate registrationDate;
	private LocalDate effectiveDate;

	private String phone;
	private String fax;
	private String email;
	
	private String houseNumber;
	private String houseSuffix;
	private String preDirection;
	private String streetName;
	private String streetType;
	private String postDirection;
	private String unitType;
	private String unitNumber;
	private String address;
	private String city;
	private String state;
	private String zip;
	private String zipPlus;
	
	private String mailingAddress1;
	private String mailingAddress2;
	private String mailingAddress3;
	private String mailingCity;
	private String mailingState;
	private String mailingZip;
	private String mailingZipPlus;
	private String mailingCountry;
	
	private String ballotAddress1;
	private String ballotAddress2;
	private String ballotAddress3;
	private String ballotCity;
	private String ballotState;
	private String ballotZip;
	private String ballotZipPlus;
	private String ballotCountry;
	
	private Boolean statusActive;
	private String statusReason;
	
	private LocalDate affiliatedDate;
	private Boolean idRequired;
	private Integer birthYear;
	private Boolean uocava;
	private String issueMethod;
	

	// -------------------------------------- Constructors

	protected AbstractSimpleVoter() {
	}

	protected AbstractSimpleVoter(String lastName, String firstName, String middleName, String nameSuffix) {
		this.lastName = lastName;
		this.firstName = firstName;
		this.middleName = middleName;
		this.suffix = nameSuffix;
	}

	// -------------------------------------- Business Methods

	@Transient
	public String getDisplayName(boolean lastFirst) {
		return getDisplayName(firstName, middleName, lastName, suffix, lastFirst);
	}

	@Transient
	@JsonView({ VoterView.Basic.class, //
	})
	public String getDisplayName() {
		return getDisplayName(true);
	}

	@Transient
	@JsonView({ VoterView.Search.class, VoterView.Demographics.class })
	public String getAddressMultilineDisplay() {
		return StringUtil.getAddressDisplay(getAddress(), null, null, getCity(), getState(), getZip(), "\n");
	}

	// -------------------------------------- Common Methods

	@Override
	protected boolean requiredEquals(Voter oo) {
		return new EqualsBuilder().append(getVoterId(), oo.getVoterId()).isEquals();
	}

	@Override
	protected int requiredHashCode() {
		return new HashCodeBuilder().append(getVoterId()).toHashCode();
	}

	@Override
	public int compareTo(AbstractSimpleVoter<T> o) {
		if (equals(o))
			return 0;

		return new CompareToBuilder().append(nullSafeLowercase(getDisplayName()), nullSafeLowercase(o.getDisplayName()))
				.toComparison() > 0 ? 1 : -1;
	}

	public String toString() {
		return getDisplayName() + " (voter ID " + getVoterId() + ")";
	}

	// -------------------------------------- Accessor Methods

	@Column(length = 100)
	@JsonView(VoterView.Extended.class)
	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	@Column(length = 100)
	@JsonView(VoterView.Extended.class)
	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	@Column(length = 100)
	@JsonView(VoterView.Extended.class)
	public String getMiddleName() {
		return middleName;
	}

	public void setMiddleName(String middleName) {
		this.middleName = middleName;
	}

	@Column(name = "NameSuffix", length = 20)
	@JsonView(VoterView.Extended.class)
	public String getSuffix() {
		return suffix;
	}

	public void setSuffix(String suffix) {
		this.suffix = suffix;
	}

	@Column(name = "ResidentialCity", length = 255)
	@JsonView({ VoterView.Extended.class, VoterView.Demographics.class })
	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	@Column(name = "ResidentialZip", length = 15)
	@JsonView({ VoterView.Extended.class, VoterView.Demographics.class })
	public String getZip() {
		return zip;
	}

	public void setZip(String zip) {
		this.zip = zip;
	}

	@Column(length = 30)
	@JsonView({ VoterView.Search.class, VoterView.Demographics.class })
	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	@Column(name = "Email", length = 255)
	@JsonView({ VoterView.Search.class, VoterView.Demographics.class })
	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	@Column(name = "ResidentialState", length = 2)
	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	@Column(length = 255)
	public String getVoterId() {
		return voterId;
	}

	public void setVoterId(String voterId) {
		this.voterId = voterId;
	}

	@Column(length = 30)
	public String getDriversLicense() {
		return driversLicense;
	}

	public void setDriversLicense(String driversLicense) {
		this.driversLicense = driversLicense;
	}

	@Column(length = 9)
	public String getSsn() {
		return ssn;
	}

	public void setSsn(String ssn) {
		this.ssn = ssn;
	}

	public LocalDate getRegistrationDate() {
		return registrationDate;
	}

	public void setRegistrationDate(LocalDate registrationDate) {
		this.registrationDate = registrationDate;
	}

	public LocalDate getEffectiveDate() {
		return effectiveDate;
	}

	public void setEffectiveDate(LocalDate effectiveDate) {
		this.effectiveDate = effectiveDate;
	}

	@Column(length = 10)
	public String getHouseNumber() {
		return houseNumber;
	}

	public void setHouseNumber(String houseNumber) {
		this.houseNumber = houseNumber;
	}

	@Column(length = 20)
	public String getHouseSuffix() {
		return houseSuffix;
	}

	public void setHouseSuffix(String houseSuffix) {
		this.houseSuffix = houseSuffix;
	}

	@Column(length = 5)
	public String getPreDirection() {
		return preDirection;
	}

	public void setPreDirection(String preDirection) {
		this.preDirection = preDirection;
	}

	@Column(length = 255)
	public String getStreetName() {
		return streetName;
	}

	public void setStreetName(String streetName) {
		this.streetName = streetName;
	}

	@Column(length = 10)
	public String getStreetType() {
		return streetType;
	}

	public void setStreetType(String streetType) {
		this.streetType = streetType;
	}

	@Column(length = 255)	
	public String getPostDirection() {
		return postDirection;
	}

	public void setPostDirection(String postDirection) {
		this.postDirection = postDirection;
	}

	@Column(length = 20)
	public String getUnitType() {
		return unitType;
	}

	public void setUnitType(String unitType) {
		this.unitType = unitType;
	}

	@Column(length = 15)
	public String getUnitNumber() {
		return unitNumber;
	}

	public void setUnitNumber(String unitNumber) {
		this.unitNumber = unitNumber;
	}

	@Column(name = "ResidentialZipPlus", length = 20)
	public String getZipPlus() {
		return zipPlus;
	}

	public void setZipPlus(String zipPlus) {
		this.zipPlus = zipPlus;
	}

	@Column(length = 255)
	public String getMailingAddress1() {
		return mailingAddress1;
	}

	public void setMailingAddress1(String mailingAddress1) {
		this.mailingAddress1 = mailingAddress1;
	}

	@Column(length = 255)
	public String getMailingAddress2() {
		return mailingAddress2;
	}

	public void setMailingAddress2(String mailingAddress2) {
		this.mailingAddress2 = mailingAddress2;
	}

	@Column(length = 255)
	public String getMailingAddress3() {
		return mailingAddress3;
	}

	public void setMailingAddress3(String mailingAddress3) {
		this.mailingAddress3 = mailingAddress3;
	}

	@Column(length = 255)
	public String getMailingCity() {
		return mailingCity;
	}

	public void setMailingCity(String mailingCity) {
		this.mailingCity = mailingCity;
	}

	@Column(length = 2)
	public String getMailingState() {
		return mailingState;
	}

	public void setMailingState(String mailingState) {
		this.mailingState = mailingState;
	}

	@Column(length = 15)
	public String getMailingZip() {
		return mailingZip;
	}

	public void setMailingZip(String mailingZip) {
		this.mailingZip = mailingZip;
	}

	@Column(length = 20)
	public String getMailingZipPlus() {
		return mailingZipPlus;
	}

	public void setMailingZipPlus(String mailingZipPlus) {
		this.mailingZipPlus = mailingZipPlus;
	}

	@Column(length = 100)
	public String getMailingCountry() {
		return mailingCountry;
	}

	public void setMailingCountry(String mailingCountry) {
		this.mailingCountry = mailingCountry;
	}

	@Column(length = 255)
	public String getBallotAddress1() {
		return ballotAddress1;
	}

	public void setBallotAddress1(String ballotAddress1) {
		this.ballotAddress1 = ballotAddress1;
	}

	@Column(length = 255)
	public String getBallotAddress2() {
		return ballotAddress2;
	}

	public void setBallotAddress2(String ballotAddress2) {
		this.ballotAddress2 = ballotAddress2;
	}

	@Column(length = 255)
	public String getBallotAddress3() {
		return ballotAddress3;
	}

	public void setBallotAddress3(String ballotAddress3) {
		this.ballotAddress3 = ballotAddress3;
	}

	@Column(length = 255)
	public String getBallotCity() {
		return ballotCity;
	}

	public void setBallotCity(String ballotCity) {
		this.ballotCity = ballotCity;
	}

	@Column(length = 2)
	public String getBallotState() {
		return ballotState;
	}

	public void setBallotState(String ballotState) {
		this.ballotState = ballotState;
	}

	@Column(length = 15)
	public String getBallotZip() {
		return ballotZip;
	}

	public void setBallotZip(String ballotZip) {
		this.ballotZip = ballotZip;
	}

	@Column(length = 20)
	public String getBallotZipPlus() {
		return ballotZipPlus;
	}

	public void setBallotZipPlus(String ballotZipPlus) {
		this.ballotZipPlus = ballotZipPlus;
	}

	@Column(length = 100)
	public String getBallotCountry() {
		return ballotCountry;
	}

	public void setBallotCountry(String ballotCountry) {
		this.ballotCountry = ballotCountry;
	}

	@Column(name = "VoterStatusActive")
	public Boolean getStatusActive() {
		return statusActive;
	}

	public void setStatusActive(Boolean statusActive) {
		this.statusActive = statusActive;
	}

	@Column(name = "VoterStatusReason", length = 255)
	public String getStatusReason() {
		return statusReason;
	}

	public void setStatusReason(String statusReason) {
		this.statusReason = statusReason;
	}

	public LocalDate getAffiliatedDate() {
		return affiliatedDate;
	}

	public void setAffiliatedDate(LocalDate affiliatedDate) {
		this.affiliatedDate = affiliatedDate;
	}

	public Boolean getIdRequired() {
		return idRequired;
	}

	public void setIdRequired(Boolean idRequired) {
		this.idRequired = idRequired;
	}

	public Integer getBirthYear() {
		return birthYear;
	}

	public void setBirthYear(Integer birthYear) {
		this.birthYear = birthYear;
	}

	public Boolean getUocava() {
		return uocava;
	}

	public void setUocava(Boolean uocava) {
		this.uocava = uocava;
	}

	@Column(length = 255)
	public String getIssueMethod() {
		return issueMethod;
	}

	public void setIssueMethod(String issueMethod) {
		this.issueMethod = issueMethod;
	}

	@Column(length = 255)
	public String getFax() {
		return fax;
	}

	public void setFax(String fax) {
		this.fax = fax;
	}

	@Column(name = "ResidentialAddress", length = 255)
	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
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
