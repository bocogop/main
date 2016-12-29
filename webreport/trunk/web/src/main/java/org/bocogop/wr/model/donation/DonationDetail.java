package org.bocogop.wr.model.donation;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.PUBLIC_ONLY;

import java.math.BigDecimal;

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

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;

import org.bocogop.shared.model.core.AbstractAuditedVersionedPersistent;
import org.bocogop.wr.model.donation.DonationSummary.DonationSummaryView;
import org.bocogop.wr.model.expenditure.Expenditure.ExpenditureView;

@Entity
@Table(name = "DonationDetail", schema = "wr")
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = PUBLIC_ONLY, isGetterVisibility = PUBLIC_ONLY)
public class DonationDetail extends AbstractAuditedVersionedPersistent<DonationDetail>
		implements Comparable<DonationDetail> {
	private static final long serialVersionUID = 5218483913611148504L;

	/**
	 * See @JsonView documentation - CPB
	 */
	public static class DonationDetailView {
		public interface Basic {
		}

		public interface Extended extends Basic {
		}
	}

	// -------------------------------------- Fields

	private DonationSummary donationSummary;
	private DonGenPostFund donGenPostFund;
	private BigDecimal donationValue;

	// -------------------------------------- Business Methods

	// ---------------------------------------- Common Methods

	@Override
	protected boolean requiredEquals(DonationDetail oo) {
		return new EqualsBuilder().append(nullSafeGetId(getDonationSummary()), nullSafeGetId(oo.getDonationSummary()))
				.append(nullSafeGetId(getDonGenPostFund()), nullSafeGetId(oo.getDonGenPostFund()))
				.append(nullSafeGetDoubleValue(getDonationValue()), nullSafeGetDoubleValue(oo.getDonationValue()))
				.isEquals();
	}

	@Override
	protected int requiredHashCode() {
		return new HashCodeBuilder().append(nullSafeGetId(getDonationSummary()))
				.append(nullSafeGetId(getDonGenPostFund())).append(nullSafeGetDoubleValue(getDonationValue()))
				.toHashCode();
	}

	@Override
	public int compareTo(DonationDetail o) {
		if (equals(o))
			return 0;
		return new CompareToBuilder().append(getDonGenPostFund(), o.getDonGenPostFund())
				.append(nullSafeGetDoubleValue(getDonationValue()), nullSafeGetDoubleValue(o.getDonationValue()))
				.toComparison() > 0 ? 1 : -1;
	}

	// -------------------------------------- Accessor Methods

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "DonationSummaryFK")
	@JsonIgnore
	@NotNull
	public DonationSummary getDonationSummary() {
		return donationSummary;
	}

	public void setDonationSummary(DonationSummary donationSummary) {
		this.donationSummary = donationSummary;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "GeneralPostFundFK")
	@JsonView({ DonationDetailView.Extended.class, DonationSummaryView.Search.class, ExpenditureView.Search.class })
	public DonGenPostFund getDonGenPostFund() {
		return donGenPostFund;
	}

	public void setDonGenPostFund(DonGenPostFund donGenPostFund) {
		this.donGenPostFund = donGenPostFund;
	}

	@Column(name = "DonationValue")
	@JsonView({ DonationDetailView.Basic.class, DonationSummaryView.Search.class, ExpenditureView.Search.class })
	public BigDecimal getDonationValue() {
		return donationValue;
	}

	public void setDonationValue(BigDecimal donationValue) {
		this.donationValue = donationValue;
	}

}
