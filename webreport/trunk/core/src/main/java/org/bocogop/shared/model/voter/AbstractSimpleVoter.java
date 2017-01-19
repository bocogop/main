package org.bocogop.shared.model.voter;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.PUBLIC_ONLY;

import java.time.LocalDate;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

import org.apache.commons.lang.WordUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.bocogop.shared.model.AbstractAuditedVersionedPersistent;
import org.bocogop.shared.model.Participation.ParticipationView;
import org.bocogop.shared.model.voter.Voter.VoterView;
import org.bocogop.shared.util.StringUtil;
import org.hibernate.validator.constraints.NotBlank;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;

@MappedSuperclass
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = PUBLIC_ONLY, isGetterVisibility = PUBLIC_ONLY)
public abstract class AbstractSimpleVoter<T extends AbstractSimpleVoter<T>>
		extends AbstractAuditedVersionedPersistent<Voter> implements Comparable<AbstractSimpleVoter<T>> {
	private static final long serialVersionUID = 3222064615857480112L;

	public static String getDisplayName(String firstName, String middleName, String lastName, String suffix,
			String nickname, boolean lastFirst) {
		return WordUtils.capitalizeFully(StringUtil.getDisplayName(lastFirst, firstName, middleName, lastName, suffix)
				+ (StringUtils.isNotBlank(nickname) ? " (\"" + nickname + "\")" : ""));
	}

	public static String getDisplayName(String firstName, String middleName, String lastName, String suffix,
			String nickname) {
		return getDisplayName(firstName, middleName, lastName, suffix, nickname, true);
	}

	// -------------------------------------- Fields

	@NotBlank
	private String firstName;
	private String middleName;
	@NotBlank
	private String lastName;
	private String suffix;
	private String nickname;

	private String voterId;

	private LocalDate affiliatedDate;

	private LocalDate registrationDate;
	private LocalDate effectiveDate;
	private Boolean statusActive;
	private String statusReason;

	private String address;
	private String city;
	private String state;
	private String zip;
	private String zipPlus;

	private Integer birthYear;
	private Integer ageApprox;

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

	private String phone;
	private String userProvidedPhone;
	private String fax;
	private String email;
	private String userProvidedEmail;

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
		return getDisplayName(firstName, middleName, lastName, suffix, nickname, lastFirst);
	}

	@Transient
	@JsonView({ VoterView.Search.class, VoterView.Demographics.class, ParticipationView.VotersForEvent.class })
	public String getDisplayName() {
		return getDisplayName(true);
	}

	@Transient
	@JsonView({ VoterView.Search.class, VoterView.Demographics.class, ParticipationView.VotersForEvent.class })
	public String getAddressMultilineDisplay() {
		return StringUtil.getAddressDisplay(getAddress(), null, null, getCity(), getState(), getZip(), null, "\n");
	}

	@Transient
	@JsonView({ VoterView.Extended.class, VoterView.Demographics.class })
	public String getFullMailingAddressMultilineDisplay() {
		return StringUtil.getAddressDisplay(getMailingAddress1(), getMailingAddress2(), getMailingAddress3(),
				getMailingCity(), getMailingState(), getFullMailingZip(), getMailingCountry(), "\n");
	}

	@Transient
	@JsonView({ VoterView.Extended.class, VoterView.Demographics.class })
	public String getFullBallotAddressMultilineDisplay() {
		return StringUtil.getAddressDisplay(getBallotAddress1(), getBallotAddress2(), getBallotAddress3(),
				getBallotCity(), getBallotState(), getFullBallotZip(), getBallotCountry(), "\n");
	}

	@Transient
	@JsonView({ VoterView.Demographics.class })
	public String getFullZip() {
		return StringUtils.isNotBlank(getZipPlus()) ? getZip() + "-" + getZipPlus() : getZip();
	}

	@Transient
	@JsonView({ VoterView.Demographics.class })
	public String getFullMailingZip() {
		return StringUtils.isNotBlank(getMailingZipPlus()) ? getMailingZip() + "-" + getMailingZipPlus()
				: getMailingZip();
	}

	@Transient
	@JsonView({ VoterView.Demographics.class })
	public String getFullBallotZip() {
		return StringUtils.isNotBlank(getBallotZipPlus()) ? getBallotZip() + "-" + getBallotZipPlus() : getBallotZip();
	}

	@Transient
	@JsonView({ VoterView.Search.class, VoterView.Demographics.class })
	public String getFinalPhone() {
		return StringUtils.isNotBlank(getUserProvidedPhone()) ? getUserProvidedPhone() : getPhone();
	}

	@Transient
	@JsonView({ VoterView.Search.class, VoterView.Demographics.class })
	public String getFinalEmail() {
		return StringUtils.isNotBlank(getUserProvidedEmail()) ? getUserProvidedEmail() : getEmail();
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

	@JsonView({ VoterView.Extended.class, VoterView.Demographics.class })
	public LocalDate getRegistrationDate() {
		return registrationDate;
	}

	public void setRegistrationDate(LocalDate registrationDate) {
		this.registrationDate = registrationDate;
	}

	@JsonView({ VoterView.Extended.class, VoterView.Demographics.class })
	public LocalDate getEffectiveDate() {
		return effectiveDate;
	}

	public void setEffectiveDate(LocalDate effectiveDate) {
		this.effectiveDate = effectiveDate;
	}

	@Column(name = "ResidentialZipPlus", length = 20)
	@JsonIgnore
	public String getZipPlus() {
		return zipPlus;
	}

	public void setZipPlus(String zipPlus) {
		this.zipPlus = zipPlus;
	}

	@Column(length = 255)
	@JsonIgnore
	public String getMailingAddress1() {
		return mailingAddress1;
	}

	public void setMailingAddress1(String mailingAddress1) {
		this.mailingAddress1 = mailingAddress1;
	}

	@Column(length = 255)
	@JsonIgnore
	public String getMailingAddress2() {
		return mailingAddress2;
	}

	public void setMailingAddress2(String mailingAddress2) {
		this.mailingAddress2 = mailingAddress2;
	}

	@Column(length = 255)
	@JsonIgnore
	public String getMailingAddress3() {
		return mailingAddress3;
	}

	public void setMailingAddress3(String mailingAddress3) {
		this.mailingAddress3 = mailingAddress3;
	}

	@Column(length = 255)
	@JsonIgnore
	public String getMailingCity() {
		return mailingCity;
	}

	public void setMailingCity(String mailingCity) {
		this.mailingCity = mailingCity;
	}

	@Column(length = 2)
	@JsonIgnore
	public String getMailingState() {
		return mailingState;
	}

	public void setMailingState(String mailingState) {
		this.mailingState = mailingState;
	}

	@Column(length = 15)
	@JsonIgnore
	public String getMailingZip() {
		return mailingZip;
	}

	public void setMailingZip(String mailingZip) {
		this.mailingZip = mailingZip;
	}

	@Column(length = 20)
	@JsonIgnore
	public String getMailingZipPlus() {
		return mailingZipPlus;
	}

	public void setMailingZipPlus(String mailingZipPlus) {
		this.mailingZipPlus = mailingZipPlus;
	}

	@Column(length = 100)
	@JsonIgnore
	public String getMailingCountry() {
		return mailingCountry;
	}

	public void setMailingCountry(String mailingCountry) {
		this.mailingCountry = mailingCountry;
	}

	@Column(length = 255)
	@JsonIgnore
	public String getBallotAddress1() {
		return ballotAddress1;
	}

	public void setBallotAddress1(String ballotAddress1) {
		this.ballotAddress1 = ballotAddress1;
	}

	@Column(length = 255)
	@JsonIgnore
	public String getBallotAddress2() {
		return ballotAddress2;
	}

	public void setBallotAddress2(String ballotAddress2) {
		this.ballotAddress2 = ballotAddress2;
	}

	@Column(length = 255)
	@JsonIgnore
	public String getBallotAddress3() {
		return ballotAddress3;
	}

	public void setBallotAddress3(String ballotAddress3) {
		this.ballotAddress3 = ballotAddress3;
	}

	@Column(length = 255)
	@JsonIgnore
	public String getBallotCity() {
		return ballotCity;
	}

	public void setBallotCity(String ballotCity) {
		this.ballotCity = ballotCity;
	}

	@Column(length = 2)
	@JsonIgnore
	public String getBallotState() {
		return ballotState;
	}

	public void setBallotState(String ballotState) {
		this.ballotState = ballotState;
	}

	@Column(length = 15)
	@JsonIgnore
	public String getBallotZip() {
		return ballotZip;
	}

	public void setBallotZip(String ballotZip) {
		this.ballotZip = ballotZip;
	}

	@Column(length = 20)
	@JsonIgnore
	public String getBallotZipPlus() {
		return ballotZipPlus;
	}

	public void setBallotZipPlus(String ballotZipPlus) {
		this.ballotZipPlus = ballotZipPlus;
	}

	@Column(length = 100)
	@JsonIgnore
	public String getBallotCountry() {
		return ballotCountry;
	}

	public void setBallotCountry(String ballotCountry) {
		this.ballotCountry = ballotCountry;
	}

	@Column(name = "VoterStatusActive")
	@JsonView({ VoterView.Search.class, VoterView.Extended.class, VoterView.Demographics.class,
			ParticipationView.VotersForEvent.class })
	public Boolean getStatusActive() {
		return statusActive;
	}

	public void setStatusActive(Boolean statusActive) {
		this.statusActive = statusActive;
	}

	@Column(name = "VoterStatusReason", length = 255)
	@JsonView({ VoterView.Extended.class, VoterView.Demographics.class })
	public String getStatusReason() {
		return statusReason;
	}

	public void setStatusReason(String statusReason) {
		this.statusReason = statusReason;
	}

	@JsonView({ VoterView.Extended.class, VoterView.Demographics.class })
	public LocalDate getAffiliatedDate() {
		return affiliatedDate;
	}

	public void setAffiliatedDate(LocalDate affiliatedDate) {
		this.affiliatedDate = affiliatedDate;
	}

	@JsonView({ VoterView.Search.class, VoterView.Extended.class, VoterView.Demographics.class })
	public Integer getBirthYear() {
		return birthYear;
	}

	public void setBirthYear(Integer birthYear) {
		this.birthYear = birthYear;
	}

	@Column(length = 255)
	@JsonView({ VoterView.Extended.class, VoterView.Demographics.class })
	public String getFax() {
		return fax;
	}

	public void setFax(String fax) {
		this.fax = fax;
	}

	@Column(name = "ResidentialAddress", length = 255)
	@JsonView({ VoterView.Extended.class, VoterView.Demographics.class })
	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	@Column(length = 100)
	@JsonView(VoterView.Extended.class)
	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	@Column(name = "PhoneUserProvided", length = 30)
	@JsonIgnore
	public String getUserProvidedPhone() {
		return userProvidedPhone;
	}

	public void setUserProvidedPhone(String userProvidedPhone) {
		this.userProvidedPhone = userProvidedPhone;
	}

	@Column(name = "EmailUserProvided", length = 255)
	@JsonIgnore
	public String getUserProvidedEmail() {
		return userProvidedEmail;
	}

	public void setUserProvidedEmail(String userProvidedEmail) {
		this.userProvidedEmail = userProvidedEmail;
	}

	@Column(insertable = false, updatable = false)
	public Integer getAgeApprox() {
		return ageApprox;
	}

	public void setAgeApprox(Integer ageApprox) {
		this.ageApprox = ageApprox;
	}

}
