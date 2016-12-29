package org.bocogop.wr.model.expenditure;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.PUBLIC_ONLY;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

import org.bocogop.shared.model.core.AbstractAuditedVersionedPersistent;
import org.bocogop.wr.model.donation.DonationSummary;

@Entity
@Table(name = "ExpenditureDonationAssociation", schema = "wr")
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = PUBLIC_ONLY, isGetterVisibility = PUBLIC_ONLY)
public class ExpenditureDonationAssociation extends AbstractAuditedVersionedPersistent<ExpenditureDonationAssociation> {
	private static final long serialVersionUID = 583796042812902141L;

	// -------------------------------------- Fields

	private Expenditure expenditure;
	private DonationSummary donation;

	// -------------------------------------- Constructors

	public ExpenditureDonationAssociation() {
	}

	public ExpenditureDonationAssociation(Expenditure expenditure, DonationSummary donation) {
		this.expenditure = expenditure;
		this.donation = donation;
	}

	// -------------------------------------- Business Methods

	// -------------------------------------- Common Methods

	@Override
	protected boolean requiredEquals(ExpenditureDonationAssociation oo) {
		return new EqualsBuilder().append(nullSafeGetId(getExpenditure()), nullSafeGetId(oo.getExpenditure()))
				.append(nullSafeGetId(getDonation()), nullSafeGetId(oo.getDonation())).isEquals();
	}

	@Override
	protected int requiredHashCode() {
		return new HashCodeBuilder().append(nullSafeGetId(getExpenditure())).append(nullSafeGetId(getDonation()))
				.toHashCode();
	}

	// -------------------------------------- Accessor Methods

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ExpenditureFK", nullable = false)
	public Expenditure getExpenditure() {
		return expenditure;
	}

	public void setExpenditure(Expenditure expenditure) {
		this.expenditure = expenditure;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "DonationSummaryFK", nullable = false)
	public DonationSummary getDonation() {
		return donation;
	}

	public void setDonation(DonationSummary donation) {
		this.donation = donation;
	}

}
