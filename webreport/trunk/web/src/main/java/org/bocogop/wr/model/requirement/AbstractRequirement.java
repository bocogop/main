package org.bocogop.wr.model.requirement;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.PUBLIC_ONLY;
import static org.bocogop.wr.model.requirement.RequirementScopeType.FACILITY;
import static org.bocogop.wr.model.requirement.RequirementScopeType.GLOBAL;

import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.Hibernate;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.SortNatural;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;

import org.bocogop.shared.model.core.AbstractAuditedVersionedPersistent;
import org.bocogop.wr.model.facility.Facility;
import org.bocogop.wr.model.notification.Notification.NotificationView;
import org.bocogop.wr.model.requirement.VolunteerRequirement.VolunteerRequirementView;
import org.bocogop.wr.persistence.conversion.RequirementTypeConverter;
import org.bocogop.wr.web.conversion.RequirementDateTypeConverter;

@Entity
@Inheritance
@DiscriminatorColumn(name = "Type")
@Table(name = "Requirement", schema = "wr")
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = PUBLIC_ONLY, isGetterVisibility = PUBLIC_ONLY)
public abstract class AbstractRequirement extends AbstractAuditedVersionedPersistent<AbstractRequirement>
		implements Comparable<AbstractRequirement> {
	private static final long serialVersionUID = 583796042812902141L;

	/**
	 * See @JsonView documentation - CPB
	 */
	public static class RequirementView {
		public interface Basic {
		}

		public interface Extended extends Basic {
		}
	}

	public static String getTypeCode(RequirementScopeType scope, RequirementApplicationType type) {
		switch (scope) {
		case GLOBAL:
			switch (type) {
			case ALL_VOLUNTEERS:
				return "G";
			case ROLE_TYPE:
				return "GT";
			case SPECIFIC_ROLES:
				return "GR";
			default:
				throw new AssertionError("Unexpected type " + type);
			}
		case FACILITY:
			switch (type) {
			case ALL_VOLUNTEERS:
				return "F";
			case ROLE_TYPE:
				return "FT";
			case SPECIFIC_ROLES:
				return "FR";
			default:
				throw new AssertionError("Unexpected type " + type);
			}
		default:
			throw new AssertionError("Unexpected scope " + scope);
		}
	}

	public static AbstractRequirement getInstance(RequirementScopeType scope, RequirementApplicationType type) {
		switch (scope) {
		case GLOBAL:
			switch (type) {
			case ALL_VOLUNTEERS:
				return new GlobalRequirement();
			case ROLE_TYPE:
				return new GlobalRoleTypeRequirement();
			case SPECIFIC_ROLES:
				return new GlobalRoleRequirement();
			default:
				throw new AssertionError("Unexpected type " + type);
			}
		case FACILITY:
			switch (type) {
			case ALL_VOLUNTEERS:
				return new FacilityRequirement();
			case ROLE_TYPE:
				return new FacilityRoleTypeRequirement();
			case SPECIFIC_ROLES:
				return new FacilityRoleRequirement();
			default:
				throw new AssertionError("Unexpected type " + type);
			}
		default:
			throw new AssertionError("Unexpected scope " + scope);
		}
	}

	// -------------------------------------- Fields

	@Length(max = 50)
	@NotBlank(message = "Name is required.")
	private String name;
	@Length(max = 250)
	private String description;
	private RequirementType type;
	private boolean inactive;
	private RequirementDateType dateType;
	private Integer daysNotification;
	private SortedSet<RequirementDetailField> detailFields;
	private Set<RequirementAvailableStatus> availableStatuses;
	private boolean preventTimeposting;
	private String tmsCourseId;

	// -------------------------------------- Business Methods

	@Transient
	public abstract Facility getFacilityScope();

	@Transient
	public abstract RequirementApplicationType getApplicationType();

	@Transient
	public RequirementScopeType getScope() {
		return getFacilityScope() == null ? GLOBAL : FACILITY;
	}

	@Transient
	public boolean isActive() {
		return !isInactive();
	}

	@Transient
	public Set<RequirementStatus> getBasicAvailableStatuses() {
		return getAvailableStatuses().stream().map(p -> p.getStatus()).collect(Collectors.toSet());
	}

	// -------------------------------------- Common Methods

	@Transient
	private int getClassOrder() {
		Class<?> clazz = Hibernate.getClass(this);
		if (clazz == GlobalRequirement.class)
			return 0;
		if (clazz == GlobalRoleRequirement.class)
			return 1;
		if (clazz == GlobalRoleTypeRequirement.class)
			return 2;
		if (clazz == FacilityRequirement.class)
			return 3;
		if (clazz == FacilityRoleRequirement.class)
			return 4;
		if (clazz == FacilityRoleTypeRequirement.class)
			return 5;
		throw new AssertionError("Unexpected instance of this class: " + clazz);
	}

	@Override
	public int compareTo(AbstractRequirement o) {
		if (equals(o))
			return 0;

		return new CompareToBuilder().append(getClassOrder(), o.getClassOrder())
				.append(nullSafeLowercase(getName()), nullSafeLowercase(o.getName())).toComparison() > 0 ? 1 : -1;
	}

	@Override
	protected boolean requiredEquals(AbstractRequirement oo) {
		return new EqualsBuilder().append(getName(), oo.getName())
				.append(nullSafeGetId(getFacilityScope()), nullSafeGetId(oo.getFacilityScope())).isEquals();
	}

	@Override
	protected int requiredHashCode() {
		return new HashCodeBuilder().append(getName()).append(nullSafeGetId(getFacilityScope())).toHashCode();
	}

	public String toString() {
		return getName();
	}

	// -------------------------------------- Accessor Methods

	@Column(length = 50, nullable = false)
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Column(length = 250)
	@JsonView(RequirementView.Basic.class)
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Column(name = "TMSCourseId")
	@JsonView(RequirementView.Basic.class)
	public String getTmsCourseId() {
		return tmsCourseId;
	}

	public void setTmsCourseId(String tmsCourseId) {
		this.tmsCourseId = tmsCourseId;
	}

	@Column(name = "IsInactive", nullable = false)
	@JsonView(RequirementView.Basic.class)
	public boolean isInactive() {
		return inactive;
	}

	public void setInactive(boolean inactive) {
		this.inactive = inactive;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "WR_STD_RequirementDateTypeFK", unique = true)
	@Convert(converter = RequirementDateTypeConverter.class)
	@JsonView(value = { NotificationView.NotificationsForUser.class, //
			RequirementView.Basic.class, //
			VolunteerRequirementView.Search.class })
	public RequirementDateType getDateType() {
		return dateType;
	}

	public void setDateType(RequirementDateType dateType) {
		this.dateType = dateType;
	}

	@OneToMany(mappedBy = "requirement", fetch = FetchType.LAZY)
	@BatchSize(size = 1000)
	@JsonIgnore
	@SortNatural
	public SortedSet<RequirementDetailField> getDetailFields() {
		if (detailFields == null)
			detailFields = new TreeSet<>();
		return detailFields;
	}

	public void setDetailFields(SortedSet<RequirementDetailField> detailFields) {
		this.detailFields = detailFields;
	}

	@OneToMany(mappedBy = "requirement", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
	@BatchSize(size = 1000)
	@JsonIgnore
	public Set<RequirementAvailableStatus> getAvailableStatuses() {
		if (availableStatuses == null)
			availableStatuses = new HashSet<>();
		return availableStatuses;
	}

	public void setAvailableStatuses(Set<RequirementAvailableStatus> availableStatuses) {
		this.availableStatuses = availableStatuses;
	}

	@Column(nullable = false)
	@JsonView(RequirementView.Basic.class)
	public boolean isPreventTimeposting() {
		return preventTimeposting;
	}

	public void setPreventTimeposting(boolean preventTimeposting) {
		this.preventTimeposting = preventTimeposting;
	}

	@JsonView({ NotificationView.NotificationsForUser.class, //
			RequirementView.Basic.class, //
			VolunteerRequirementView.Search.class })
	public Integer getDaysNotification() {
		return daysNotification;
	}

	public void setDaysNotification(Integer daysNotification) {
		this.daysNotification = daysNotification;
	}

	@Column(name = "RequirementType")
	@Convert(converter = RequirementTypeConverter.class)
	@JsonView({ NotificationView.NotificationsForUser.class, //
			RequirementView.Basic.class, //
			VolunteerRequirementView.Search.class })
	public RequirementType getType() {
		return type;
	}

	public void setType(RequirementType type) {
		this.type = type;
	}

}