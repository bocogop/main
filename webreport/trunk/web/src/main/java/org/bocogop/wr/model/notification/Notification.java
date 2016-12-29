package org.bocogop.wr.model.notification;

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
import javax.persistence.Transient;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;

import org.bocogop.shared.model.AppUser;
import org.bocogop.shared.model.Permission;
import org.bocogop.shared.model.Role;
import org.bocogop.shared.model.core.AbstractAuditedVersionedPersistent;
import org.bocogop.wr.model.facility.Facility;
import org.bocogop.wr.model.requirement.AbstractRequirement;
import org.bocogop.wr.model.requirement.VolunteerRequirement;
import org.bocogop.wr.model.volunteer.Volunteer;
import org.bocogop.wr.persistence.conversion.NotificationLinkTypeConverter;
import org.bocogop.wr.persistence.conversion.NotificationSeverityTypeConverter;
import org.bocogop.wr.persistence.conversion.NotificationTypeConverter;

@Entity
@Table(name = "Notification", schema = "wr")
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = PUBLIC_ONLY, isGetterVisibility = PUBLIC_ONLY)
public class Notification extends AbstractAuditedVersionedPersistent<Notification> implements Comparable<Notification> {

	private static final long serialVersionUID = -8719706544473483784L;

	public static class NotificationView {
		public interface Basic {
		}

		public interface NotificationsForUser extends Basic {
		}
	}

	// -------------------------------------- Fields

	private String name;
	private String description;
	private NotificationSeverityType severity;
	private NotificationType type;
	private LocalDate beginDate;
	private LocalDate expirationDate;
	private boolean clearable;

	private NotificationLinkType link;
	private NotificationLinkType link2;
	private NotificationLinkType link3;

	private AppUser originatingUser;
	private Facility originatingFacility;

	private Role targetRole;
	private Permission targetPermission;
	private Facility targetFacility;
	private AppUser targetUser;
	private Volunteer targetVolunteer;

	private Volunteer referenceVolunteer;
	private AbstractRequirement referenceRequirement;
	private VolunteerRequirement referenceVolunteerRequirement;

	private Integer referenceAuditFromVersion;
	private Integer referenceAuditToVersion;

	/* */
	private String uniqueIdOverride;

	// -------------------------------------- Constructors

	public Notification() {
	}

	public Notification(String name, String description, NotificationSeverityType severity, NotificationType type,
			LocalDate beginDate, LocalDate expirationDate, AppUser originatingUser, Facility originatingFacility,
			boolean clearable, NotificationLinkType... linkTypes) {
		if (type == null)
			throw new IllegalArgumentException("Type cannot be null");
		if (severity == null)
			throw new IllegalArgumentException("Severity cannot be null");

		this.name = name;
		this.description = description;
		this.severity = severity;
		this.type = type;
		this.beginDate = beginDate;
		this.expirationDate = expirationDate;
		this.originatingUser = originatingUser;
		this.originatingFacility = originatingFacility;
		this.clearable = clearable;

		if (linkTypes != null) {
			if (linkTypes.length > 0)
				this.link = linkTypes[0];
			if (linkTypes.length > 1)
				this.link2 = linkTypes[1];
			if (linkTypes.length > 2)
				this.link3 = linkTypes[2];
		}
	}

	// -------------------------------------- Business Methods

	@Transient
	public String getDisplayName() {
		return getType().getName() + " (" + getSeverity().getName() + ") - " + getName();
	}

	@Transient
	public String getUniqueIdentifier() {
		if (uniqueIdOverride != null)
			return uniqueIdOverride;
		return String.valueOf(getId());
	}

	public Notification setUniqueIdentifier(String uniqueIdentifier) {
		this.uniqueIdOverride = uniqueIdentifier;
		return this;
	}

	// --------- Syntax sugar - CPB

	public Notification withTargetRole(Role role) {
		setTargetRole(role);
		return this;
	}

	public Notification withTargetPermission(Permission permission) {
		setTargetPermission(permission);
		return this;
	}
	
	public Notification withTargetFacility(Facility facility) {
		setTargetFacility(facility);
		return this;
	}

	public Notification withTargetUser(AppUser targetUser) {
		setTargetUser(targetUser);
		return this;
	}

	public Notification withTargetVolunteer(Volunteer targetVolunteer) {
		setTargetVolunteer(targetVolunteer);
		return this;
	}

