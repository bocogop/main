package org.bocogop.wr.model.voter;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.PUBLIC_ONLY;

import java.time.LocalDate;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang.WordUtils;
import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.bocogop.shared.model.core.AbstractAuditedVersionedPersistent;
import org.bocogop.shared.util.StringUtil;
import org.bocogop.wr.model.voter.Voter.VoterView;
import org.bocogop.wr.util.DateUtil;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.format.annotation.DateTimeFormat;

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

	private String identifyingCode;

	@NotBlank
	private String lastName;
	@NotBlank
	private String firstName;
	private String middleName;
	private String suffix;
	private String nickname;

	@NotNull(message = "Please enter a date of birth.")
	@DateTimeFormat(pattern = DateUtil.TWO_DIGIT_DATE_ONLY)
	private LocalDate dateOfBirth;
	/* Computed */
	private Integer age;
	/* Computed */
	private boolean youth;

	@NotBlank(message = "Please enter a street address.")
	private String addressLine1;
	private String addressLine2;
	@NotBlank(message = "Please enter a city.")
	private String city;
	@NotBlank(message = "Please enter a zipcode.")
	private String zip;
	private String phone;
	private String phoneAlt;
	private String phoneAlt2;
	private String email;
	private String emergencyContactName;
	private String emergencyContactRelationship;
	private String emergencyContactPhone;
	private String emergencyContactPhoneAlt;

	// -------------------------------------- Constructors

	protected AbstractSimpleVoter() {
	}

	/**
	 * Convenience constructor for when we just want to create a dummy object
	 * for the UI (e.g. Notifications)
	 */
	protected AbstractSimpleVoter(long id, String lastName, String firstName, String middleName, String nameSuffix) {
		setId(id);
		this.lastName = lastName;
		this.firstName = firstName;
		this.middleName = middleName;
		this.suffix = nameSuffix;
	}

	public AbstractSimpleVoter(long id, String identifyingCode, String lastName, String firstName, String middleName,
			String nameSuffix, LocalDate dateOfBirth, int age, boolean youth, String nickname, String addressLine1,
			String addressLine2, String city, String zip, String phone, String phoneAlt, String phoneAlt2, String email,
			String emergencyContactName, String emergencyContactRelationship, String emergencyContactPhone,
			String emergencyContactPhoneAlt) {
		this(id, lastName, firstName, middleName, nameSuffix);
		this.identifyingCode = identifyingCode;
		this.dateOfBirth = dateOfBirth;
		this.age = age;
		this.youth = youth;
		this.nickname = nickname;
		this.addressLine1 = addressLine1;
		this.addressLine2 = addressLine2;
		this.city = city;
		this.zip = zip;
		this.phone = phone;
		this.phoneAlt = phoneAlt;
		this.phoneAlt2 = phoneAlt2;
		this.email = email;
		this.emergencyContactName = emergencyContactName;
		this.emergencyContactRelationship = emergencyContactRelationship;
		this.emergencyContactPhone = emergencyContactPhone;
		this.emergencyContactPhoneAlt = emergencyContactPhoneAlt;
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
		return getAddressDisplay(false);
	}

	private String getAddressDisplay(boolean useIdForState) {
		return StringUtil.getAddressDisplay(getAddressLine1(), getAddressLine2(), null, getCity(),
				useIdForState ? String.valueOf(getStateId()) : getStateString(), getZip(), "\n");
	}

	@Transient
	protected abstract String getStateString();

	@Transient
	protected abstract Long getStateId();

	// -------------------------------------- Common Methods

	@Override
	protected boolean requiredEquals(Voter oo) {
		return new EqualsBuilder().append(getDisplayName(), oo.getDisplayName())
				.append(getDateOfBirth(), oo.getDateOfBirth()).append(getIdentifyingCode(), oo.getIdentifyingCode())
				.isEquals();
	}

	@Override
	protected int requiredHashCode() {
		return new HashCodeBuilder().append(getDisplayName()).append(getDateOfBirth()).append(getIdentifyingCode())
				.toHashCode();
	}

	@Override
	public int compareTo(AbstractSimpleVoter<T> o) {
		if (equals(o))
			return 0;

		return new CompareToBuilder().append(nullSafeLowercase(getDisplayName()), nullSafeLowercase(o.getDisplayName()))
				.toComparison() > 0 ? 1 : -1;
	}

	public String toString() {
		return getDisplayName() + " (DOB " + getDateOfBirth() + ", code '" + identifyingCode + "')";
	}

	// -------------------------------------- Accessor Methods

	@Column(length = 10)
	@JsonView({ VoterView.Search.class, VoterView.Demographics.class })
	public String getIdentifyingCode() {
		return identifyingCode;
	}

	public void setIdentifyingCode(String identifyingCode) {
		this.identifyingCode = identifyingCode;
	}

	@Column(insertable = false, updatable = false)
	@JsonView({ VoterView.Extended.class, VoterView.Demographics.class })
	public Integer getAge() {
		return age;
	}

	public void setAge(Integer age) {
		this.age = age;
	}

	@Column(name = "IsYouth", insertable = false, updatable = false, nullable = false)
	@JsonView({ VoterView.Extended.class, VoterView.Demographics.class })
	public boolean isYouth() {
		return youth;
	}

	public void setYouth(boolean youth) {
		this.youth = youth;
	}

	@Column(length = 30)
	@JsonView(VoterView.Extended.class)
	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	@Column(length = 30)
	@JsonView(VoterView.Extended.class)
	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	@Column(length = 20)
	@JsonView(VoterView.Extended.class)
	public String getMiddleName() {
		return middleName;
	}

	public void setMiddleName(String middleName) {
		this.middleName = middleName;
	}

	@Column(name = "NameSuffix", length = 10)
	@JsonView(VoterView.Extended.class)
	public String getSuffix() {
		return suffix;
	}

	public void setSuffix(String suffix) {
		this.suffix = suffix;
	}

	@JsonView(VoterView.Basic.class)
	public LocalDate getDateOfBirth() {
		return dateOfBirth;
	}

	public void setDateOfBirth(LocalDate dateOfBirth) {
		this.dateOfBirth = dateOfBirth;
	}

	@Column(name = "NickName", length = 30)
	@JsonView(VoterView.Extended.class)
	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	@Column(name = "StreetAddress1", length = 35, nullable = false)
	@JsonView({ VoterView.Extended.class, VoterView.Demographics.class })
	public String getAddressLine1() {
		return addressLine1;
	}

	public void setAddressLine1(String addressLine1) {
		this.addressLine1 = addressLine1;
	}

	@Column(name = "StreetAddress2", length = 35)
	@JsonView({ VoterView.Extended.class, VoterView.Demographics.class })
	public String getAddressLine2() {
		return addressLine2;
	}

	public void setAddressLine2(String addressLine2) {
		this.addressLine2 = addressLine2;
	}

	@Column(length = 30, nullable = false)
	@JsonView({ VoterView.Extended.class, VoterView.Demographics.class })
	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	@Column(name = "ZipCode", length = 10)
	@JsonView({ VoterView.Extended.class, VoterView.Demographics.class })
	public String getZip() {
		return zip;
	}

	public void setZip(String zip) {
		this.zip = zip;
	}

	@Column(name = "Telephone", length = 30)
	@JsonView({ VoterView.Search.class, VoterView.Demographics.class })
	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	@Column(name = "AlternateTelephone", length = 30)
	@JsonView({ VoterView.Extended.class, VoterView.Demographics.class })
	public String getPhoneAlt() {
		return phoneAlt;
	}

	public void setPhoneAlt(String phoneAlternate) {
		this.phoneAlt = phoneAlternate;
	}

	@Column(name = "AlternateTelephone2", length = 30)
	@JsonView({ VoterView.Extended.class, VoterView.Demographics.class })
	public String getPhoneAlt2() {
		return phoneAlt2;
	}

	public void setPhoneAlt2(String phoneAlternate2) {
		this.phoneAlt2 = phoneAlternate2;
	}

	@Column(name = "EMailAddress", length = 250)
	@JsonView({ VoterView.Search.class, VoterView.Demographics.class })
	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	@Column(length = 250, name = "EmergencyContactName")
	@JsonView({ VoterView.Extended.class, VoterView.Demographics.class })
	public String getEmergencyContactName() {
		return emergencyContactName;
	}

	public void setEmergencyContactName(String emergencyContactName) {
		this.emergencyContactName = emergencyContactName;
	}

	@Column(length = 250, name = "EmergencyContactRelationship")
	@JsonView({ VoterView.Extended.class, VoterView.Demographics.class })
	public String getEmergencyContactRelationship() {
		return emergencyContactRelationship;
	}

	public void setEmergencyContactRelationship(String emergencyContactRelationship) {
		this.emergencyContactRelationship = emergencyContactRelationship;
	}

	@Column(length = 30, name = "EmergencyContactTelephone")
	@JsonView({ VoterView.Extended.class, VoterView.Demographics.class })
	public String getEmergencyContactPhone() {
		return emergencyContactPhone;
	}

	public void setEmergencyContactPhone(String emergencyContactPhone) {
		this.emergencyContactPhone = emergencyContactPhone;
	}

	@Column(length = 30, name = "EmergencyContactAlternateTelephone")
	@JsonView({ VoterView.Extended.class, VoterView.Demographics.class })
	public String getEmergencyContactPhoneAlt() {
		return emergencyContactPhoneAlt;
	}

	public void setEmergencyContactPhoneAlt(String emergencyContactPhoneAlt) {
		this.emergencyContactPhoneAlt = emergencyContactPhoneAlt;
	}
}
