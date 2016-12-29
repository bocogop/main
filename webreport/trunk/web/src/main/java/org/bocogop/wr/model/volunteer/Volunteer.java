package org.bocogop.wr.model.volunteer;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.PUBLIC_ONLY;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.BatchSize;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.GrantedAuthority;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;

import org.bocogop.shared.model.CoreUserDetails;
import org.bocogop.wr.model.ObjectScopedToStationNumbers;
import org.bocogop.wr.model.award.Award;
import org.bocogop.wr.model.donation.Donor;
import org.bocogop.wr.model.facility.Facility;
import org.bocogop.wr.model.lookup.Language;
import org.bocogop.wr.model.notification.Notification.NotificationView;
import org.bocogop.wr.model.organization.AbstractBasicOrganization;
import org.bocogop.wr.model.requirement.VolunteerRequirement;
import org.bocogop.wr.model.time.AdjustedHoursEntry;
import org.bocogop.wr.model.volunteer.VolunteerAssignment.VolunteerAssignmentView;
import org.bocogop.wr.util.DateUtil;

/**
 * @author Connor
 *
 */
@Entity
@Table(name = "Volunteers", schema = "wr")
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = PUBLIC_ONLY, isGetterVisibility = PUBLIC_ONLY)
public class Volunteer extends AbstractVolunteer<Volunteer> implements ObjectScopedToStationNumbers, CoreUserDetails {
	private static final long serialVersionUID = 6904844123870655771L;

	public static class VolunteerView {
		public interface Basic {
		}

		public interface Search extends Basic {
		}

		public interface Extended extends Search {
		}

		public interface Demographics extends Basic {
		}
	}

	// -------------------------------------- Fields

	private Language preferredLanguage;
	private String remarks;
	@DateTimeFormat(pattern = DateUtil.TWO_DIGIT_DATE_ONLY)
	private LocalDate entryDate;

	private boolean vaEmployee;
	private String vaEmployeeUsername;

	private Integer lastAwardHours;
	@DateTimeFormat(pattern = DateUtil.TWO_DIGIT_DATE_ONLY)
	private LocalDate lastAwardDate;
	private Award lastAward;

	@DateTimeFormat(pattern = DateUtil.TWO_DIGIT_DATE_ONLY)
	private LocalDate statusDate;

	private VolunteerStatus status;

	private String terminationRemarks;
	private TransportationMethod transportationMethod;

	private Integer mealsEligible;
	private String mealRemarks;

	private List<VolunteerAssignment> volunteerAssignments;
	private List<VolunteerRequirement> volunteerRequirements;
	private Set<VolunteerOrganization> volunteerOrganizations;
	private AbstractBasicOrganization primaryOrganization;
	private Facility originallyCreatedAt;
	private Facility primaryFacility;
	private List<ParkingSticker> parkingStickers;
	private List<Uniform> uniforms;

	private String pivBadgeID;
	private LocalDate pivExpiration;

	private boolean leieApprovalOverride;
	private LocalDate leieExclusionDate;

	private List<AdjustedHoursEntry> timeAdjustments;

	/* Mapping as list to ensure lazy loading - CPB */
	private List<Donor> donor;

	private int grandfatheredYearsWorked;

	/* Transient security fields */
	private Collection<? extends GrantedAuthority> authorities = null;

	// -------------------------------------- Constructors

	public Volunteer() {
	}

	/**
	 * Convenience constructor for when we just want to create a dummy object
	 * for the UI (e.g. Notifications)
	 */
	public Volunteer(long id, String lastName, String firstName, String middleName, String nameSuffix) {
		super(id, lastName, firstName, middleName, nameSuffix);
	}

	// -------------------------------------- Business Methods

	public Set<VolunteerAssignment> getAssignmentsByStatus(boolean activeStatus) {
		return getVolunteerAssignments().stream().filter(p -> activeStatus ? p.isActive() : p.isInactive())
				.collect(Collectors.toSet());
	}

