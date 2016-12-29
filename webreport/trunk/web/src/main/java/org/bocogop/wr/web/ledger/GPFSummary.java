package org.bocogop.wr.web.ledger;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.PUBLIC_ONLY;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

import org.bocogop.wr.model.donation.DonGenPostFund;

@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = PUBLIC_ONLY, isGetterVisibility = PUBLIC_ONLY)
public class GPFSummary {

	private DonGenPostFund gpf;
	private BigDecimal donationTotal;
	private BigDecimal expenditureTotal;
	private BigDecimal ledgerAdjustmentTotal;
	private BigDecimal grandDonationTotal;
	private BigDecimal grandExpenditureTotal;
	private BigDecimal grandLedgerAdjustmentTotal;

	public GPFSummary(DonGenPostFund gpf, BigDecimal[] sums) {
		this.gpf = gpf;
		this.donationTotal = sums == null || sums.length < 1 ? BigDecimal.ZERO : sums[0];
		this.expenditureTotal = sums == null || sums.length < 2 ? BigDecimal.ZERO : sums[1];
		this.ledgerAdjustmentTotal = sums == null || sums.length < 3 ? BigDecimal.ZERO : sums[2];
		this.grandDonationTotal = sums == null || sums.length < 4 ? BigDecimal.ZERO : sums[3];
		this.grandExpenditureTotal = sums == null || sums.length < 5 ? BigDecimal.ZERO : sums[4];
		this.grandLedgerAdjustmentTotal = sums == null || sums.length < 6 ? BigDecimal.ZERO : sums[5];
	}

	public BigDecimal getBalance() {
		return grandDonationTotal.subtract(grandExpenditureTotal).add(grandLedgerAdjustmentTotal);
	}

	public BigDecimal getPeriodChange() {
		return donationTotal.subtract(expenditureTotal).add(ledgerAdjustmentTotal);
	}

	public DonGenPostFund getGpf() {
		return gpf;
	}

	public BigDecimal getDonationTotal() {
		return donationTotal;
	}

	public BigDecimal getExpenditureTotal() {
		return expenditureTotal;
	}

	public BigDecimal getLedgerAdjustmentTotal() {
		return ledgerAdjustmentTotal;
	}

	public BigDecimal getGrandDonationTotal() {
		return grandDonationTotal;
	}

	public BigDecimal getGrandExpenditureTotal() {
		return grandExpenditureTotal;
	}

	public BigDecimal getGrandLedgerAdjustmentTotal() {
		return grandLedgerAdjustmentTotal;
	}

}
