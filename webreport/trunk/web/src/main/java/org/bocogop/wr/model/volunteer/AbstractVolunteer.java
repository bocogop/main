package org.bocogop.wr.model.volunteer;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.PUBLIC_ONLY;

import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonView;

import org.bocogop.shared.model.lookup.sds.Gender;
import org.bocogop.shared.model.lookup.sds.State;
import org.bocogop.wr.model.volunteer.Volunteer.VolunteerView;

@MappedSuperclass
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = PUBLIC_ONLY, isGetterVisibility = PUBLIC_ONLY)
public abstract class AbstractVolunteer<T extends AbstractVolunteer<T>> extends AbstractSimpleVolunteer<T> {
	private static final long serialVersionUID = 3222064615857480112L;

	// -------------------------------------- Fields

	@NotNull(message = "Please enter a gender.")
	private Gender gender;

	@NotNull(message = "Please select a state.")
	private State state;

	// -------------------------------------- Constructors

	protected AbstractVolunteer() {
	}

	protected AbstractVolunteer(long id, String lastName, String firstName, String middleName, String nameSuffix) {
		super(id, lastName, firstName, middleName, nameSuffix);
	}

	// -------------------------------------- Business Methods

	@Override
	@Transient
	protected String getStateString() {
		return getState() == null ? null : getState().getPostalName();
	}

	@Override
	@Transient
	protected Long getStateId() {
		return nullSafeGetId(getState());
	}

	// -------------------------------------- Common Methods

	// -------------------------------------- Accessor Methods

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "STD_GenderFK")
	@JsonView(VolunteerView.Search.class)
	public Gender getGender() {
		return gender;
	}

	public void setGender(Gender gender) {
		this.gender = gender;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "STD_StateFK", nullable = false)
	@JsonView(VolunteerView.Extended.class)
	public State getState() {
		return state;
	}

	public void setState(State state) {
		this.state = state;
	}

}