	@Transient
	@Override
	@JsonIgnore
	public boolean isSoundsEnabled() {
		return false;
	}

	@Transient
	@JsonIgnore
	public Donor getDonor() {
		return getDonorList().isEmpty() ? null : getDonorList().get(0);
	}

	@Transient
	@JsonIgnore
	public Collection<String> getScopedToStationNumbers() {
		List<String> results = new ArrayList<>();
		for (VolunteerAssignment assignment : getVolunteerAssignments())
			results.add(assignment.getFacility().getStationNumber());
		return results;
	}

	@Transient
	@JsonIgnore
	public Set<AbstractBasicOrganization> getBasicOrganizations() {
		Set<AbstractBasicOrganization> results = new HashSet<>();
		for (VolunteerOrganization o : getVolunteerOrganizations())
			results.add(o.getOrganization());
		return results;
	}

	public Set<VolunteerOrganization> getVolunteerOrganizationsByStatus(boolean activeStatus) {
		return getVolunteerOrganizations().stream().filter(p -> activeStatus ? p.isActive() : p.isInactive())
				.collect(Collectors.toSet());
	}

	@Transient
	@Override
	public boolean isNationalAdmin() {
		return false;
	}

	@Override
	@Transient
	@JsonIgnore
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return authorities;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthoritiesAtFacility(long facilityId) {
		return authorities;
	}

	public void setAuthorities(Collection<? extends GrantedAuthority> authorities) {
		if (this.authorities != null)
			throw new IllegalStateException(
					"Cannot assign new authorities to volunteer once they have been assigned once.");
		this.authorities = authorities;
	}

	@Transient
	@Override
	@JsonIgnore
	public String getPassword() {
		LocalDate dob = getDateOfBirth();
		if (dob == null)
			return "_INVALID_password*#@$&(#@*)$&!!--1NKWE";
		return dob.format(DateUtil.TWO_DIGIT_DATE_ONLY_FORMAT);
	}

	@Transient
	@Override
	@JsonIgnore
	public String getUsername() {
		return getIdentifyingCode();
	}

	@Transient
	@Override
	@JsonIgnore
	public boolean isAccountNonExpired() {
		return true;
	}

	@Transient
	@Override
	@JsonIgnore
	public boolean isAccountNonLocked() {
		return true;
	}

	@Transient
	@Override
	@JsonIgnore
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Transient
	@Override
	@JsonIgnore
	public boolean isEnabled() {
		return !getStatus().isVolunteerTerminated();
	}

	@Transient
	@Override
	@JsonIgnore
	public ZoneId getTimeZone() {
		return null;
	}

	@Transient
	@JsonIgnore
	public Facility getPrimaryOrOriginallyCreatedAtFacility() {
		return (getPrimaryFacility() != null) ? getPrimaryFacility() : getOriginallyCreatedAt();
	}

