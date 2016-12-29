package org.bocogop.wr.model.benefitingService;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.PUBLIC_ONLY;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

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

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.SortNatural;
import org.hibernate.validator.constraints.NotBlank;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;

import org.bocogop.shared.model.core.AbstractAuditedVersionedPersistent;
import org.bocogop.wr.model.benefitingService.BenefitingServiceRole.BenefitingServiceRoleView;
import org.bocogop.wr.model.facility.AbstractLocation.BasicLocationView;
import org.bocogop.wr.model.facility.Facility;
import org.bocogop.wr.model.facility.Facility.FacilityView;
import org.bocogop.wr.model.time.OccasionalWorkEntry;
import org.bocogop.wr.model.time.OccasionalWorkEntry.OccasionalWorkEntryView;
import org.bocogop.wr.model.volunteer.VolunteerAssignment;
import org.bocogop.wr.model.volunteer.VolunteerAssignment.VolunteerAssignmentView;
import org.bocogop.wr.persistence.conversion.BenefitingServiceScopeTypeConverter;
import org.bocogop.wr.util.FacilityUtil;

@Entity
@Table(name = "BenefitingServices", schema = "wr")
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = PUBLIC_ONLY, isGetterVisibility = PUBLIC_ONLY)
public class BenefitingService extends AbstractAuditedVersionedPersistent<BenefitingService>
		implements Comparable<BenefitingService> {
	private static final long serialVersionUID = 6904844123870655771L;

	/**
	 * See @JsonView documentation - CPB
	 */
	public static class BenefitingServiceView {
		public interface Basic extends BasicLocationView.Basic {
		}

		public interface Extended extends Basic {
		}

		public interface ListBenefitingServicesWithRoles
				extends Extended, FacilityView.BasicWithScope, BenefitingServiceRoleView.Basic {
		}
	}

	public static String getDisplayName(String name, String subdivision) {
		return name + (StringUtils.isBlank(subdivision) ? "" : " - " + subdivision);
	}

	// -------------------------------------- Fields

	@NotBlank
	private String name;
	private String subdivision;
	private String abbreviation;
	@NotNull
	private ScopeType scope;
	private BenefitingServiceTemplate template;
	private Facility facility;
	private boolean inactive;
	private boolean gamesRelated;

	private SortedSet<BenefitingServiceRole> benefitingServiceRoles;
	private SortedSet<OccasionalWorkEntry> occasionalWorkEntries;
	private List<VolunteerAssignment> volunteerAssignments;

	// -------------------------------------- Constructors

	public BenefitingService() {
	}

	public BenefitingService(BenefitingServiceTemplate s, Facility facility) {
		setName(s.getName());
		setSubdivision(s.getSubdivision());
		setAbbreviation(s.getAbbreviation());
		setScope(ScopeType.NATIONAL);
		setTemplate(s);
		setFacility(facility);
		setInactive(s.isInactive());
		setGamesRelated(s.isGamesRelated());
	}

	// -------------------------------------- Business Methods

	@Transient
	@JsonView({ BenefitingServiceView.Basic.class, VolunteerAssignmentView.Search.class })
	public String getLocationDisplayName() {
		return FacilityUtil.getLocationDisplayName(getFacility());
	}

	@Transient
	@JsonView({ BenefitingServiceView.Basic.class, VolunteerAssignmentView.Search.class })
	public String getFacilityDisplayName() {
		return FacilityUtil.getFacilityDisplayName(getFacility());
	}

	@Transient
	@JsonIgnore
	public String getDisplayName() {
		return getDisplayName(getName(), getSubdivision());
	}

	@Transient
	@JsonIgnore
	public boolean isNational() {
		return getTemplate() != null;
	}

	@Transient
	@JsonIgnore
	public List<BenefitingServiceRole> getRequiredAndReadOnlyRoles() {
		return getBenefitingServiceRoles().stream().filter(p -> p.isRequiredAndReadOnly()).collect(Collectors.toList());
	}

	// -------------------------------------- Common Methods

	@Override
	protected boolean requiredEquals(BenefitingService oo) {
		/*
		 * doubt all three of these are necessary but don't have data to confirm
		 * yet - CPB
		 */
		return new EqualsBuilder().append(getName(), oo.getName())
				.append(nullSafeGetId(getFacility()), nullSafeGetId(oo.getFacility()))
				.append(getSubdivision(), oo.getSubdivision()).append(getScope(), oo.getScope()).isEquals();
	}

	@Override
	protected int requiredHashCode() {
		return new HashCodeBuilder().append(getName()).append(nullSafeGetId(getFacility())).append(getSubdivision())
				.append(getScope()).toHashCode();
	}

	@Override
	public int compareTo(BenefitingService o) {
		if (equals(o))
			return 0;

		return new CompareToBuilder().append(nullSafeLowercase(getDisplayName()), nullSafeLowercase(o.getDisplayName()))
				.append(nullSafeGetId(getFacility()), nullSafeGetId(o.getFacility())).append(getScope(), o.getScope())
				.toComparison() > 0 ? 1 : -1;
	}

	@Override
	public String toString() {
		return getDisplayName();
	}

	// -------------------------------------- Accessor Methods

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "FacilityFK", nullable = false)
	@JsonView(BenefitingServiceView.Basic.class)
	public Facility getFacility() {
		return facility;
	}

	public void setFacility(Facility facility) {
		this.facility = facility;
	}

	@OneToMany(mappedBy = "benefitingService", fetch = FetchType.LAZY)
	@BatchSize(size = 500)
	@JsonIgnore
	@SortNatural
	public SortedSet<OccasionalWorkEntry> getOccasionalWorkEntries() {
		if (occasionalWorkEntries == null)
			occasionalWorkEntries = new TreeSet<>();
		return occasionalWorkEntries;
	}

	public void setOccasionalWorkEntries(SortedSet<OccasionalWorkEntry> occasionalWorkEntries) {
		this.occasionalWorkEntries = occasionalWorkEntries;
	}

	@Column(name = "ServiceName", length = 50, nullable = false)
	@JsonView({ BenefitingServiceView.Basic.class, VolunteerAssignmentView.Search.class,
			OccasionalWorkEntryView.TimeReport.class })
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Column(length = 30)
	@JsonView({ BenefitingServiceView.Basic.class, VolunteerAssignmentView.Search.class })
	public String getSubdivision() {
		return subdivision;
	}

	public void setSubdivision(String subdivision) {
		this.subdivision = subdivision;
	}

	@Column(length = 7)
	@JsonView(BenefitingServiceView.Basic.class)
	public String getAbbreviation() {
		return abbreviation;
	}

	public void setAbbreviation(String abbreviation) {
		this.abbreviation = abbreviation;
	}

	@Convert(converter = BenefitingServiceScopeTypeConverter.class)
	@JsonView(BenefitingServiceView.Basic.class)
	public ScopeType getScope() {
		return scope;
	}

	public void setScope(ScopeType scope) {
		this.scope = scope;
	}

	@OneToMany(mappedBy = "benefitingService", fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
	@BatchSize(size = 500)
	@JsonView(BenefitingServiceView.ListBenefitingServicesWithRoles.class)
	@SortNatural
	public SortedSet<BenefitingServiceRole> getBenefitingServiceRoles() {
		if (benefitingServiceRoles == null)
			benefitingServiceRoles = new TreeSet<>();
		return benefitingServiceRoles;
	}

	public void setBenefitingServiceRoles(SortedSet<BenefitingServiceRole> benefitingServiceRoles) {
		this.benefitingServiceRoles = benefitingServiceRoles;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "BenefitingServiceTemplatesFK")
	@JsonIgnore
	public BenefitingServiceTemplate getTemplate() {
		return template;
	}

	public void setTemplate(BenefitingServiceTemplate template) {
		this.template = template;
	}

	@Column(name = "IsInactive", nullable = false)
	@JsonView(BenefitingServiceView.Basic.class)
	public boolean isInactive() {
		return inactive;
	}

	public void setInactive(boolean inactive) {
		this.inactive = inactive;
	}

	@OneToMany(mappedBy = "benefitingService", fetch = FetchType.LAZY)
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

	@Column(name = "IsGamesRelated", nullable = false)
	@JsonView(BenefitingServiceView.Basic.class)
	public boolean isGamesRelated() {
		return gamesRelated;
	}

	public void setGamesRelated(boolean gamesRelated) {
		this.gamesRelated = gamesRelated;
	}

}
