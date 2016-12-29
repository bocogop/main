package org.bocogop.wr.model.benefitingService;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.PUBLIC_ONLY;

import java.util.ArrayList;
import java.util.List;

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

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.annotations.BatchSize;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;

import org.bocogop.shared.model.core.AbstractAuditedVersionedPersistent;
import org.bocogop.wr.model.benefitingService.BenefitingService.BenefitingServiceView;
import org.bocogop.wr.model.facility.AbstractLocation.BasicLocationView;
import org.bocogop.wr.model.facility.AbstractUpdateableLocation;
import org.bocogop.wr.model.requirement.BenefitingServiceRoleRequirementAssociation;
import org.bocogop.wr.model.time.OccasionalWorkEntry;
import org.bocogop.wr.model.time.OccasionalWorkEntry.OccasionalWorkEntryView;
import org.bocogop.wr.model.validation.constraints.ExtendedEmailValidator;
import org.bocogop.wr.model.volunteer.VolunteerAssignment;
import org.bocogop.wr.model.volunteer.VolunteerAssignment.VolunteerAssignmentView;
import org.bocogop.wr.persistence.conversion.BenefitingServiceScopeTypeConverter;
import org.bocogop.wr.util.FacilityUtil;
import org.bocogop.wr.util.ValidationUtil;