	// -------------------------------------- Accessor Methods

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "PreferredLanguageForLoginFK")
	@JsonView(VolunteerView.Extended.class)
	public Language getPreferredLanguage() {
		return preferredLanguage;
	}

	public void setPreferredLanguage(Language preferredLanguage) {
		this.preferredLanguage = preferredLanguage;
	}

	@JsonView(VolunteerView.Extended.class)
	public String getRemarks() {
		return remarks;
	}

	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}

	@Column(name = "TerminatedRemarks")
	@JsonView(VolunteerView.Extended.class)
	public String getTerminationRemarks() {
		return terminationRemarks;
	}

	public void setTerminationRemarks(String terminationRemarks) {
		this.terminationRemarks = terminationRemarks;
	}

	@Column(name = "VAEmployee", nullable = false)
	@JsonView(VolunteerView.Extended.class)
	public boolean isVaEmployee() {
		return vaEmployee;
	}

	public void setVaEmployee(boolean vaEmployee) {
		this.vaEmployee = vaEmployee;
	}

	@JsonView(VolunteerView.Extended.class)
	public String getVaEmployeeUsername() {
		return vaEmployeeUsername;
	}

	public void setVaEmployeeUsername(String vaEmployeeUsername) {
		this.vaEmployeeUsername = vaEmployeeUsername;
	}

	@Column(insertable = false, updatable = false)
	@JsonView(VolunteerView.Extended.class)
	public int getGrandfatheredYearsWorked() {
		return grandfatheredYearsWorked;
	}

	public void setGrandfatheredYearsWorked(int grandfatheredYearsWorked) {
		this.grandfatheredYearsWorked = grandfatheredYearsWorked;
	}

	@Column(name = "HoursLastAward")
	@JsonView(VolunteerView.Extended.class)
	public Integer getLastAwardHours() {
		return lastAwardHours;
	}

	public void setLastAwardHours(Integer lastAwardHours) {
		this.lastAwardHours = lastAwardHours;
	}

	@Column(name = "DateLastAward")
	@JsonView(VolunteerView.Extended.class)
	public LocalDate getLastAwardDate() {
		return lastAwardDate;
	}

	public void setLastAwardDate(LocalDate lastAwardDate) {
		this.lastAwardDate = lastAwardDate;
	}

	@ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@JoinColumn(name = "AwardCodesFK")
	@JsonView(VolunteerView.Extended.class)
	public Award getLastAward() {
		return lastAward;
	}

	public void setLastAward(Award lastAward) {
		this.lastAward = lastAward;
	}

	@JsonView(VolunteerView.Search.class)
	@Column(insertable = false, updatable = false)
	public LocalDate getStatusDate() {
		return statusDate;
	}

	public void setStatusDate(LocalDate statusDate) {
		this.statusDate = statusDate;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "WR_STD_TransportationMethodFK")
	@JsonView(VolunteerView.Extended.class)
	public TransportationMethod getTransportationMethod() {
		return transportationMethod;
	}

	public void setTransportationMethod(TransportationMethod transportationMethod) {
		this.transportationMethod = transportationMethod;
	}

	@Column(name = "EligibleForNumMeals")
	@JsonView(VolunteerView.Extended.class)
	public Integer getMealsEligible() {
		return mealsEligible;
	}

	public void setMealsEligible(Integer mealsEligible) {
		this.mealsEligible = mealsEligible;
	}

	@OneToMany(mappedBy = "volunteer", fetch = FetchType.LAZY)
	@BatchSize(size = 500)
	@JsonIgnore
	public Set<VolunteerOrganization> getVolunteerOrganizations() {
		if (volunteerOrganizations == null)
			volunteerOrganizations = new HashSet<>();
		return volunteerOrganizations;
	}

	public void setVolunteerOrganizations(Set<VolunteerOrganization> volunteerOrganizations) {
		this.volunteerOrganizations = volunteerOrganizations;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "PrimaryOrganizationFK", insertable = false, updatable = false)
	@BatchSize(size = 500)
	@JsonIgnore
	public AbstractBasicOrganization getPrimaryOrganization() {
		return primaryOrganization;
	}

	/* Managed by a native method in VolunteerDAOImpl */
	@SuppressWarnings("unused")
	private void setPrimaryOrganization(AbstractBasicOrganization primaryOrganization) {
		this.primaryOrganization = primaryOrganization;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "PrimaryFacilityFK", insertable = false, updatable = false)
	@BatchSize(size = 500)
	@JsonView({ NotificationView.NotificationsForUser.class, //
			VolunteerAssignmentView.Search.class, //
			VolunteerView.Basic.class, //
	})
	public Facility getPrimaryFacility() {
		return primaryFacility;
	}

	/* Managed by a native method in VolunteerDAOImpl */
	@SuppressWarnings("unused")
	private void setPrimaryFacility(Facility primaryFacility) {
		this.primaryFacility = primaryFacility;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "OriginalFacilityCreatedFK", nullable = false)
	@BatchSize(size = 500)
	@JsonView(VolunteerView.Extended.class)
	public Facility getOriginallyCreatedAt() {
		return originallyCreatedAt;
	}

	public void setOriginallyCreatedAt(Facility originallyCreatedAt) {
		this.originallyCreatedAt = originallyCreatedAt;
	}

	@OneToMany(mappedBy = "volunteer", fetch = FetchType.LAZY)
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

	@OneToMany(mappedBy = "volunteer", fetch = FetchType.LAZY)
	@BatchSize(size = 500)
	@JsonIgnore
	public List<VolunteerRequirement> getVolunteerRequirements() {
		if (volunteerRequirements == null)
			volunteerRequirements = new ArrayList<>();
		return volunteerRequirements;
	}

	public void setVolunteerRequirements(List<VolunteerRequirement> volunteerRequirements) {
		this.volunteerRequirements = volunteerRequirements;
	}

	@JsonView(VolunteerView.Extended.class)
	public LocalDate getEntryDate() {
		return entryDate;
	}

	public void setEntryDate(LocalDate entryDate) {
		this.entryDate = entryDate;
	}

	@OneToMany(mappedBy = "volunteer", fetch = FetchType.LAZY)
	@BatchSize(size = 500)
	@JsonIgnore
	public List<ParkingSticker> getParkingStickers() {
		if (parkingStickers == null)
			parkingStickers = new ArrayList<>();
		return parkingStickers;
	}

	public void setParkingStickers(List<ParkingSticker> parkingStickers) {
		this.parkingStickers = parkingStickers;
	}

	@OneToMany(mappedBy = "volunteer", fetch = FetchType.LAZY)
	@BatchSize(size = 500)
	@JsonIgnore
	public List<Uniform> getUniforms() {
		if (uniforms == null)
			uniforms = new ArrayList<>();
		return uniforms;
	}

	public void setUniforms(List<Uniform> uniforms) {
		this.uniforms = uniforms;
	}

	@Column(name = "PIVBadgeID", length = 100)
	@JsonView(VolunteerView.Extended.class)
	public String getPivBadgeID() {
		return pivBadgeID;
	}

	public void setPivBadgeID(String pivBadgeID) {
		this.pivBadgeID = pivBadgeID;
	}

	@Column(name = "PIVExpiration")
	@JsonView(VolunteerView.Extended.class)
	public LocalDate getPivExpiration() {
		return pivExpiration;
	}

	public void setPivExpiration(LocalDate pivExpiration) {
		this.pivExpiration = pivExpiration;
	}

	@JsonIgnore
	public String getMealRemarks() {
		return mealRemarks;
	}

	public void setMealRemarks(String mealRemarks) {
		this.mealRemarks = mealRemarks;
	}

	@OneToMany(mappedBy = "volunteer", fetch = FetchType.LAZY)
	@BatchSize(size = 500)
	@JsonIgnore
	public List<AdjustedHoursEntry> getTimeAdjustments() {
		if (timeAdjustments == null)
			timeAdjustments = new ArrayList<>();
		return timeAdjustments;
	}

	public void setTimeAdjustments(List<AdjustedHoursEntry> timeAdjustments) {
		this.timeAdjustments = timeAdjustments;
	}

	@OneToMany(mappedBy = "volunteer", fetch = FetchType.LAZY)
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

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "WR_STD_VolunteerStatusFK", nullable = false, insertable = false, updatable = false)
	@BatchSize(size = 500)
	@JsonView(VolunteerView.Search.class)
	public VolunteerStatus getStatus() {
		return status;
	}

	public void setStatus(VolunteerStatus status) {
		this.status = status;
	}

	@Column(nullable = false)
	public boolean isLeieApprovalOverride() {
		return leieApprovalOverride;
	}

	public void setLeieApprovalOverride(boolean leieApprovalOverride) {
		this.leieApprovalOverride = leieApprovalOverride;
	}

	public LocalDate getLeieExclusionDate() {
		return leieExclusionDate;
	}

	public void setLeieExclusionDate(LocalDate leieOverrideExclusionDate) {
		this.leieExclusionDate = leieOverrideExclusionDate;
	}

}
