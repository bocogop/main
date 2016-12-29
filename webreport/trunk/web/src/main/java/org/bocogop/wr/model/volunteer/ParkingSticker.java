package org.bocogop.wr.model.volunteer;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.PUBLIC_ONLY;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.validator.constraints.NotBlank;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;

import org.bocogop.shared.model.core.AbstractAuditedVersionedPersistent;
import org.bocogop.shared.model.lookup.sds.State;
import org.bocogop.shared.model.lookup.sds.State.StateView;
import org.bocogop.wr.model.facility.Facility;

@Entity
@Table(name = "ParkingSticker", schema = "wr")
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = PUBLIC_ONLY, isGetterVisibility = PUBLIC_ONLY)
public class ParkingSticker extends AbstractAuditedVersionedPersistent<ParkingSticker>
		implements Comparable<ParkingSticker> {
	private static final long serialVersionUID = 6904844123870655771L;

	/**
	 * See @JsonView documentation - CPB
	 */
	public static class ParkingStickerView {
		public interface Basic {
		}

		public interface Extended extends Basic, StateView.Basic {
		}
	}

	// -------------------------------------- Fields

	@NotBlank
	private String stickerNumber;
	private String licensePlate;

	private Volunteer volunteer;
	private State state;
	@NotNull
	private Facility facility;

	// -------------------------------------- Business Methods

	// -------------------------------------- Common Methods

	@Override
	protected boolean requiredEquals(ParkingSticker oo) {
		return new EqualsBuilder().append(getStickerNumber(), oo.getStickerNumber())
				.append(getLicensePlate(), oo.getLicensePlate())
				.append(nullSafeGetId(getState()), nullSafeGetId(oo.getState())).isEquals();
	}

	@Override
	protected int requiredHashCode() {
		return new HashCodeBuilder().append(getStickerNumber()).append(getLicensePlate())
				.append(nullSafeGetId(getState())).toHashCode();
	}

	@Override
	public int compareTo(ParkingSticker o) {
		if (equals(o))
			return 0;

		return new CompareToBuilder().append(getStickerNumber(), o.getStickerNumber()).toComparison() > 0 ? 1 : -1;
	}

	public String toString() {
		return getStickerNumber() + " (license " + getLicensePlate() + ")";
	}

	// -------------------------------------- Accessor Methods

	@Column(length = 13, nullable = false)
	public String getStickerNumber() {
		return stickerNumber;
	}

	public void setStickerNumber(String stickerNumber) {
		this.stickerNumber = stickerNumber;
	}

	@Column(length = 12)
	public String getLicensePlate() {
		return licensePlate;
	}

	public void setLicensePlate(String licensePlate) {
		this.licensePlate = licensePlate;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "WrVolunteersFK")
	@JsonIgnore
	public Volunteer getVolunteer() {
		return volunteer;
	}

	public void setVolunteer(Volunteer volunteer) {
		this.volunteer = volunteer;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "STD_StateFK")
	@JsonView(ParkingStickerView.Extended.class)
	public State getState() {
		return state;
	}

	public void setState(State state) {
		this.state = state;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "FacilityFK")
	@JsonView(ParkingStickerView.Extended.class)
	public Facility getFacility() {
		return facility;
	}

	public void setFacility(Facility facility) {
		this.facility = facility;
	}

}
