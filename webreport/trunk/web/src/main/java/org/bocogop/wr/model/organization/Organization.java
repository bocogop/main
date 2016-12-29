package org.bocogop.wr.model.organization;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.PUBLIC_ONLY;

import java.time.Month;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Transient;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.annotations.BatchSize;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;

import org.bocogop.shared.util.PersistenceUtil;
import org.bocogop.wr.model.donation.Donor;
import org.bocogop.wr.model.donation.DonationSummary.DonationSummaryView;
import org.bocogop.wr.model.facility.Kiosk.KioskAssignmentsAndOrgsView;
import org.bocogop.wr.model.organization.OrganizationBranch.OrganizationBranchView;
import org.bocogop.wr.model.time.OccasionalWorkEntry.OccasionalWorkEntryView;
import org.bocogop.wr.model.time.WorkEntry.WorkEntryView;
import org.bocogop.wr.persistence.conversion.OrganizationScopeTypeConverter;

@Entity
@DiscriminatorValue("O")
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = PUBLIC_ONLY, isGetterVisibility = PUBLIC_ONLY)
public class Organization extends AbstractBasicOrganization {
	private static final long serialVersionUID = 6904844123870655771L;

	// -------------------------------------- Fields

	@NotNull
	private ScopeType scope;
	@NotBlank(message = "Full Name is required.")
	@Length(max = 255)
	protected String fullName;
	private boolean onNationalAdvisoryCommittee;
	private boolean nacExecutiveMember;
	private NACStatus nacMembershipStatus;
	private OrganizationType type;

	@Min(1)
	@Max(12)
	private Integer annualJointReviewMonth;

	private List<OrganizationBranch> branches;

	/* Mapping as list to ensure lazy loading - CPB */
	private List<Donor> donor;

	// -------------------------------------- Business Methods

	@Override
	@Transient
	@JsonIgnore
	public Organization getRootOrganization() {
		return this;
	}

	@Transient
	@JsonIgnore
	public String getAJRMonthName() {
		if (this.annualJointReviewMonth != null) {
			return Month.of(annualJointReviewMonth.intValue()).getDisplayName(TextStyle.FULL, Locale.getDefault());
		} else {
			return "";
		}
	}

	@Override
	@Transient
	@JsonView(OrganizationView.Search.class)
	public String getScale() {
		return "Organization";
	}

	@Transient
	@JsonView({ //
			DonationSummaryView.Search.class, //
			KioskAssignmentsAndOrgsView.Combined.class, //
			OccasionalWorkEntryView.TimeReport.class, //
			OrganizationView.Basic.class, //
			WorkEntryView.TimeReportByDate.class, //
			WorkEntryView.TimeReportByVolunteer.class, //
	})
	public String getDisplayName() {
		return getName();
	}

	@Transient
	@JsonIgnore
	public Donor getDonor() {
		return getDonorList().isEmpty() ? null : getDonorList().get(0);
	}

	// -------------------------------------- Common Methods

	@Override
	protected boolean requiredEquals(AbstractBasicOrganization oo) {
		Organization o = (Organization) PersistenceUtil.initializeAndUnproxy(oo);
		return new EqualsBuilder().append(getName().toUpperCase(), o.getName().toUpperCase())
				.append(nullSafeGetId(getFacility()), nullSafeGetId(oo.getFacility())).isEquals();
	}

	@Override
	protected int requiredHashCode() {
		return new HashCodeBuilder().append(getName().toUpperCase()).append(nullSafeGetId(getFacility())).toHashCode();
	}

	public String toString() {
		return getName() + " (station ID " + nullSafeGetId(getFacility()) + ")";
	}

	// -------------------------------------- Accessor Methods

	@Override
	@Column(nullable = false)
	@Convert(converter = OrganizationScopeTypeConverter.class)
	@JsonView(OrganizationView.Extended.class)
	public ScopeType getScope() {
		return scope;
	}

	public void setScope(ScopeType scopeType) {
		this.scope = scopeType;
	}

	@Override
	@Column(name = "FullName", length = 250, nullable = false)
	@JsonView(OrganizationView.Extended.class)
	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "WR_STD_VolunteerOrganizationTypesFK")
	@JsonView(OrganizationView.Extended.class)
	public OrganizationType getType() {
		return type;
	}

	public void setType(OrganizationType type) {
		this.type = type;
	}

	@Column(name = "IsOnNationalAdvisoryCommittee", nullable = false)
	@JsonView(OrganizationView.Extended.class)
	public boolean isOnNationalAdvisoryCommittee() {
		return onNationalAdvisoryCommittee;
	}

	public void setOnNationalAdvisoryCommittee(boolean onNationalAdvisoryCommittee) {
		this.onNationalAdvisoryCommittee = onNationalAdvisoryCommittee;
	}

	@Column(name = "IsNACExecutiveMember", nullable = false)
	@JsonView(OrganizationView.Extended.class)
	public boolean isNacExecutiveMember() {
		return nacExecutiveMember;
	}

	public void setNacExecutiveMember(boolean nacExecutiveMember) {
		this.nacExecutiveMember = nacExecutiveMember;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "NACMembershipStatusFK")
	@JsonView(OrganizationView.Extended.class)
	public NACStatus getNacMembershipStatus() {
		return nacMembershipStatus;
	}

	public void setNacMembershipStatus(NACStatus nacMembershipStatus) {
		this.nacMembershipStatus = nacMembershipStatus;
	}

	@JsonView(OrganizationView.Extended.class)
	public Integer getAnnualJointReviewMonth() {
		return annualJointReviewMonth;
	}

	public void setAnnualJointReviewMonth(Integer annualJointReviewMonth) {
		this.annualJointReviewMonth = annualJointReviewMonth;
	}

	@OneToMany(mappedBy = "organization", fetch = FetchType.LAZY)
	@BatchSize(size = 500)
	@JsonView(OrganizationBranchView.Basic.class)
	public List<OrganizationBranch> getBranches() {
		if (branches == null)
			branches = new ArrayList<>();
		return branches;
	}

	public void setBranches(List<OrganizationBranch> branches) {
		this.branches = branches;
	}

	@OneToMany(mappedBy = "organization", fetch = FetchType.LAZY)
	@BatchSize(size = 500)
	@JsonIgnore
	private List<Donor> getDonorList() {
		if (donor == null)
			donor = new ArrayList<>();
		return donor;
	}

	public void setDonorList(List<Donor> donor) {
		this.donor = donor;
	}

}
