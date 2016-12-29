package org.bocogop.wr.model.time;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.PUBLIC_ONLY;

import java.time.LocalDate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonView;

import org.bocogop.shared.model.core.AbstractAuditedVersionedPersistent;
import org.bocogop.wr.model.facility.Facility;
import org.bocogop.wr.model.volunteer.Volunteer;
import org.bocogop.wr.util.DateUtil;

@Entity
@Table(name = "VolunteerAdjustments", schema = "wr")
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = PUBLIC_ONLY, isGetterVisibility = PUBLIC_ONLY)
public class AdjustedHoursEntry extends AbstractAuditedVersionedPersistent<AdjustedHoursEntry>
		implements Comparable<AdjustedHoursEntry> {
	private static final long serialVersionUID = 6904844123870655771L;

	public static class AdjustedHoursEntryView {
		public interface Basic {
		}

		public interface Extended extends Basic {
		}
	}

	// -------------------------------------- Fields

	private Facility facility;
	private Volunteer volunteer;

	private LocalDate date;
	private double hours;
	private String description;

	// -------------------------------------- Business Methods

	// -------------------------------------- Common Methods

	@Override
	protected boolean requiredEquals(AdjustedHoursEntry oo) {
		return new EqualsBuilder().append(nullSafeGetId(getVolunteer()), nullSafeGetId(oo.getVolunteer()))
				.append(nullSafeGetId(getFacility()), nullSafeGetId(oo.getFacility())).append(getDate(), oo.getDate())
				.append(getHours(), oo.getHours()).isEquals();
	}

	@Override
	protected int requiredHashCode() {
		return new HashCodeBuilder().append(nullSafeGetId(getVolunteer())).append(nullSafeGetId(getFacility()))
				.append(getDate()).append(getHours()).toHashCode();
	}

	@Override
	public int compareTo(AdjustedHoursEntry o) {
		if (equals(o))
			return 0;

		return new CompareToBuilder().append(getDate(), o.getDate()).toComparison() > 0 ? 1 : -1;
	}

	public String toString() {
		return "{Adjustment of " + getHours() + " hours on " + getDate().format(DateUtil.DATE_ONLY_FORMAT) + ")";
	}

	// -------------------------------------- Accessor Methods

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "StationFK")
	@JsonView(AdjustedHoursEntryView.Extended.class)
	public Facility getFacility() {
		return facility;
	}

	public void setFacility(Facility facility) {
		this.facility = facility;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "WrVolunteersFK")
	@JsonView(AdjustedHoursEntryView.Extended.class)
	public Volunteer getVolunteer() {
		return volunteer;
	}

	public void setVolunteer(Volunteer volunteer) {
		this.volunteer = volunteer;
	}

	@JsonView(AdjustedHoursEntryView.Basic.class)
	@Column(name = "AdjustmentDate", nullable = false)
	public LocalDate getDate() {
		return date;
	}

	public void setDate(LocalDate date) {
		this.date = date;
	}

	@JsonView(AdjustedHoursEntryView.Basic.class)
	@Column(name = "AdjustmentHours", nullable = false)
	public double getHours() {
		return hours;
	}

	public void setHours(double hours) {
		this.hours = hours;
	}

	@JsonView(AdjustedHoursEntryView.Extended.class)
	@Column(name = "AdjustmentDescription", length = 256, nullable = false)
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

}
