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
@Table(name = "DonGenPostFund", schema = "wr")
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = PUBLIC_ONLY, isGetterVisibility = PUBLIC_ONLY)
public class DonGenPostFund extends AbstractAuditedVersionedPersistent<DonGenPostFund>
		implements Comparable<DonGenPostFund> {
	private static final long serialVersionUID = 3900607532835213411L;

	public static final String GPF_NONE_VALUE = "None";
	
	// -------------------------------------- Fields

	@NotNull
	private Facility facility;
	@Size(max = 25)
	private String generalPostFund;
	private boolean inactive;

	// -------------------------------------- Constructors

	public DonGenPostFund() {
	}

	public DonGenPostFund(String generalPostFund) {
		this.generalPostFund = generalPostFund;
	}

	// -------------------------------------- Business Methods

	// ---------------------------------------- Common Methods

	@Override
	protected boolean requiredEquals(DonGenPostFund oo) {
		return new EqualsBuilder().append(nullSafeGetId(getFacility()), nullSafeGetId(oo.getFacility()))
				.append(getGeneralPostFund(), oo.getGeneralPostFund()).isEquals();
	}

	@Override
	protected int requiredHashCode() {
		return new HashCodeBuilder().append(nullSafeGetId(getFacility())).append(getGeneralPostFund())
				.toHashCode();
	}

	@Override
	public int compareTo(DonGenPostFund o) {
		if (equals(o))
			return 0;
		return new CompareToBuilder()
				.append(nullSafeLowercase(getGeneralPostFund()), nullSafeLowercase(o.getGeneralPostFund()))
				.toComparison() > 0 ? 1 : -1;
	}
	
	public String toString() {
		return getGeneralPostFund() + (inactive ? " (inactive)" : "");
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

	@Column(name = "GeneralPostFund", length = 25)
	public String getGeneralPostFund() {
		return generalPostFund;
	}

	public void setGeneralPostFund(String generalPostFund) {
		this.generalPostFund = generalPostFund;
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
