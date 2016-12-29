package org.bocogop.wr.model.donation;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.PUBLIC_ONLY;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;

import org.bocogop.shared.model.core.AbstractAuditedVersionedPersistent;
import org.bocogop.wr.model.facility.Facility;

@Entity
@Table(name = "DonReference", schema = "wr")
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = PUBLIC_ONLY, isGetterVisibility = PUBLIC_ONLY)
public class DonationReference extends AbstractAuditedVersionedPersistent<DonationReference>
		implements Comparable<DonationReference> {

	private static final long serialVersionUID = 3900607532835213411L;
	// -------------------------------------- Fields

	@NotNull
	private Facility facility;
	@Size(max = 50)
	private String donationReference;
	private boolean inactive;

	// -------------------------------------- Constructors

	public DonationReference() {
	}

	public DonationReference(String donationReference) {
		this.donationReference = donationReference;
	}

	// -------------------------------------- Business Methods

	// ---------------------------------------- Common Methods

	@Override
	protected boolean requiredEquals(DonationReference oo) {
		return new EqualsBuilder().append(nullSafeGetId(getFacility()), nullSafeGetId(oo.getFacility()))
				.append(getDonationReference(), oo.getDonationReference()).isEquals();
	}

	@Override
	protected int requiredHashCode() {
		return new HashCodeBuilder().append(nullSafeGetId(getFacility())).append(getDonationReference()).toHashCode();
	}

	@Override
	public int compareTo(DonationReference o) {
		if (equals(o))
			return 0;

		return new CompareToBuilder()
				.append(nullSafeLowercase(getDonationReference()), nullSafeLowercase(o.getDonationReference()))
				.toComparison() > 0 ? 1 : -1;
	}

	@Override
	public String toString() {
		return "\"" + getDonationReference() + "\" at facility " + getFacility();
	}

	// -------------------------------------- Accessor Methods

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "FacilityFK", nullable = false)
	@JsonIgnore
	public Facility getFacility() {
		return facility;
	}

	public void setFacility(Facility facility) {
		this.facility = facility;
	}

	@Column(name = "DonationReference", length = 50)
	public String getDonationReference() {
		return donationReference;
	}

	public void setDonationReference(String donationReference) {
		this.donationReference = donationReference;
	}

	@Transient
	public boolean isActive() {
		return !isInactive();
	}

	@Column(name = "IsInactive", nullable = false)
	public boolean isInactive() {
		return inactive;
	}

	public void setInactive(boolean inactive) {
		this.inactive = inactive;
	}

}
