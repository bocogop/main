package org.bocogop.wr.model.mealTicket;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.PUBLIC_ONLY;

import java.time.LocalDate;
import java.time.ZonedDateTime;

import javax.persistence.Column;
import javax.persistence.Convert;
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
import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonView;

import org.bocogop.shared.model.core.AbstractAuditedVersionedPersistent;
import org.bocogop.shared.util.StringUtil;
import org.bocogop.wr.model.facility.Facility;
import org.bocogop.wr.model.volunteer.Volunteer;
import org.bocogop.wr.persistence.conversion.MealTicketRequestTypeConverter;
import org.bocogop.wr.util.DateUtil;

@Entity
@Table(name = "MealsProvided", schema = "wr")
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = PUBLIC_ONLY, isGetterVisibility = PUBLIC_ONLY)
public class MealTicket extends AbstractAuditedVersionedPersistent<MealTicket> implements Comparable<MealTicket> {

	private static final long serialVersionUID = -8719706544473483784L;

	public static class MealTicketView {
		public interface Basic {
		}

		public interface Search extends Basic {
		}

		public interface Extended extends Basic {
		}
	}

	// -------------------------------------- Fields

	private LocalDate mealDate;
	private Facility facility;
	private int numberOfMeals;
	private Volunteer volunteer;
	private String occasionalLastName;
	private String occasionalFirstName;
	private boolean reprinted;
	private boolean unscheduled;
	private MealTicketRequestType howAdded;
	private ZonedDateTime ticketRequestTime;
	private ZonedDateTime lastPrintedDate;

	// -------------------------------------- Constructors

	public MealTicket() {
	}

	private MealTicket(MealTicketRequestType howAdded, ZonedDateTime ticketRequestTime) {
		this.howAdded = howAdded;
		this.numberOfMeals = 1;
		this.ticketRequestTime = ticketRequestTime;
	}

	/* For scheduled volunteers */
	public MealTicket(LocalDate mealDate, Facility facility, Volunteer volunteer, MealTicketRequestType howAdded,
			ZonedDateTime ticketRequestTime) {
		this(howAdded, ticketRequestTime);
		this.mealDate = mealDate;
		this.facility = facility;
		this.volunteer = volunteer;
		this.howAdded = howAdded;
		this.unscheduled = false;
	}

	/* For occasional volunteers */
	public MealTicket(LocalDate mealDate, Facility facility, String occasionalLastName, String occasionalFirstName,
			ZonedDateTime ticketRequestTime) {
		this(MealTicketRequestType.MANUAL, ticketRequestTime);
		this.facility = facility;
		this.mealDate = mealDate;
		this.occasionalLastName = occasionalLastName;
		this.occasionalFirstName = occasionalFirstName;
		this.unscheduled = true;
	}

	// -------------------------------------- Business Methods

	@Transient
	@JsonView(MealTicketView.Basic.class)
	public String getDisplayName() {
		return getVolunteer() != null ? getVolunteer().getDisplayName()
				: StringUtil.getDisplayName(true, getOccasionalFirstName(), null, getOccasionalLastName(), null);
	}

	// -------------------------------------- Common Methods

	@Override
	public int compareTo(MealTicket o) {
		if (equals(o))
			return 0;

		return new CompareToBuilder().append(nullSafeLowercase(getDisplayName()), nullSafeLowercase(o.getDisplayName()))
				.toComparison() > 0 ? 1 : -1;
	}

	@Override
	protected boolean requiredEquals(MealTicket oo) {
		return new EqualsBuilder().append(nullSafeGetId(getVolunteer()), nullSafeGetId(oo.getVolunteer()))
				.append(getOccasionalLastName(), oo.getOccasionalLastName())
				.append(getOccasionalFirstName(), oo.getOccasionalFirstName())
				.append(nullSafeGetId(getFacility()), nullSafeGetId(oo.getFacility()))
				.append(getMealDate(), oo.getMealDate()).append(getTicketRequestTime(), oo.getTicketRequestTime())
				.isEquals();
	}

	@Override
	protected int requiredHashCode() {
		return new HashCodeBuilder().append(nullSafeGetId(getVolunteer())).append(nullSafeGetId(getFacility()))
				.append(getTicketRequestTime()).toHashCode();
	}

	// -------------------------------------- Accessor Methods

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "VolunteerFK", unique = true)
	@JsonView(MealTicketView.Basic.class)
	public Volunteer getVolunteer() {
		return volunteer;
	}

	public void setVolunteer(Volunteer volunteer) {
		this.volunteer = volunteer;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "FacilityFK", unique = true)
	@JsonView(MealTicketView.Basic.class)
	@NotNull
	public Facility getFacility() {
		return facility;
	}

	public void setFacility(Facility facility) {
		this.facility = facility;
	}

	@Column(nullable = false)
	public int getNumberOfMeals() {
		return numberOfMeals;
	}

	public void setNumberOfMeals(int numberOfMeals) {
		this.numberOfMeals = numberOfMeals;
	}

	@Column(name = "OccVolLastName", length = 30)
	@JsonView(MealTicketView.Basic.class)
	public String getOccasionalLastName() {
		return occasionalLastName;
	}

	public void setOccasionalLastName(String occasionalLastName) {
		this.occasionalLastName = occasionalLastName;
	}

	@Column(name = "OccVolFirstName", length = 30)
	@JsonView(MealTicketView.Basic.class)
	public String getOccasionalFirstName() {
		return occasionalFirstName;
	}

	public void setOccasionalFirstName(String occasionalFirstName) {
		this.occasionalFirstName = occasionalFirstName;
	}

	@Column(name = "DateOfMeal")
	@DateTimeFormat(pattern = DateUtil.TWO_DIGIT_DATE_ONLY)
	@JsonView(MealTicketView.Basic.class)
	public LocalDate getMealDate() {
		return mealDate;
	}

	public void setMealDate(LocalDate mealDate) {
		this.mealDate = mealDate;
	}

	@Column(name = "IsReprinted")
	@JsonView(MealTicketView.Basic.class)
	public Boolean getReprinted() {
		return reprinted;
	}

	@Transient
	public Boolean isReprinted() {
		return reprinted;
	}

	public void setReprinted(Boolean reprinted) {
		if (reprinted == null)
			reprinted = false;
		this.reprinted = reprinted;
	}

	@Column(name = "IsUnscheduled")
	@JsonView(MealTicketView.Basic.class)
	public Boolean isUnscheduled() {
		return unscheduled;
	}

	public void setUnscheduled(Boolean unscheduled) {
		if (unscheduled == null)
			unscheduled = false;
		this.unscheduled = unscheduled;
	}

	@Column(name = "HowAdded")
	@JsonView(MealTicketView.Basic.class)
	@Convert(converter = MealTicketRequestTypeConverter.class)
	public MealTicketRequestType getHowAdded() {
		return howAdded;
	}

	public void setHowAdded(MealTicketRequestType howAdded) {
		this.howAdded = howAdded;
	}

	@Column(name = "TimeLastPrinted")
	@JsonView(MealTicketView.Basic.class)
	public ZonedDateTime getLastPrintedDate() {
		return lastPrintedDate;
	}

	public void setLastPrintedDate(ZonedDateTime lastPrintedDate) {
		this.lastPrintedDate = lastPrintedDate;
	}

	@Column(name = "TicketRequestTime")
	@JsonView(MealTicketView.Extended.class)
	public ZonedDateTime getTicketRequestTime() {
		return ticketRequestTime;
	}

	public void setTicketRequestTime(ZonedDateTime ticketRequestTime) {
		this.ticketRequestTime = ticketRequestTime;
	}

}