@Entity
@Table(name = "BenefitingServiceRoles", schema = "wr")
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = PUBLIC_ONLY, isGetterVisibility = PUBLIC_ONLY)
public class BenefitingServiceRole extends AbstractAuditedVersionedPersistent<BenefitingServiceRole>
		implements Comparable<BenefitingServiceRole> {
	private static final long serialVersionUID = 6904844123870655771L;

	/**
	 * See @JsonView documentation - CPB
	 */
	public static class BenefitingServiceRoleView {
		public interface Basic extends BasicLocationView.Basic {
		}

		public interface Extended extends Basic {
		}
	}

	public static String getDisplayName(BenefitingServiceRole role, BenefitingService optionalService) {
		if (optionalService != null) {
			return optionalService.getDisplayName() + " - " + role.getName();
		}
		return role.getName();
	}

	// -------------------------------------- Fields

	@NotBlank
	@Length(max = 50)
	private String name;
	private String description;
	private AbstractUpdateableLocation<?> facility;

	private BenefitingService benefitingService;
	private BenefitingServiceRoleTemplate template;
	private BenefitingServiceRoleType roleType;
	@NotNull
	private ScopeType scope;
	private String contactName;
	@ExtendedEmailValidator(message = "Please enter a valid email in the format 'user@domain.tld'.")
	private String contactEmail;
	@Pattern(regexp = ValidationUtil.PHONE_REGEX, message = "Please enter a valid phone number.")
	private String contactPhone;
	private boolean inactive;
	private boolean requiredAndReadOnly;

	private List<BenefitingServiceRoleRequirementAssociation> benefitingServiceRoleRequirementAssociations;
	private List<VolunteerAssignment> volunteerAssignments;
	private List<OccasionalWorkEntry> occasionalWorkEntries;

	// -------------------------------------- Constructors

	public BenefitingServiceRole() {
	}

	public BenefitingServiceRole(BenefitingServiceRoleTemplate t, BenefitingService parentService,
			AbstractUpdateableLocation<?> facility) {
		setName(t.getName());
		setFacility(facility);
		setBenefitingService(parentService);
		setTemplate(t);
		setScope(ScopeType.NATIONAL);
		setInactive(t.isInactive());
		setRoleType(t.getRoleType());
	}

	// -------------------------------------- Business Methods

	@Transient
	@JsonView(BenefitingServiceRoleView.Basic.class)
	public long getLocationId() {
		return FacilityUtil.getLocationId(getFacility());
	}

	@Transient
	@JsonView({ BenefitingServiceRoleView.Basic.class, VolunteerAssignmentView.Search.class })
	public String getLocationDisplayName() {
		return FacilityUtil.getLocationDisplayName(getFacility());
	}

	@Transient
	@JsonView(BenefitingServiceRoleView.Basic.class)
	public String getFacilityDisplayName() {
		return FacilityUtil.getFacilityDisplayName(getFacility());
	}

	/*
	 * Not a javabean prop because in most cases we want to prepend the parent
	 * name, which could cause lazy loading exceptions unless we ensure it's
	 * attached - CPB
	 */
	@Transient
	public String getDisplayName(boolean includeParentServiceDisplayName) {
		return getDisplayName(this, includeParentServiceDisplayName ? getBenefitingService() : null);
	}

	@Transient
	@JsonIgnore
	public boolean isNational() {
		return getTemplate() != null;
	}

	// -------------------------------------- Common Methods

	@Override
	protected boolean requiredEquals(BenefitingServiceRole oo) {
		return new EqualsBuilder().append(getName(), oo.getName())
				.append(nullSafeGetId(getBenefitingService()), nullSafeGetId(oo.getBenefitingService()))
				.append(nullSafeGetId(getFacility()), nullSafeGetId(oo.getFacility())).isEquals();
	}

	@Override
	protected int requiredHashCode() {
		return new HashCodeBuilder().append(getName()).append(nullSafeGetId(getBenefitingService()))
				.append(nullSafeGetId(getFacility())).toHashCode();
	}

	@Override
	public int compareTo(BenefitingServiceRole o) {
		if (equals(o))
			return 0;

		return new CompareToBuilder().append(nullSafeLowercase(getName()), nullSafeLowercase(o.getName()))
				.append(nullSafeGetId(getFacility()), nullSafeGetId(o.getFacility())).toComparison() > 0 ? 1 : -1;
	}

	@Override
	public String toString() {
		return getName();
	}

	// -------------------------------------- Accessor Methods

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "FacilityFK", nullable = false)
	@JsonView(BenefitingServiceRoleView.Basic.class)
	public AbstractUpdateableLocation<?> getFacility() {
		return facility;
	}

	public void setFacility(AbstractUpdateableLocation<?> facility) {
		this.facility = facility;
	}

	@Convert(converter = BenefitingServiceScopeTypeConverter.class)
	@JsonView(BenefitingServiceRoleView.Basic.class)
	@Column(nullable = false)
	public ScopeType getScope() {
		return scope;
	}

	public void setScope(ScopeType scope) {
		this.scope = scope;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "WR_STD_BenefitingServiceRoleTypesFK")
	@JsonView(BenefitingServiceRoleView.Basic.class)
	public BenefitingServiceRoleType getRoleType() {
		return roleType;
	}

	public void setRoleType(BenefitingServiceRoleType roleType) {
		this.roleType = roleType;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "BenefitingServicesFK", nullable = false)
	@JsonIgnore
	public BenefitingService getBenefitingService() {
		return benefitingService;
	}

	public void setBenefitingService(BenefitingService benefitingService) {
		this.benefitingService = benefitingService;
	}

	@Column(length = 50, nullable = false)
	@JsonView({ BenefitingServiceRoleView.Basic.class, VolunteerAssignmentView.Search.class,
			OccasionalWorkEntryView.TimeReport.class })
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Column(length = 4000)
	@JsonView(BenefitingServiceRoleView.Basic.class)
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@OneToMany(mappedBy = "benefitingServiceRole", fetch = FetchType.LAZY)
	@BatchSize(size = 500)
	@JsonIgnore
	public List<VolunteerAssignment> getVolunteerAssignments() {
		if (volunteerAssignments == null)
			volunteerAssignments = new ArrayList<>();
		return volunteerAssignments;
	}

	public void setVolunteerAssignments(List<VolunteerAssignment> volunteerAssignments) {
		this.volunteerAssignments = volunteerAssignments;
	}

	@OneToMany(mappedBy = "benefitingServiceRole", fetch = FetchType.LAZY)
	@BatchSize(size = 500)
	@JsonIgnore
	public List<OccasionalWorkEntry> getOccasionalWorkEntries() {
		if (occasionalWorkEntries == null)
			occasionalWorkEntries = new ArrayList<>();
		return occasionalWorkEntries;
	}

	public void setOccasionalWorkEntries(List<OccasionalWorkEntry> occasionalWorkEntries) {
		this.occasionalWorkEntries = occasionalWorkEntries;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "BenefitingServiceRoleTemplatesFK")
	@JsonIgnore
	public BenefitingServiceRoleTemplate getTemplate() {
		return template;
	}

	public void setTemplate(BenefitingServiceRoleTemplate template) {
		this.template = template;
	}

	@Column(name = "IsInactive", nullable = false)
	@JsonView(BenefitingServiceRoleView.Basic.class)
	public boolean isInactive() {
		return inactive;
	}

	public void setInactive(boolean inactive) {
		this.inactive = inactive;
	}

	@Column(name = "IsRequiredAndReadOnly", nullable = false)
	@JsonView(BenefitingServiceRoleView.Basic.class)
	public boolean isRequiredAndReadOnly() {
		return requiredAndReadOnly;
	}

	public void setRequiredAndReadOnly(boolean requiredAndReadOnly) {
		this.requiredAndReadOnly = requiredAndReadOnly;
	}

	@Column(length = 50)
	@JsonView(BenefitingServiceView.Basic.class)
	public String getContactName() {
		return contactName;
	}

	public void setContactName(String contactName) {
		this.contactName = contactName;
	}

	@Column(length = 250)
	@JsonView(BenefitingServiceView.Basic.class)
	public String getContactEmail() {
		return contactEmail;
	}

	public void setContactEmail(String contactEmail) {
		this.contactEmail = contactEmail;
	}

	@Column(length = 30)
	@JsonView(BenefitingServiceView.Basic.class)
	public String getContactPhone() {
		return contactPhone;
	}

	public void setContactPhone(String contactPhone) {
		this.contactPhone = contactPhone;
	}

	@OneToMany(mappedBy = "benefitingServiceRole", fetch = FetchType.LAZY)
	@BatchSize(size = 500)
	@JsonIgnore
	private List<BenefitingServiceRoleRequirementAssociation> getBenefitingServiceRoleAssociations() {
		if (benefitingServiceRoleRequirementAssociations == null)
			benefitingServiceRoleRequirementAssociations = new ArrayList<>();
		return benefitingServiceRoleRequirementAssociations;
	}

	public void setBenefitingServiceRoleAssociations(
			List<BenefitingServiceRoleRequirementAssociation> benefitingServiceRoleRequirementAssociations) {
		this.benefitingServiceRoleRequirementAssociations = benefitingServiceRoleRequirementAssociations;
	}

}
