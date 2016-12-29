package org.bocogop.wr.model.donation;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.PUBLIC_ONLY;
import static org.bocogop.wr.model.donation.DonorType.DonorTypeValue.INDIVIDUAL;
import static org.bocogop.wr.model.donation.DonorType.DonorTypeValue.ORGANIZATION;
import static org.bocogop.wr.model.donation.DonorType.DonorTypeValue.ORG_AND_INDIVIDUAL;
import static org.bocogop.wr.model.donation.DonorType.DonorTypeValue.OTHER_AND_INDIVIDUAL;
import static org.bocogop.wr.model.donation.DonorType.DonorTypeValue.OTHER_GROUPS;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.annotations.BatchSize;
import org.hibernate.validator.constraints.Length;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;

import org.bocogop.shared.model.core.AbstractAuditedVersionedPersistent;
import org.bocogop.shared.model.lookup.sds.State;
import org.bocogop.shared.util.StringUtil;
import org.bocogop.wr.model.donation.DonationSummary.DonationSummaryView;
import org.bocogop.wr.model.donation.DonorType.DonorTypeValue;
import org.bocogop.wr.model.expenditure.Expenditure.ExpenditureView;
import org.bocogop.wr.model.organization.AbstractBasicOrganization;
import org.bocogop.wr.model.organization.BasicOrganization;
import org.bocogop.wr.model.validation.constraints.ExtendedEmailValidator;
import org.bocogop.wr.model.volunteer.Volunteer;
import org.bocogop.wr.util.ValidationUtil;
import org.bocogop.wr.web.conversion.DonorTypeConverter;

@Entity
@Table(name = "Donor", schema = "wr")
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = PUBLIC_ONLY, isGetterVisibility = PUBLIC_ONLY)
public class Donor extends AbstractAuditedVersionedPersistent<Donor> implements Comparable<Donor> {
	private static final long serialVersionUID = 583796042812902141L;

	/**
	 * See @JsonView documentation - CPB
	 */
	public static class DonorView {
		public interface Basic {
		}

		public interface Search extends Basic, DonationSummaryView.Search {
		}

		public interface Extended extends Basic {
		}
	}

	// -------------------------------------- Fields

	@Length(max = 30)
	private String lastName;
	@Length(max = 30)
	private String firstName;
	@Length(max = 20)
	private String middleName;
	private String prefix;
	@Length(max = 10)
	private String suffix;
	@Length(max = 50)
	private String otherGroup;
	@Length(max = 35)
	private String addressLine1;
	@Length(max = 35)
	private String addressLine2;
	@Length(max = 30)
	private String city;
	@Length(max = 10)
	private String zip;
	@Length(max = 250)
	@ExtendedEmailValidator(message = "Please enter a valid email in the format 'user@domain.tld'.")
	private String email;
	@Length(max = 30)
	@Pattern(regexp = ValidationUtil.PHONE_REGEX, message = "Please enter a valid phone number.")
	private String phone;

	private DonorType donorType;
	private AbstractBasicOrganization organization;
	private State state;
	private Volunteer volunteer;

	private List<DonationSummary> donations;

	// -------------------------------------- Business Methods

	@Transient
	@JsonView(DonorView.Search.class)
	public String getAddressMultilineDisplay() {
		DonorTypeValue t = getDonorType().getLookupType();
		if (ORGANIZATION == t) {
			BasicOrganization org = getOrganization();
			if (org != null) {
				return org.getAddressMultilineDisplay();
			} else {
				return getAddressDisplay(false);
			}
		}

		Volunteer v = getVolunteer();
		if (v != null) {
			return v.getAddressMultilineDisplay();
		} else {
			return getAddressDisplay(false);
		}
	}

	private String getAddressDisplay(boolean useIdForState) {
		return StringUtil.getAddressDisplay(addressLine1, addressLine2, null, city, useIdForState
				? String.valueOf(nullSafeGetId(getState())) : (getState() != null ? getState().getPostalName() : ""),
				zip, "\n");
	}

	@Transient
	@JsonView(DonorView.Extended.class)
	public String getIndividualSalutation() {
		DonorTypeValue t = getDonorType().getLookupType();
		if (t == ORGANIZATION)
			return StringUtils.defaultString(getOrganization().getContactName());

		if (t == INDIVIDUAL) {
			Volunteer v = getVolunteer();
			if (v != null)
				return v.getDisplayName(false);

			return StringUtil.getDisplayName(false, firstName, middleName, lastName, suffix);
		}

		if (t == ORG_AND_INDIVIDUAL || t == OTHER_AND_INDIVIDUAL) {
			return StringUtil.getDisplayName(false, firstName, middleName, lastName, suffix);
		}

		return "";
	}

