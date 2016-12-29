package org.bocogop.wr.model.organization;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.PUBLIC_ONLY;

import java.time.LocalDate;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;

import org.bocogop.shared.model.core.AbstractAuditedVersionedPersistent;
import org.bocogop.shared.model.lookup.sds.State;
import org.bocogop.wr.model.validation.constraints.ExtendedEmailValidator;
import org.bocogop.wr.util.DateUtil;
import org.bocogop.wr.util.ValidationUtil;
import org.bocogop.wr.web.conversion.StdVAVSTitleConverter;

@Entity
@Table(name = "OrganizationNationalOfficials", schema = "wr")
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = PUBLIC_ONLY, isGetterVisibility = PUBLIC_ONLY)
public class NationalOfficial extends AbstractAuditedVersionedPersistent<NationalOfficial>
		implements Comparable<NationalOfficial> {
	private static final long serialVersionUID = 5052914173550763012L;

	// -------------------------------------- Fields

	private AbstractBasicOrganization organization;

	@NotBlank(message = "Last Name is required.")
	@Length(max = 30)
	private String lastName;

	@Length(max = 30)
	private String firstName;

	@Length(max = 20)
	private String middleName;

	@Length(max = 10)
	private String suffix;

	@Length(max = 10)
	private String prefix;

	@Length(max = 30)
	private String title;

	private boolean certifyingOfficial;

	@Length(max = 250)
	@ExtendedEmailValidator(message = "Please enter a valid email in the format 'user@domain.tld'.")
	private String email;

	@NotBlank(message = "Street Address is required.")
	@Length(max = 35)
	private String streetAddress;

	@Length(max = 30)
	@NotBlank(message = "City is required.")
	private String city;

	@NotNull(message = "State is required.")
	private State state;

	@Length(max = 10)
	@NotBlank(message = "Zip Code is required.")
	private String zip;

	@Pattern(regexp = ValidationUtil.PHONE_REGEX, message = "Please enter a valid phone number.")
	private String phone;

	@NotNull(message = "VAVS Title is required.")
	private StdVAVSTitle stdVAVSTitle;

	private LocalDate vavsStartDate;
	private LocalDate vavsEndDate;

	private boolean nationalCommitteeMember;
	private LocalDate nacStartDate;
	private LocalDate nacEndDate;

	// -------------------------------------- Constructors

	public NationalOfficial() {
	}

	public NationalOfficial(AbstractBasicOrganization organization) {
		this.organization = organization;
	}

	// -------------------------------------- Business Methods

	// -------------------------------------- Common Methods

	@Override
	protected boolean requiredEquals(NationalOfficial oo) {
		return new EqualsBuilder().append(nullSafeGetId(organization), nullSafeGetId(oo.getOrganization()))
				.append(getLastName(), oo.getLastName())
				.append(nullSafeGetId(getStdVAVSTitle()), nullSafeGetId(oo.getStdVAVSTitle())).isEquals();
	}

	@Override
	protected int requiredHashCode() {
		return new HashCodeBuilder().append(nullSafeGetId(organization)).append(lastName)
				.append(nullSafeGetId(getStdVAVSTitle())).toHashCode();
	}

	@Override
	public int compareTo(NationalOfficial o) {
		if (equals(o))
			return 0;

		return new CompareToBuilder().append(nullSafeLowercase(lastName), nullSafeLowercase(o.getLastName()))
				.append(nullSafeGetId(organization), nullSafeGetId(o.getOrganization())).toComparison() > 0 ? 1 : -1;
	}

	public String toString() {
		return "LastName: " + getLastName() + getStdVAVSTitle() != null ? "\nVAVSTitle: " + getStdVAVSTitle().getName()
				: "" + "\norganization: " + nullSafeGetId(organization) + ")";
	}

	// -------------------------------------- Accessor Methods

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "OrganizationFK", unique = true)
	@JsonIgnore
	public AbstractBasicOrganization getOrganization() {
		return organization;
	}

	public void setOrganization(AbstractBasicOrganization organization) {
		this.organization = organization;
	}

	@Column(name = "LastName", length = 30, nullable = false)
	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	@Column(name = "FirstName", length = 30)
	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	@Column(name = "MiddleName", length = 20)
	public String getMiddleName() {
		return middleName;
	}

	public void setMiddleName(String middleName) {
		this.middleName = middleName;
	}

	@Column(name = "NameSuffix", length = 10)
	public String getSuffix() {
		return suffix;
	}

	public void setSuffix(String suffix) {
		this.suffix = suffix;
	}

	@Column(name = "NamePrefix", length = 10)
	public String getPrefix() {
		return prefix;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	@Column(name = "Title", length = 30)
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	@Column(name = "IsNationalCertifyingOfficial", nullable = false)
	public boolean isCertifyingOfficial() {
		return certifyingOfficial;
	}

	public void setCertifyingOfficial(boolean certifyingOfficial) {
		this.certifyingOfficial = certifyingOfficial;
	}

	@Column(name = "EmailAddress", length = 255)
	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	@Column(name = "StreetAddress1", length = 35, nullable = false)
	public String getStreetAddress() {
		return streetAddress;
	}

	public void setStreetAddress(String streetAddress) {
		this.streetAddress = streetAddress;
	}

	@Column(name = "City", length = 30, nullable = false)
	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "StateFK", nullable = false)
	public State getState() {
		return state;
	}

	public void setState(State state) {
		this.state = state;
	}

	@Column(name = "Zip", length = 10)
	public String getZip() {
		return zip;
	}

	public void setZip(String zip) {
		this.zip = zip;
	}

	@Column(name = "Telephone", length = 30)
	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "WR_STD_VAVS_TITLEFK", nullable = false)
	@Convert(converter = StdVAVSTitleConverter.class)
	public StdVAVSTitle getStdVAVSTitle() {
		return stdVAVSTitle;
	}

	public void setStdVAVSTitle(StdVAVSTitle stdVAVSTitle) {
		this.stdVAVSTitle = stdVAVSTitle;
	}

	@Column(name = "DateAppointed")
	@DateTimeFormat(pattern = DateUtil.TWO_DIGIT_DATE_ONLY)
	public LocalDate getVavsStartDate() {
		return vavsStartDate;
	}

	public void setVavsStartDate(LocalDate vavsStartDate) {
		this.vavsStartDate = vavsStartDate;
	}

	@Column(name = "DateUnappointed")
	@DateTimeFormat(pattern = DateUtil.TWO_DIGIT_DATE_ONLY)
	public LocalDate getVavsEndDate() {
		return vavsEndDate;
	}

	public void setVavsEndDate(LocalDate vavsEndDate) {
		this.vavsEndDate = vavsEndDate;
	}

	@Column(name = "IsNACExecutiveCommitteeMember")
	public Boolean isNationalCommitteeMember() {
		return nationalCommitteeMember;
	}

	public void setNationalCommitteeMember(Boolean nationalCommitteeMember) {
		if (nationalCommitteeMember == null)
			nationalCommitteeMember = false;
		this.nationalCommitteeMember = nationalCommitteeMember;
	}

	@Column(name = "NACStartDate")
	@DateTimeFormat(pattern = DateUtil.TWO_DIGIT_DATE_ONLY)
	public LocalDate getNacStartDate() {
		return nacStartDate;
	}

	public void setNacStartDate(LocalDate nacStartDate) {
		this.nacStartDate = nacStartDate;
	}

	@Column(name = "NACEndDate")
	@DateTimeFormat(pattern = DateUtil.TWO_DIGIT_DATE_ONLY)
	public LocalDate getNacEndDate() {
		return nacEndDate;
	}

	public void setNacEndDate(LocalDate nacEndDate) {
		this.nacEndDate = nacEndDate;
	}

}
