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
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonView;

import org.bocogop.shared.model.core.AbstractAuditedVersionedPersistent;
import org.bocogop.wr.model.facility.Kiosk.KioskAssignmentsAndOrgsView;
import org.bocogop.wr.model.organization.AbstractBasicOrganization;
import org.bocogop.wr.model.volunteer.VolunteerAssignment;
import org.bocogop.wr.util.DateUtil;

@Entity
@Table(name = "Hours", schema = "wr")
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = PUBLIC_ONLY, isGetterVisibility = PUBLIC_ONLY)
public class WorkEntry extends AbstractAuditedVersionedPersistent<WorkEntry> implements Comparable<WorkEntry> {
	private static final long serialVersionUID = 6904844123870655771L;

	public static class WorkEntryView {
		public interface Basic {
		}

		public interface Extended extends Basic {
		}

		public interface TimeReportByVolunteer extends Basic {
		}

		public interface TimeReportByDate extends Basic {
		}

	}

	// -------------------------------------- Fields

	private AbstractBasicOrganization organization;
	private VolunteerAssignment volunteerAssignment;

	@NotNull
	private LocalDate dateWorked;
	private double hoursWorked;
	private int fiscalYear;

	// -------------------------------------- Constructors

	public WorkEntry() {
	}

	public WorkEntry(VolunteerAssignment volunteerAssignment, AbstractBasicOrganization organization,
			LocalDate dateWorked, double hoursWorked) {
		this.volunteerAssignment = volunteerAssignment;
		this.organization = organization;
		this.dateWorked = dateWorked;
		this.hoursWorked = hoursWorked;
	}

	// -------------------------------------- Business Methods

	@Transient
	@JsonView(KioskAssignmentsAndOrgsView.Combined.class)
	public int getFullHours() {
		return (int) Math.floor(getHoursWorked());
	}

	@Transient
	@JsonView(KioskAssignmentsAndOrgsView.Combined.class)
	public int getFullMinutes() {
		return (int) (getHoursWorked() * 60) % 60;
	}

	// -------------------------------------- Common Methods

	@Override
	protected boolean requiredEquals(WorkEntry oo) {
		return new EqualsBuilder()
				.append(nullSafeGetId(getVolunteerAssignment()), nullSafeGetId(oo.getVolunteerAssignment()))
				.append(nullSafeGetId(getOrganization()), nullSafeGetId(oo.getOrganization()))
				.append(getDateWorked(), oo.getDateWorked()).append(getHoursWorked(), oo.getHoursWorked()).isEquals();
	}

	@Override
	protected int requiredHashCode() {
		return new HashCodeBuilder().append(nullSafeGetId(getVolunteerAssignment()))
				.append(nullSafeGetId(getOrganization())).append(getDateWorked()).append(getHoursWorked()).toHashCode();
	}

	@Override
	public int compareTo(WorkEntry o) {
		if (equals(o))
			return 0;

		return new CompareToBuilder().append(getDateWorked(), o.getDateWorked()).toComparison() > 0 ? 1 : -1;
	}

	public String toString() {
		return "{" + getHoursWorked() + " hours on " + getDateWorked().format(DateUtil.DATE_ONLY_FORMAT) + ")";
	}

	// -------------------------------------- Accessor Methods

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "WrOrganizationsFK")
	@JsonView({ KioskAssignmentsAndOrgsView.Combined.class, //
			WorkEntryView.TimeReportByVolunteer.class, //
			WorkEntryView.TimeReportByDate.class })
	public AbstractBasicOrganization getOrganization() {
		return organization;
	}

	public void setOrganization(AbstractBasicOrganization organization) {
		this.organization = organization;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "WrVolunteerAssignmentsFK")
	@JsonView({ KioskAssignmentsAndOrgsView.Combined.class, //
			WorkEntryView.TimeReportByVolunteer.class, //
			WorkEntryView.TimeReportByDate.class })
	public VolunteerAssignment getVolunteerAssignment() {
		return volunteerAssignment;
	}

	public void setVolunteerAssignment(VolunteerAssignment volunteerAssignment) {
		this.volunteerAssignment = volunteerAssignment;
	}

	@JsonView(WorkEntryView.Basic.class)
	@Column(nullable = false)
	public LocalDate getDateWorked() {
		return dateWorked;
	}

	public void setDateWorked(LocalDate dateWorked) {
		this.dateWorked = dateWorked;
	}

	@JsonView(WorkEntryView.Basic.class)
	@Column(nullable = false, scale = 4, precision = 2)
	public double getHoursWorked() {
		return hoursWorked;
	}

	public void setHoursWorked(double hoursWorked) {
		this.hoursWorked = hoursWorked;
	}

	@Column(insertable = false, updatable = false)
	@JsonView(WorkEntryView.Extended.class)
	public int getFiscalYear() {
		return fiscalYear;
	}

	public void setFiscalYear(int fiscalYear) {
		this.fiscalYear = fiscalYear;
	}

}
