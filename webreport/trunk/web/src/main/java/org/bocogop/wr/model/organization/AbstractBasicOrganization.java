package org.bocogop.wr.model.organization;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.PUBLIC_ONLY;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.Pattern;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.hibernate.annotations.BatchSize;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;

import org.bocogop.shared.model.core.AbstractAuditedVersionedPersistent;
import org.bocogop.shared.model.lookup.sds.State;
import org.bocogop.shared.util.StringUtil;
import org.bocogop.wr.model.donation.DonationSummary.DonationSummaryView;
import org.bocogop.wr.model.donation.Donor.DonorView;
import org.bocogop.wr.model.facility.Facility;
import org.bocogop.wr.model.facility.Kiosk.KioskAssignmentsAndOrgsView;
import org.bocogop.wr.model.time.OccasionalWorkEntry.OccasionalWorkEntryView;
import org.bocogop.wr.model.time.WorkEntry;
import org.bocogop.wr.model.time.WorkEntry.WorkEntryView;
import org.bocogop.wr.model.validation.constraints.ExtendedEmailValidator;
import org.bocogop.wr.model.volunteer.VolunteerOrganization;
import org.bocogop.wr.util.ValidationUtil;

@Entity
@Inheritance
@Table(name = "Organizations", schema = "wr")
@DiscriminatorColumn(name = "Type")
// @DiscriminatorOptions(force = true)
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = PUBLIC_ONLY, isGetterVisibility = PUBLIC_ONLY)
public abstract class AbstractBasicOrganization extends AbstractAuditedVersionedPersistent<AbstractBasicOrganization>
		implements BasicOrganization {
	private static final long serialVersionUID = 3810180355498406255L;

	/**
	 * See @JsonView documentation - CPB
	 */
	public static class OrganizationView {
		public interface Basic {
		}

		public interface Search extends Basic {
		}

		public interface Extended extends Search {
		}
	}

	// ---------------------------------------- Fields

	private Facility facility;

	@NotBlank(message = "Display Name is required.")
	@Length(max = 50)
	protected String name;
	@Length(max = 6)
	protected String abbreviation;
	protected boolean inactive;

	@Length(max = 35)
	protected String addressLine1;
	@Length(max = 35)
	protected String addressLine2;
	@Length(max = 30)
	protected String city;
	protected State state;
	@Length(max = 10)
	protected String zip;
	@Length(max = 250)
	@ExtendedEmailValidator(message = "Please enter a valid email in the format 'user@domain.tld'.")
	protected String email;
	@Length(max = 30)
	@Pattern(regexp = ValidationUtil.PHONE_REGEX, message = "Please enter a valid phone number.")
	protected String phone;
	@Length(max = 255)
	protected String contactName;
	@Length(max = 255)
	protected String contactTitle;

	protected List<VolunteerOrganization> volunteerOrganizations;
	protected List<WorkEntry> workEntries;

	// ---------------------------------------- Business Methods

	@Override
	@Transient
	@JsonIgnore
	public abstract Organization getRootOrganization();

	@Transient
	@JsonIgnore
	public Collection<String> getScopedToStationNumbers() {
		Facility institution = getFacility();
		if (institution == null)
			return new ArrayList<>();
		return Arrays.asList(institution.getStationNumber());
	}

	@Override
	@Transient
	@JsonView({ OccasionalWorkEntryView.TimeReport.class, //
			OrganizationView.Extended.class, //
			WorkEntryView.TimeReportByVolunteer.class, //
			WorkEntryView.TimeReportByDate.class })
	public boolean isActive() {
		return !inactive;
	}

	@Override
	@Transient
	@JsonView({ //
			DonationSummaryView.Search.class, //
			KioskAssignmentsAndOrgsView.Combined.class, //
			OccasionalWorkEntryView.TimeReport.class, //
			OrganizationView.Basic.class, //
			WorkEntryView.TimeReportByVolunteer.class, //
			WorkEntryView.TimeReportByDate.class, //
	})
	public abstract String getDisplayName();

	@Override
	@Transient
	public abstract String getScale();

	@Override
	@Transient
	@JsonView(OrganizationView.Basic.class)
	public String getAddressMultilineDisplay() {
		return getAddressDisplay();
	}

	@Transient
	private String getAddressDisplay() {
		return StringUtil.getAddressDisplay(addressLine1, addressLine2, null, city,
				getState() != null ? getState().getPostalName() : "", zip, "\n");
	}

	// ---------------------------------------- Common Methods

	@Override
	public int compareTo(BasicOrganization o) {
		if (equals(o))
			return 0;

		return new CompareToBuilder()
				.append(getRootOrganization().getDisplayName(), o.getRootOrganization().getDisplayName())
				.append(getDisplayName(), o.getDisplayName()).toComparison() > 0 ? 1 : -1;
	}

	// ---------------------------------------- Accessor Methods

	@Override
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "StationFK")
	@JsonView(OrganizationView.Search.class)
	public Facility getFacility() {
		return facility;
	}

	public void setFacility(Facility facility) {
		this.facility = facility;
	}

	@Override
	@Column(length = 6)
	@JsonView(OrganizationView.Search.class)
	public String getAbbreviation() {
		return abbreviation;
	}

	public void setAbbreviation(String abbreviation) {
		this.abbreviation = abbreviation;
	}

	@Override
	@Column(name = "OrganizationName", length = 35, nullable = false)
	@JsonView(OrganizationView.Search.class)
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	@Column(name = "IsInactive", nullable = false)
	@JsonView({ OrganizationView.Extended.class, DonorView.Search.class })
	public boolean isInactive() {
		return inactive;
	}

	public void setInactive(boolean inactive) {
		this.inactive = inactive;
	}

	@Override
	@OneToMany(mappedBy = "organization", fetch = FetchType.LAZY)
	@BatchSize(size = 500)
	@JsonIgnore
	public List<VolunteerOrganization> getVolunteerOrganizations() {
		if (volunteerOrganizations == null)
			volunteerOrganizations = new ArrayList<>();
		return volunteerOrganizations;
	}

	public void setVolunteerOrganizations(List<VolunteerOrganization> volunteerOrganizations) {
		this.volunteerOrganizations = volunteerOrganizations;
	}

	@Override
	@Column(name = "Address1", length = 35)
	@JsonView(OrganizationView.Extended.class)
	public String getAddressLine1() {
		return addressLine1;
	}

	public void setAddressLine1(String address1) {
		this.addressLine1 = address1;
	}

	@Override
	@Column(name = "Address2", length = 35)
	@JsonView(OrganizationView.Extended.class)
	public String getAddressLine2() {
		return addressLine2;
	}

	public void setAddressLine2(String address2) {
		this.addressLine2 = address2;
	}

	@Override
	@Column(length = 30)
	@JsonView(OrganizationView.Extended.class)
	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	@Override
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "STD_StateFK", unique = true)
	@JsonView(OrganizationView.Extended.class)
	public State getState() {
		return state;
	}

	public void setState(State state) {
		this.state = state;
	}

	@Override
	@Column(length = 10)
	@JsonView(OrganizationView.Extended.class)
	public String getZip() {
		return zip;
	}

	public void setZip(String zip) {
		this.zip = zip;
	}

	@Override
	@Column(length = 250)
	@JsonView(OrganizationView.Extended.class)
	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	@Override
	@Column(length = 30)
	@JsonView(OrganizationView.Extended.class)
	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	@Override
	@OneToMany(mappedBy = "organization", fetch = FetchType.LAZY)
	@BatchSize(size = 500)
	@JsonIgnore
	public List<WorkEntry> getWorkEntries() {
		if (workEntries == null)
			workEntries = new ArrayList<>();
		return workEntries;
	}

	public void setWorkEntries(List<WorkEntry> workEntries) {
		this.workEntries = workEntries;
	}

	@Override
	@Column(name = "ContactName", length = 255)
	@JsonView(OrganizationView.Extended.class)
	public String getContactName() {
		return contactName;
	}

	public void setContactName(String contactName) {
		this.contactName = contactName;
	}

	@Override
	@Column(name = "ContactTitle", length = 255)
	@JsonView(OrganizationView.Extended.class)
	public String getContactTitle() {
		return contactTitle;
	}

	public void setContactTitle(String contactTitle) {
		this.contactTitle = contactTitle;
	}

}
