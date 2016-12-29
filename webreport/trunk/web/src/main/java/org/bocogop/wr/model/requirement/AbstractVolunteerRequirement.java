package org.bocogop.wr.model.requirement;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.PUBLIC_ONLY;

import java.time.LocalDate;
import java.util.Comparator;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonView;

import org.bocogop.shared.model.core.AbstractAuditedVersionedPersistent;
import org.bocogop.wr.model.notification.Notification.NotificationView;
import org.bocogop.wr.model.volunteer.Volunteer;
import org.bocogop.wr.web.conversion.RequirementStatusConverter;

@MappedSuperclass
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = PUBLIC_ONLY, isGetterVisibility = PUBLIC_ONLY)
public abstract class AbstractVolunteerRequirement
		extends AbstractAuditedVersionedPersistent<AbstractVolunteerRequirement> {
	private static final long serialVersionUID = 3810180355498406255L;

	public static class CompareByRequirement implements Comparator<AbstractVolunteerRequirement> {
		@Override
		public int compare(AbstractVolunteerRequirement o1, AbstractVolunteerRequirement o2) {
			if (o1.equals(o2))
				return 0;
			return new CompareToBuilder()
					.append(o1 == null ? null : o1.getRequirement(), o2 == null ? null : o2.getRequirement())
					.toComparison() > 0 ? 1 : -1;
		}
	}

	// ---------------------------------------- Fields

	private Volunteer volunteer;
	private AbstractRequirement requirement;
	private RequirementStatus status;
	private LocalDate requirementDate;
	private String comments;
	private boolean inactive;

	// ---------------------------------------- Business Methods

	// ---------------------------------------- Common Methods

	@Override
	protected boolean requiredEquals(AbstractVolunteerRequirement oo) {
		return new EqualsBuilder().append(nullSafeGetId(getVolunteer()), nullSafeGetId(oo.getVolunteer()))
				.append(nullSafeGetId(getRequirement()), nullSafeGetId(oo.getRequirement())).isEquals();
	}

	@Override
	protected int requiredHashCode() {
		return new HashCodeBuilder().append(nullSafeGetId(getVolunteer())).append(nullSafeGetId(getRequirement()))
				.toHashCode();
	}

	// ---------------------------------------- Accessor Methods

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "RequirementFK", nullable = false)
	public AbstractRequirement getRequirement() {
		return requirement;
	}

	public void setRequirement(AbstractRequirement requirement) {
		this.requirement = requirement;
	}

	@Column(name = "IsInactive", nullable = false)
	public boolean isInactive() {
		return inactive;
	}

	public void setInactive(boolean inactive) {
		this.inactive = inactive;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "VolunteerFK", nullable = false)
	@JsonView({ NotificationView.NotificationsForUser.class })
	public Volunteer getVolunteer() {
		return volunteer;
	}

	public void setVolunteer(Volunteer volunteer) {
		this.volunteer = volunteer;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "WR_STD_RequirementStatusFK", unique = true)
	@Convert(converter = RequirementStatusConverter.class)
	public RequirementStatus getStatus() {
		return status;
	}

	public void setStatus(RequirementStatus status) {
		this.status = status;
	}

	@Column(name = "RequirementDate")
	public LocalDate getRequirementDate() {
		return requirementDate;
	}

	public void setRequirementDate(LocalDate requirementDate) {
		this.requirementDate = requirementDate;
	}

	@Column(length = 2000)
	public String getComments() {
		return comments;
	}

	public void setComments(String comments) {
		this.comments = comments;
	}

}
