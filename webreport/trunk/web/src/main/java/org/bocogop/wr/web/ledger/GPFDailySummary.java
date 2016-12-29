package org.bocogop.wr.web.ledger;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.PUBLIC_ONLY;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

import org.bocogop.wr.model.donation.DonGenPostFund;

@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = PUBLIC_ONLY, isGetterVisibility = PUBLIC_ONLY)
public class GPFDailySummary {

	private LocalDate date;
	private DonGenPostFund gpf;
	private BigDecimal donationTotal;
	private BigDecimal expenditureTotal;
	private BigDecimal ledgerAdjustmentTotal;

	public GPFDailySummary(LocalDate date, DonGenPostFund gpf, BigDecimal donationTotal, BigDecimal expenditureTotal,
			BigDecimal ledgerAdjustmentTotal) {
		this.date = date;
		this.gpf = gpf;
		this.donationTotal = donationTotal;
		this.expenditureTotal = expenditureTotal;
		this.ledgerAdjustmentTotal = ledgerAdjustmentTotal;
	}

	public BigDecimal getPeriodChange() {
		return donationTotal.subtract(expenditureTotal).add(ledgerAdjustmentTotal);
	}

	public LocalDate getDate() {
		return date;
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

}