	@Transient
	@JsonView(DonationSummaryView.Search.class)
	public String getIndividualName() {
		DonorTypeValue t = getDonorType().getLookupType();
		if (t == ORGANIZATION)
			return getOrganization().getDisplayName();

		if (t == INDIVIDUAL) {
			Volunteer v = getVolunteer();
			if (v != null)
				return v.getDisplayName();

			return StringUtil.getDisplayName(true, firstName, middleName, lastName, suffix);
		}

		if (t == ORG_AND_INDIVIDUAL || t == OTHER_AND_INDIVIDUAL) {
			return StringUtil.getDisplayName(true, firstName, middleName, lastName, suffix);
		}

		return "";
	}

	@Transient
	@JsonView(DonationSummaryView.Search.class)
	public String getOtherGroupName() {
		DonorTypeValue t = getDonorType().getLookupType();
		if (t == OTHER_GROUPS || t == OTHER_AND_INDIVIDUAL)
			return getOtherGroup();
		if (t == ORG_AND_INDIVIDUAL) {
			AbstractBasicOrganization o = getOrganization();
			if (o != null)
				return o.getDisplayName();
		}
		return "";
	}

	/*
	 * Attempts to merge everything down into one value; however, for
	 * ORG_AND_INDIVIDUAL and OTHER_AND_INDIVIDUAL there are really two values,
	 * which aren't consistently used in the legacy data. Some screens we break
	 * these out (donation summary) so we use the two methods above. Whereas
	 * other screens (donor popup) we only have one column so we use this
	 * method. CPB
	 */
	@Transient
	@JsonView({ DonorView.Basic.class, DonationSummaryView.Search.class, ExpenditureView.Search.class })
	public String getDisplayName() {
		DonorTypeValue t = getDonorType().getLookupType();
		if (t == ORGANIZATION)
			return getOrganization() != null ? getOrganization().getDisplayName() : "";
		if (t == OTHER_GROUPS || t == OTHER_AND_INDIVIDUAL)
			return getOtherGroup();
		if (t == ORG_AND_INDIVIDUAL) {
			return getOrganization() != null ? getOrganization().getDisplayName() : "";
		}
		Volunteer v = getVolunteer();
		if (v != null)
			return v.getDisplayName();

		return StringUtil.getDisplayName(true, firstName, middleName, lastName, suffix);
	}

	@Transient
	@JsonIgnore
	private boolean isDemographicsUsed() {
		return getDonorType().getLookupType() == INDIVIDUAL && getVolunteer() == null;
	}

	// -------------------------------------- Common Methods

	@Override
	public int compareTo(Donor o) {
		if (equals(o))
			return 0;

		return new CompareToBuilder().append(nullSafeLowercase(getDisplayName()), nullSafeLowercase(o.getDisplayName()))
				.toComparison() > 0 ? 1 : -1;
	}

	@Override
	protected boolean requiredEquals(Donor oo) {
		String displayName = isDemographicsUsed() ? nullSafeLowercase(getDisplayName()) : "";
		String otherDisplayName = oo.isDemographicsUsed() ? nullSafeLowercase(oo.getDisplayName()) : "";
		String addressStr = isDemographicsUsed() ? nullSafeLowercase(getAddressDisplay(true)) : "";
		String otherAddressStr = oo.isDemographicsUsed() ? nullSafeLowercase(oo.getAddressDisplay(true)) : "";

		return new EqualsBuilder().append(nullSafeGetId(getDonorType()), nullSafeGetId(oo.getDonorType()))
				.append(nullSafeGetId(getVolunteer()), nullSafeGetId(oo.getVolunteer()))
				.append(nullSafeGetId(getOrganization()), nullSafeGetId(oo.getOrganization()))
				.append(displayName, otherDisplayName).append(addressStr, otherAddressStr).isEquals();
	}

	@Override
	protected int requiredHashCode() {
		String displayName = isDemographicsUsed() ? nullSafeLowercase(getDisplayName()) : "";
		String addressStr = isDemographicsUsed() ? nullSafeLowercase(getAddressDisplay(true)) : "";

		return new HashCodeBuilder().append(nullSafeGetId(getDonorType())).append(nullSafeGetId(getVolunteer()))
				.append(nullSafeGetId(getOrganization())).append(displayName).append(addressStr).toHashCode();
	}