	public Notification withRefVolunteer(Volunteer referenceVolunteer) {
		setReferenceVolunteer(referenceVolunteer);
		return this;
	}

	public Notification withRefRequirement(AbstractRequirement referenceRequirement) {
		setReferenceRequirement(referenceRequirement);
		return this;
	}

	public Notification withRefVolunteerRequirement(VolunteerRequirement referenceVolunteerRequirement) {
		setReferenceVolunteerRequirement(referenceVolunteerRequirement);
		setReferenceVolunteer(referenceVolunteerRequirement.getVolunteer());
		setReferenceRequirement(referenceVolunteerRequirement.getRequirement());
		return this;
	}

	public Notification withRefAuditFromVersion(int referenceAuditFromVersion) {
		setReferenceAuditFromVersion(referenceAuditFromVersion);
		return this;
	}
	
	public Notification withRefAuditToVersion(int referenceAuditToVersion) {
		setReferenceAuditToVersion(referenceAuditToVersion);
		return this;
	}
	
	// -------------------------------------- Common Methods

	@Override
	public int compareTo(Notification o) {
		if (equals(o))
			return 0;

		return new CompareToBuilder() //
				.append(getSeverity(), o.getSeverity()) //
				.append(getType().getSortOrder(), o.getType().getSortOrder()) //
				.append(o.getBeginDate(), getBeginDate()) //
				.append(o.getExpirationDate(), getExpirationDate()) //
				.append(nullSafeLowercase(getName()), nullSafeLowercase(o.getName())) //
				.append(nullSafeGetId(getTargetRole()), nullSafeGetId(o.getTargetRole())) //
				.append(nullSafeGetId(getTargetFacility()), nullSafeGetId(o)) //
				.append(nullSafeGetId(getTargetUser()), nullSafeGetId(o.getTargetUser())) //
				.append(nullSafeGetId(getTargetVolunteer()), nullSafeGetId(o.getTargetVolunteer())) //
				.append(nullSafeGetId(getReferenceVolunteer()), nullSafeGetId(o.getReferenceVolunteer())) //
				.append(getDescription(), o.getDescription()) //
				.toComparison() > 0 ? 1 : -1;
	}

	@Override
	protected boolean requiredEquals(Notification oo) {
		return new EqualsBuilder() //
				.append(getSeverity(), oo.getSeverity()) //
				.append(getType(), oo.getType()) //
				.append(getName(), oo.getName()) //
				.append(getBeginDate(), oo.getBeginDate()) //
				.append(getExpirationDate(), oo.getExpirationDate()) //
				.append(nullSafeGetId(getTargetRole()), nullSafeGetId(oo.getTargetRole())) //
				.append(nullSafeGetId(getTargetFacility()), nullSafeGetId(oo.getTargetFacility())) //
				.append(nullSafeGetId(getTargetUser()), nullSafeGetId(oo.getTargetUser())) //
				.append(nullSafeGetId(getTargetVolunteer()), nullSafeGetId(oo.getTargetVolunteer())) //
				.append(nullSafeGetId(getReferenceVolunteer()), nullSafeGetId(oo.getReferenceVolunteer())) //
				.append(getDescription(), oo.getDescription()) //
				.isEquals();
	}

	@Override
	protected int requiredHashCode() {
		return new HashCodeBuilder() //
				.append(getSeverity()) //
				.append(getType()) //
				.append(getName()) //
				.append(getBeginDate()) //
				.append(getExpirationDate()) //
				.append(nullSafeGetId(getTargetRole())) //
				.append(nullSafeGetId(getTargetFacility())) //
				.append(nullSafeGetId(getTargetUser())) //
				.append(nullSafeGetId(getTargetVolunteer())) //
				.append(nullSafeGetId(getReferenceVolunteer())) //
				.append(getDescription()) //
				.toHashCode();
	}

	// -------------------------------------- Accessor Methods

	@Column(nullable = false, length = 50)
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Column(nullable = false, length = 200)
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Integer getReferenceAuditFromVersion() {
		return referenceAuditFromVersion;
	}

	public void setReferenceAuditFromVersion(Integer referenceAuditFromVersion) {
		this.referenceAuditFromVersion = referenceAuditFromVersion;
	}

	public Integer getReferenceAuditToVersion() {
		return referenceAuditToVersion;
	}

	public void setReferenceAuditToVersion(Integer referenceAuditToVersion) {
		this.referenceAuditToVersion = referenceAuditToVersion;
	}

