package org.bocogop.wr.persistence.dao;

import java.util.List;

import org.bocogop.wr.model.donation.DonationDetail;

public interface DonationDetailDAO extends CustomizableSortedDAO<DonationDetail> {

	List<DonationDetail> findByDonationSummaryId(long donationSummaryId,boolean includeNullDonGenPostFund);

}