	public String toString() {
		return getDisplayName();
	}

	// -------------------------------------- Accessor Methods

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "WrVolunteersFK", unique = true)
	@JsonView(DonorView.Search.class)
	public Volunteer getVolunteer() {
		return volunteer;
	}

	public void setVolunteer(Volunteer volunteer) {
		this.volunteer = volunteer;
	}

	@Convert(converter = DonorTypeConverter.class)
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "DonorTypeFK", unique = true)
	@JsonView({ DonorView.Basic.class, DonationSummaryView.Search.class })
	@NotNull
	public DonorType getDonorType() {
		return donorType;
	}

	public void setDonorType(DonorType donorType) {
		this.donorType = donorType;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "OrganizationFK", unique = true)
	@JsonView(DonorView.Search.class)
	public AbstractBasicOrganization getOrganization() {
		return organization;
	}

	public void setOrganization(AbstractBasicOrganization organization) {
		this.organization = organization;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "STD_StateFK", unique = true)
	@JsonIgnore
	public State getState() {
		return state;
	}

	public void setState(State state) {
		this.state = state;
	}

	@Column(name = "NamePrefix", length = 10)
	@JsonView(DonorView.Extended.class)
	public String getPrefix() {
		return prefix;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	@Column(name = "NameSuffix", length = 10)
	@JsonView(DonorView.Extended.class)
	public String getSuffix() {
		return suffix;
	}

	public void setSuffix(String suffix) {
		this.suffix = suffix;
	}

	@Column(name = "OtherGroup", length = 50)
	@JsonView(DonorView.Extended.class)
	public String getOtherGroup() {
		return otherGroup;
	}

	public void setOtherGroup(String otherGroup) {
		this.otherGroup = otherGroup;
	}

	@Column(name = "Address1", length = 35)
	@JsonView(DonorView.Extended.class)
	public String getAddressLine1() {
		return addressLine1;
	}

	public void setAddressLine1(String address1) {
		this.addressLine1 = address1;
	}

	@Column(name = "Address2", length = 35)
	@JsonView(DonorView.Extended.class)
	public String getAddressLine2() {
		return addressLine2;
	}

	public void setAddressLine2(String address2) {
		this.addressLine2 = address2;
	}

	@Column(name = "City", length = 30)
	@JsonView(DonorView.Extended.class)
	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	@Column(name = "Zip", length = 10)
	@JsonView(DonorView.Extended.class)
	public String getZip() {
		return zip;
	}

	public void setZip(String zip) {
		this.zip = zip;
	}

	@Transient
	@JsonView(DonorView.Search.class)
	public String getDisplayEmail() {
		String emailStr = getEmail();

		if (getDonorType() != null) {
			DonorTypeValue t = getDonorType().getLookupType();
			if (t == ORGANIZATION) {
				emailStr = getOrganization() != null ? getOrganization().getEmail() : "";
			} else {
				Volunteer v = getVolunteer();
				if (v != null) {
					emailStr = v.getEmail();
				}
			}
		}

		return emailStr;
	}

	@Column(name = "Email", length = 250)
	@JsonView(DonorView.Basic.class)
	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	@Transient
	@JsonView(DonorView.Search.class)
	public String getDisplayPhone() {
		String phoneStr = getPhone();

		if (getDonorType() != null) {
			DonorTypeValue t = getDonorType().getLookupType();
			if (t == ORGANIZATION) {
				phoneStr = getOrganization() != null ? getOrganization().getPhone() : "";
			} else {
				Volunteer v = getVolunteer();
				if (v != null) {
					phoneStr = v.getPhone();
				}
			}
		}

		return phoneStr;

	}

	@Column(name = "Phone", length = 30)
	@JsonView(DonorView.Basic.class)
	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	@Column(name = "LastName", length = 30)
	@JsonView(DonorView.Extended.class)
	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	@Column(name = "firstName", length = 30)
	@JsonView(DonorView.Extended.class)
	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	@Column(name = "MiddleName", length = 20)
	@JsonView(DonorView.Extended.class)
	public String getMiddleName() {
		return middleName;
	}

	public void setMiddleName(String middleName) {
		this.middleName = middleName;
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "donor", cascade = CascadeType.ALL)
	@BatchSize(size = 100)
	@JsonIgnore
	public List<DonationSummary> getDonations() {
		if (donations == null)
			donations = new ArrayList<>();
		return donations;
	}

	public void setDonations(List<DonationSummary> donations) {
		this.donations = donations;
	}
}
