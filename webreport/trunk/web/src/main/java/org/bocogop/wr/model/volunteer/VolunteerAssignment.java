package org.bocogop.wr.model.volunteer;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.annotations.BatchSize;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;

import org.bocogop.shared.model.core.AbstractAuditedVersionedPersistent;
import org.bocogop.wr.model.benefitingService.BenefitingService;
import org.bocogop.wr.model.benefitingService.BenefitingServiceRole;
import org.bocogop.wr.model.facility.AbstractLocation.BasicLocationView;
import org.bocogop.wr.model.facility.AbstractUpdateableLocation;
import org.bocogop.wr.model.facility.Facility;
import org.bocogop.wr.model.facility.Kiosk.KioskAssignmentsAndOrgsView;
import org.bocogop.wr.model.requirement.VolunteerRequirement.VolunteerRequirementView;
import org.bocogop.wr.model.time.OccasionalWorkEntry.OccasionalWorkEntryView;
import org.bocogop.wr.model.time.WorkEntry;
import org.bocogop.wr.model.time.WorkEntry.WorkEntryView;
import org.bocogop.wr.util.FacilityUtil;

@Entity
@Table(name = "VolunteerAssignments", schema = "wr")
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = NONE, isGetterVisibility = NONE)
public class VolunteerAssignment extends AbstractAuditedVersionedPersistent<VolunteerAssignment> {
	private static final long serialVersionUID = 6904844123870655771L;

	/**
	 * See @JsonView documentation - CPB
	 */
	public static class VolunteerAssignmentView {
		public interface Basic extends BasicLocationView.Basic {
		}

		public interface Extended {
		}

		public interface Search extends Basic {
		}
	}

	// -------------------------------------- Fields

	private Volunteer volunteer;
	private BenefitingService benefitingService;
	private BenefitingServiceRole benefitingServiceRole;
	private AbstractUpdateableLocation<?> facility;
	private Facility rootFacility;
	private boolean inactive;

	private List<WorkEntry> workEntries;

	// -------------------------------------- Constructors

	public VolunteerAssignment() {
	}

	public VolunteerAssignment(Volunteer v, BenefitingServiceRole bsr) {
		setVolunteer(v);
		setBenefitingService(bsr.getBenefitingService());
		setBenefitingServiceRole(bsr);
		setFacility(bsr.getFacility());
		setRootFacility(bsr.getBenefitingService().getFacility());
		setInactive(bsr.isInactive());
	}

	// -------------------------------------- Business Methods

	@Transient
	@JsonView({ KioskAssignmentsAndOrgsView.Combined.class, //
			VolunteerAssignmentView.Basic.class, //
			VolunteerRequirementView.Basic.class, //
			WorkEntryView.TimeReportByVolunteer.class, //
			WorkEntryView.TimeReportByDate.class, //
	})
	public String getDisplayName() {
		return BenefitingServiceRole.getDisplayName(getBenefitingServiceRole(), getBenefitingService());
	}

	@Transient
	@JsonView({ VolunteerAssignmentView.Basic.class, VolunteerAssignmentView.Search.class,
			WorkEntryView.TimeReportByVolunteer.class, WorkEntryView.TimeReportByDate.class,
			KioskAssignmentsAndOrgsView.Combined.class })
	public boolean isActive() {
		return !inactive;
	}

	@Transient
	@JsonView(VolunteerAssignmentView.Extended.class)
	public long getLocationId() {
		return FacilityUtil.getLocationId(getFacility());
	}

	@Transient
	@JsonView({ KioskAssignmentsAndOrgsView.Combined.class, //
			OccasionalWorkEntryView.TimeReport.class, //
			VolunteerAssignmentView.Extended.class, //
			WorkEntryView.TimeReportByVolunteer.class, //
			WorkEntryView.TimeReportByDate.class, })
	public String getLocationDisplayName() {
		return FacilityUtil.getLocationDisplayName(getFacility());
	}

	@Transient
	@JsonView(VolunteerAssignmentView.Extended.class)
	public String getFacilityDisplayName() {
		return FacilityUtil.getFacilityDisplayName(getFacility());
	}

	// -------------------------------------- Common Methods

	@Override
	protected boolean requiredEquals(VolunteerAssignment oo) {
		return new EqualsBuilder().append(nullSafeGetId(getVolunteer()), oo.getVolunteer())
				.append(nullSafeGetId(getBenefitingService()), oo.getBenefitingService())
				.append(nullSafeGetId(getFacility()), nullSafeGetId(oo.getFacility())).isEquals();
	}

	@Override
	protected int requiredHashCode() {
		return new HashCodeBuilder().append(nullSafeGetId(getVolunteer())).append(nullSafeGetId(getBenefitingService()))
				.append(nullSafeGetId(getFacility())).toHashCode();
	}

	// -------------------------------------- Accessor Methods

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "WrVolunteersFK", nullable = false)
	@JsonView({ VolunteerAssignmentView.Search.class, WorkEntryView.TimeReportByVolunteer.class,
			WorkEntryView.TimeReportByDate.class })
	public Volunteer getVolunteer() {
		return volunteer;
	}

	public void setVolunteer(Volunteer volunteer) {
		this.volunteer = volunteer;
	}

	@OneToMany(mappedBy = "volunteerAssignment", fetch = FetchType.LAZY)
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

	@Column(name = "IsInactive", nullable = false)
	@JsonIgnore
	public boolean isInactive() {
		return inactive;
	}

	public void setInactive(boolean inactive) {
		this.inactive = inactive;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "FacilityFK", nullable = false)
	@JsonView({ VolunteerAssignmentView.Search.class, //
			WorkEntryView.TimeReportByVolunteer.class, //
			WorkEntryView.TimeReportByDate.class, //
			OccasionalWorkEntryView.TimeReport.class })
	public AbstractUpdateableLocation<?> getFacility() {
		return facility;
	}

	public void setFacility(AbstractUpdateableLocation<?> facility) {
		this.facility = facility;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "RootFacilityFK", nullable = false)
	public Facility getRootFacility() {
		return rootFacility;
	}

	public void setRootFacility(Facility rootFacility) {
		this.rootFacility = rootFacility;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "WrBenefitingServicesFK", nullable = false)
	@JsonView(VolunteerAssignmentView.Search.class)
	public BenefitingService getBenefitingService() {
		return benefitingService;
	}

	public void setBenefitingService(BenefitingService benefitingService) {
		this.benefitingService = benefitingService;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "WrBenefitingServiceRolesFK", nullable = false)
	@JsonView(VolunteerAssignmentView.Search.class)
	public BenefitingServiceRole getBenefitingServiceRole() {
		return benefitingServiceRole;
	}

	public void setBenefitingServiceRole(BenefitingServiceRole benefitingServiceRole) {
		this.benefitingServiceRole = benefitingServiceRole;
	}

}