	@Column(name = "LinkType")
	@Convert(converter = NotificationLinkTypeConverter.class)
	public NotificationLinkType getLink() {
		return link;
	}

	public void setLink(NotificationLinkType link) {
		this.link = link;
	}

	@Column(name = "Link2Type")
	@Convert(converter = NotificationLinkTypeConverter.class)
	public NotificationLinkType getLink2() {
		return link2;
	}

	public void setLink2(NotificationLinkType link2) {
		this.link2 = link2;
	}

	@Column(name = "Link3Type")
	@Convert(converter = NotificationLinkTypeConverter.class)
	public NotificationLinkType getLink3() {
		return link3;
	}

	public void setLink3(NotificationLinkType link3) {
		this.link3 = link3;
	}

	@Column(nullable = false)
	@Convert(converter = NotificationSeverityTypeConverter.class)
	public NotificationSeverityType getSeverity() {
		return severity;
	}

	public void setSeverity(NotificationSeverityType severity) {
		this.severity = severity;
	}

	@Column(nullable = false)
	@Convert(converter = NotificationTypeConverter.class)
	public NotificationType getType() {
		return type;
	}

	public void setType(NotificationType type) {
		this.type = type;
	}

	@Column(nullable = false)
	public LocalDate getBeginDate() {
		return beginDate;
	}

	public void setBeginDate(LocalDate beginDate) {
		this.beginDate = beginDate;
	}

	public LocalDate getExpirationDate() {
		return expirationDate;
	}

	public void setExpirationDate(LocalDate expirationDate) {
		this.expirationDate = expirationDate;
	}

	@Column(nullable = false, name = "IsClearable")
	public boolean isClearable() {
		return clearable;
	}

	public void setClearable(boolean clearable) {
		this.clearable = clearable;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "RoleFK")
	@JsonView({ NotificationView.NotificationsForUser.class })
	public Role getTargetRole() {
		return targetRole;
	}

	public void setTargetRole(Role targetRole) {
		this.targetRole = targetRole;
	}
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "PermissionFK")
	@JsonView({ NotificationView.NotificationsForUser.class })
	public Permission getTargetPermission() {
		return targetPermission;
	}

	public void setTargetPermission(Permission targetPermission) {
		this.targetPermission = targetPermission;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "FacilityFK")
	@JsonIgnore
	public Facility getTargetFacility() {
		return targetFacility;
	}

	public void setTargetFacility(Facility targetFacility) {
		this.targetFacility = targetFacility;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "AppUserFK")
	@JsonIgnore
	public AppUser getTargetUser() {
		return targetUser;
	}

	public void setTargetUser(AppUser targetUser) {
		this.targetUser = targetUser;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "VolunteerFK")
	@JsonIgnore
	public Volunteer getTargetVolunteer() {
		return targetVolunteer;
	}

	public void setTargetVolunteer(Volunteer targetVolunteer) {
		this.targetVolunteer = targetVolunteer;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ReferenceVolunteerFK")
	@JsonView({ NotificationView.NotificationsForUser.class })
	public Volunteer getReferenceVolunteer() {
		return referenceVolunteer;
	}

	void setReferenceVolunteer(Volunteer referenceVolunteer) {
		this.referenceVolunteer = referenceVolunteer;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ReferenceRequirementFK")
	@JsonView({})
	public AbstractRequirement getReferenceRequirement() {
		return referenceRequirement;
	}

	void setReferenceRequirement(AbstractRequirement referenceRequirement) {
		this.referenceRequirement = referenceRequirement;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ReferenceVolunteerRequirementFK")
	@JsonView({ NotificationView.NotificationsForUser.class })
	public VolunteerRequirement getReferenceVolunteerRequirement() {
		return referenceVolunteerRequirement;
	}

	void setReferenceVolunteerRequirement(VolunteerRequirement referenceVolunteerRequirement) {
		this.referenceVolunteerRequirement = referenceVolunteerRequirement;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "OriginatingAppUserFK")
	@JsonView(NotificationView.NotificationsForUser.class)
	public AppUser getOriginatingUser() {
		return originatingUser;
	}

	public void setOriginatingUser(AppUser originatingUser) {
		this.originatingUser = originatingUser;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "OriginatingFacilityFK")
	@JsonView(NotificationView.NotificationsForUser.class)
	public Facility getOriginatingFacility() {
		return originatingFacility;
	}

	public void setOriginatingFacility(Facility originatingFacility) {
		this.originatingFacility = originatingFacility;
	}

}
