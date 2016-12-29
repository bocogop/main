package org.bocogop.wr.model.volunteer;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.PUBLIC_ONLY;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;

import org.bocogop.shared.model.core.AbstractAuditedVersionedPersistent;
import org.bocogop.wr.model.facility.Facility;

@Entity
@Table(name = "Uniforms", schema = "wr")
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = PUBLIC_ONLY, isGetterVisibility = PUBLIC_ONLY)
public class Uniform extends AbstractAuditedVersionedPersistent<Uniform> implements Comparable<Uniform> {
	private static final long serialVersionUID = 6904844123870655771L;

	public static class UniformView {
		public interface Basic {
		}

		public interface Extended extends Basic {
		}
	}

	// -------------------------------------- Fields

	@NotNull
	private Volunteer volunteer;
	@NotNull
	private ShirtSize shirtSize;
	@NotNull
	private Integer numberOfShirts;
	@NotNull
	private Facility facility;

	// -------------------------------------- Business Methods

	// -------------------------------------- Common Methods

	@Override
	protected boolean requiredEquals(Uniform oo) {
		/*
		 * doubt all three of these are necessary but don't have data to confirm
		 * yet - CPB
		 */
		return new EqualsBuilder().append(nullSafeGetId(getVolunteer()), nullSafeGetId(oo.getVolunteer()))
				.append(nullSafeGetId(getFacility()), nullSafeGetId(oo.getFacility()))
				.append(nullSafeGetId(getShirtSize()), nullSafeGetId(oo.getShirtSize()))
				.append(getNumberOfShirts(), oo.getNumberOfShirts()).isEquals();
	}

	@Override
	protected int requiredHashCode() {
		return new HashCodeBuilder().append(nullSafeGetId(getVolunteer())).append(nullSafeGetId(getFacility()))
				.append(nullSafeGetId(getShirtSize())).append(getNumberOfShirts()).toHashCode();
	}

	@Override
	public int compareTo(Uniform o) {
		if (equals(o))
			return 0;
		return new CompareToBuilder().append(getFacility(), o.getFacility()).append(getShirtSize(), o.getShirtSize())
				.append(numberOfShirts, o.getNumberOfShirts()).toComparison() > 0 ? 1 : -1;
	}

	// -------------------------------------- Accessor Methods

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "WrVolunteersFK", nullable = false)
	@JsonIgnore
	public Volunteer getVolunteer() {
		return volunteer;
	}

	public void setVolunteer(Volunteer volunteer) {
		this.volunteer = volunteer;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ShirtSizesFK")
	@JsonView(UniformView.Extended.class)
	public ShirtSize getShirtSize() {
		return shirtSize;
	}

	public void setShirtSize(ShirtSize shirtSize) {
		this.shirtSize = shirtSize;
	}

	public Integer getNumberOfShirts() {
		return numberOfShirts;
	}

	public void setNumberOfShirts(Integer numberOfShirts) {
		this.numberOfShirts = numberOfShirts;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "FacilityFK")
	@JsonView(UniformView.Extended.class)
	public Facility getFacility() {
		return facility;
	}

	public void setFacility(Facility facility) {
		this.facility = facility;
	}

}
