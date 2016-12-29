package org.bocogop.wr.web.donation;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.springframework.format.annotation.DateTimeFormat;

import org.bocogop.wr.model.donation.DonationSummary;
import org.bocogop.wr.model.donation.DonorType;
import org.bocogop.wr.util.DateUtil;

public class DonationListCommand {

	// ----------------------------------- Fields

	private long facilityId;
	private String mode;

	@DateTimeFormat(pattern = DateUtil.TWO_DIGIT_DATE_ONLY)
	private LocalDate startDate;
	@DateTimeFormat(pattern = DateUtil.TWO_DIGIT_DATE_ONLY)
	private LocalDate endDate;
	private String donorName;
	private Long donationId;
	private List<DonorType> donorTypes;
	private boolean includeAcknowledged;
	private boolean includeUnacknowledged;

	private boolean searched = false;
	private SortedSet<DonationSummary> donations;

	// ----------------------------------- Constructors

	public DonationListCommand() {
	}

	public DonationListCommand(long facilityId, String mode, LocalDate startDate, LocalDate endDate, String donorName,
			Long donationId, List<DonorType> donorTypes, Boolean acknowledgementStatus) {
		this.facilityId = facilityId;
		this.mode = mode;
		this.startDate = startDate;
		this.endDate = endDate;
		this.donorName = donorName;
		this.donationId = donationId;
		this.donorTypes = donorTypes;
		this.includeAcknowledged = acknowledgementStatus == null || acknowledgementStatus;
		this.includeUnacknowledged = acknowledgementStatus == null || !acknowledgementStatus;
	}

	// ----------------------------------- Accessor Methods

	public LocalDate getStartDate() {
		return startDate;
	}

	public void setStartDate(LocalDate startDate) {
		this.startDate = startDate;
	}

	public Long getDonationId() {
		return donationId;
	}

	public void setDonationId(Long donationId) {
		this.donationId = donationId;
	}

	public long getFacilityId() {
		return facilityId;
	}

	public void setFacilityId(long facilityId) {
		this.facilityId = facilityId;
	}

	public boolean isSearched() {
		return searched;
	}

	public void setSearched(boolean searched) {
		this.searched = searched;
	}

	public LocalDate getEndDate() {
		return endDate;
	}

	public void setEndDate(LocalDate endDate) {
		this.endDate = endDate;
	}

	public String getDonorName() {
		return donorName;
	}

	public void setDonorName(String donorName) {
		this.donorName = donorName;
	}

	public List<DonorType> getDonorTypes() {
		if (donorTypes == null)
			donorTypes = new ArrayList<>();
		return donorTypes;
	}

	public void setDonorTypes(List<DonorType> donorTypes) {
		this.donorTypes = donorTypes;
	}

	public boolean isIncludeAcknowledged() {
		return includeAcknowledged;
	}

	public void setIncludeAcknowledged(boolean includeAcknowledged) {
		this.includeAcknowledged = includeAcknowledged;
	}

	public boolean isIncludeUnacknowledged() {
		return includeUnacknowledged;
	}

	public void setIncludeUnacknowledged(boolean includeUnacknowledged) {
		this.includeUnacknowledged = includeUnacknowledged;
	}

	public SortedSet<DonationSummary> getDonations() {
		if (donations == null)
			donations = new TreeSet<>();
		return donations;
	}

	public void setDonations(SortedSet<DonationSummary> donations) {
		this.donations = donations;
	}

	public String getMode() {
		return mode;
	}

	public void setMode(String mode) {
		this.mode = mode;
	}

}
