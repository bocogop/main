package org.bocogop.wr.web.donation;

import java.time.LocalDate;

import org.bocogop.shared.util.StringUtil;
import org.bocogop.wr.model.donation.DonationDetail;
import org.bocogop.wr.model.donation.DonationReference;
import org.bocogop.wr.model.donation.DonationSummary;
import org.bocogop.wr.model.donation.Donor;
import org.bocogop.wr.model.organization.AbstractBasicOrganization;
import org.bocogop.wr.model.organization.ScopeType;

public class DonationCommand {

	// -------------------------------- Fields

	private DonationSummary donationSummary;
	private Donor donor;
	private boolean isVolunteer = false;
	private String displayName;
	private DonationDetail donationDetail1;
	private DonationDetail donationDetail2;
	private DonationDetail donationDetail3;
	private DonationDetail donationDetail4;
	private Long organizationId;
	private LocalDate currentFiscalYearStartDate;
	private LocalDate currentFiscalYearEndDate;
	private boolean ackAddressFilled = false;

	private boolean printReceipt;
	private boolean printMemo;
	private boolean printThankYou;
	private String printFormat;
	private String displayedFacility;
	private DonationReference previousDonRef;
	private String fromPage;

	// -------------------------------- Constructors

	public DonationCommand() {
	}

	public DonationCommand(DonationSummary donationSummary) {
		this.donationSummary = donationSummary;
		this.donor = donationSummary.getDonor();
		this.organizationId = donationSummary.getOrganization() != null? donationSummary.getOrganization().getId(): null;
		this.ackAddressFilled = hasAckAddressField(donationSummary);
		AbstractBasicOrganization org = this.donor.getOrganization();
		
		if (this.previousDonRef == null)
			this.previousDonRef = donationSummary.getDonReference();
		// org is branch
		if (org != null && "Branch".equalsIgnoreCase(org.getScale())) {
			this.displayedFacility = org.getRootOrganization().getScope() != ScopeType.NATIONAL
					? org.getRootOrganization().getFacility().getDisplayName() : "NATIONAL";
		} else if (org != null) {
			// org is organization
			this.displayedFacility = org.getScope() != ScopeType.NATIONAL ? org.getFacility().getDisplayName()
					: "NATIONAL";
		}
	}

	// -------------------------------- Business Methods

	private boolean hasAckAddressField(DonationSummary donationSummary) {
		if (donationSummary.getAckOverrideAddress1() != null || donationSummary.getAckOverrideAddress2() != null
				|| donationSummary.getAckOverrideCity() != null || donationSummary.getAckOverrideState() != null
				|| donationSummary.getAckOverrideZip() != null) {
			return true;
		}
		return false;
	}

	// -------------------------------- Accessor Methods

	public DonationSummary getDonationSummary() {
		return donationSummary;
	}

	public void setDonationSummary(DonationSummary donationSummary) {
		this.donationSummary = donationSummary;
	}

	public Donor getDonor() {
		return donor;
	}

	public void setDonor(Donor donor) {
		this.donor = donor;
	}

	public boolean isVolunteer() {
		if (donor.getVolunteer() != null)
			isVolunteer = true;
		return isVolunteer;
	}

	public void setVolunteer(boolean isVolunteer) {
		this.isVolunteer = isVolunteer;
	}

	public String getDisplayName() {
		displayName = StringUtil.getDisplayName(true, donor.getFirstName(), donor.getMiddleName(), donor.getLastName(),
				donor.getSuffix());
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public DonationDetail getDonationDetail1() {
		return donationDetail1;
	}

	public void setDonationDetail1(DonationDetail donationDetail1) {
		this.donationDetail1 = donationDetail1;
	}

	public DonationDetail getDonationDetail2() {
		return donationDetail2;
	}

	public void setDonationDetail2(DonationDetail donationDetail2) {
		this.donationDetail2 = donationDetail2;
	}

	public DonationDetail getDonationDetail3() {
		return donationDetail3;
	}

	public void setDonationDetail3(DonationDetail donationDetail3) {
		this.donationDetail3 = donationDetail3;
	}

	public LocalDate getCurrentFiscalYearStartDate() {
		return currentFiscalYearStartDate;
	}

	public void setCurrentFiscalYearStartDate(LocalDate currentFiscalYearStartDate) {
		this.currentFiscalYearStartDate = currentFiscalYearStartDate;
	}

	public LocalDate getCurrentFiscalYearEndDate() {
		return currentFiscalYearEndDate;
	}

	public void setCurrentFiscalYearEndDate(LocalDate currentFiscalYearEndDate) {
		this.currentFiscalYearEndDate = currentFiscalYearEndDate;
	}

	public DonationDetail getDonationDetail4() {
		return donationDetail4;
	}

	public void setDonationDetail4(DonationDetail donationDetail4) {
		this.donationDetail4 = donationDetail4;
	}

	public boolean isAckAddressFilled() {
		return ackAddressFilled;
	}

	public void setAckAddressFilled(boolean ackAddressFilled) {
		this.ackAddressFilled = ackAddressFilled;
	}

	public boolean isPrintReceipt() {
		return printReceipt;
	}

	public void setPrintReceipt(boolean printReceipt) {
		this.printReceipt = printReceipt;
	}

	public boolean isPrintMemo() {
		return printMemo;
	}

	public void setPrintMemo(boolean printMemo) {
		this.printMemo = printMemo;
	}

	public boolean isPrintThankYou() {
		return printThankYou;
	}

	public void setPrintThankYou(boolean printThankYou) {
		this.printThankYou = printThankYou;
	}

	public String getPrintFormat() {
		return printFormat;
	}

	public void setPrintFormat(String printFormat) {
		this.printFormat = printFormat;
	}

	public Long getOrganizationId() {
		return organizationId;
	}

	public void setOrganizationId(Long organizationId) {
		this.organizationId = organizationId;
	}

	public String getDisplayedFacility() {
		return displayedFacility;
	}

	public void setDisplayedFacility(String displayedFacility) {
		this.displayedFacility = displayedFacility;
	}

	public DonationReference getPreviousDonRef() {
		return previousDonRef;
	}

	public void setPreviousDonRef(DonationReference previousDonRef) {
		this.previousDonRef = previousDonRef;
	}

	public String getFromPage() {
		return fromPage;
	}

	public void setFromPage(String fromPage) {
		this.fromPage = fromPage;
	}

}
