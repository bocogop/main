package org.bocogop.wr.model.expenditure;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.PUBLIC_ONLY;

import java.math.BigDecimal;
import java.time.LocalDate;

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

import org.bocogop.shared.model.AppUser;
import org.bocogop.shared.model.AppUser.AppUserView;
import org.bocogop.shared.model.core.AbstractAuditedVersionedPersistent;
import org.bocogop.wr.model.donation.DonGenPostFund;
import org.bocogop.wr.model.facility.Facility;

@Entity
@Table(name = "LedgerAdjustment", schema = "wr")
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = PUBLIC_ONLY, isGetterVisibility = PUBLIC_ONLY)
public class LedgerAdjustment extends AbstractAuditedVersionedPersistent<LedgerAdjustment>
		implements Comparable<LedgerAdjustment> {
	private static final long serialVersionUID = 2676191929993376944L;

	public static class LedgerAdjustmentView {
		public interface Basic {
		}

		public interface Search extends Basic, AppUserView.Basic {
		}

		public interface Extended extends Basic {
		}
	}

	// -------------------------------------- Fields

	private Facility facility;
	private DonGenPostFund donGenPostFund;
	private AppUser originator;
	private LocalDate requestDate;
	private BigDecimal amount;
	private String justification;

	// -------------------------------------- Business Methods

	// ---------------------------------------- Common Methods

	@Override
	public int compareTo(LedgerAdjustment oo) {
		if (equals(oo))
			return 0;

		return new CompareToBuilder() //
				.append(nullSafeGetId(getFacility()), nullSafeGetId(oo.getFacility())) //
				.append(nullSafeGetId(getDonGenPostFund()), nullSafeGetId(oo.getDonGenPostFund())) //
				.append(getRequestDate(), oo.getRequestDate()) //
				.append(nullSafeGetDoubleValue(getAmount()), nullSafeGetDoubleValue(oo.getAmount())) //
				.toComparison() > 0 ? 1 : -1;
	}

	@Override
	protected boolean requiredEquals(LedgerAdjustment oo) {
		// I don't see a more efficient way to do this since we don't have a
		// simpler business key - CPB
		return new EqualsBuilder() //
				.append(nullSafeGetId(getFacility()), nullSafeGetId(oo.getFacility())) //
				.append(getRequestDate(), oo.getRequestDate()) //
				.append(nullSafeGetDoubleValue(getAmount()), nullSafeGetDoubleValue(oo.getAmount())) //
				.append(nullSafeGetId(getDonGenPostFund()), nullSafeGetId(oo.getDonGenPostFund())) //
				.append(nullSafeGetId(getOriginator()), nullSafeGetId(oo.getOriginator())) //
				.append(getJustification(), oo.getJustification()).isEquals();
	}

	@Override
	protected int requiredHashCode() {
		return new HashCodeBuilder() //
				.append(nullSafeGetId(getFacility())) //
				.append(getRequestDate()) //
				.append(nullSafeGetDoubleValue(getAmount())) //
				.toHashCode();
	}

	// -------------------------------------- Accessor Methods

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "FacilityFK", nullable = false)
	@NotNull
	@JsonIgnore
	public Facility getFacility() {
		return facility;
	}

	public void setFacility(Facility facility) {
		this.facility = facility;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "GeneralPostFundFK", nullable = false)
	@NotNull
	public DonGenPostFund getDonGenPostFund() {
		return donGenPostFund;
	}

	public void setDonGenPostFund(DonGenPostFund donGenPostFund) {
		this.donGenPostFund = donGenPostFund;
	}

	@Column(nullable = false)
	@NotNull
	public LocalDate getRequestDate() {
		return requestDate;
	}

	public void setRequestDate(LocalDate requestDate) {
		this.requestDate = requestDate;
	}

	@Column(nullable = false)
	@NotNull
	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "AppUserFK", nullable = false)
	@NotNull
	@JsonView(LedgerAdjustmentView.Search.class)
	public AppUser getOriginator() {
		return originator;
	}

	public void setOriginator(AppUser originator) {
		this.originator = originator;
	}

	@Column(nullable = false)
	@NotNull
	public String getJustification() {
		return justification;
	}

	public void setJustification(String justification) {
		this.justification = justification;
	}

}
