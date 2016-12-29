package org.bocogop.wr.service.impl;

import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import org.bocogop.shared.service.validation.ServiceValidationException;
import org.bocogop.wr.model.donation.DonGenPostFund;
import org.bocogop.wr.model.donation.DonationDetail;
import org.bocogop.wr.model.donation.DonationSummary;
import org.bocogop.wr.model.facility.Facility;
import org.bocogop.wr.model.notification.NotificationType;
import org.bocogop.wr.service.DonationService;

@Service
public class DonationServiceImpl extends AbstractServiceImpl implements DonationService {
	private static final Logger log = LoggerFactory.getLogger(DonationServiceImpl.class);

	public DonationDetail saveOrUpdateDonationDetail(DonationDetail donationDetail) {
		return donationDetailDAO.saveOrUpdate(donationDetail);
	}

	public DonationSummary saveOrUpdateDonationSummary(DonationSummary donationSummary,
			boolean runExtraCleanupForDonationDetails, DonationDetail donationDetail1, DonationDetail donationDetail2,
			DonationDetail donationDetail3, DonationDetail donationDetail4) throws ServiceValidationException {
		boolean isEdit = donationSummary.isPersistent();

		if (StringUtils.isNotBlank(donationSummary.getEpayTrackingID())) {
			DonationSummary test = donationSummaryDAO.findByEpayTrackingID(donationSummary.getEpayTrackingID());
			if (test != null && (!isEdit || !donationSummary.getId().equals(test.getId())))
				throw new ServiceValidationException("donationSummary.error.duplicateEpayTrackingID");
		}

		donationSummary = donationSummaryDAO.saveOrUpdate(donationSummary);

		// Cleanup all donation details, then save the donation details if it is
		// from submitDonationSummary call
		// collected from donation detail page
		// if donation type = item/activity, cleanup all real gpfs
		if (runExtraCleanupForDonationDetails) {
			List<DonationDetail> donationDetails = donationSummary.getDonationDetails();
			String donationType = donationSummary.getDonationType().getDonationType();

			// remove all donation detail if DonGenPostFund is null or
			// DonGenPostFund is "None"
			for (Iterator<DonationDetail> it = donationDetails.iterator(); it.hasNext();) {
				DonationDetail donDetail = it.next();

				DonGenPostFund f = donDetail.getDonGenPostFund();
				if (f == null) {
					it.remove();
					continue;
				}

				if ("Item".equalsIgnoreCase(donationType) || "Activity".equalsIgnoreCase(donationType)) {
					if (!donDetail.getDonGenPostFund().getGeneralPostFund().equalsIgnoreCase("None"))
						it.remove();
				} else {
					if (f.getGeneralPostFund().equalsIgnoreCase("None"))
						it.remove();
				}
			}

			if (donationDetail1 != null && donationDetail1.getDonationValue() != null
					&& donationDetail1.getDonationValue().doubleValue() > 0) {
				donationDetail1.setDonationSummary(donationSummary);
				donationDetails.add(donationDetail1);
			}
			if (donationDetail2 != null && donationDetail2.getDonationValue() != null
					&& donationDetail2.getDonationValue().doubleValue() > 0) {
				donationDetail2.setDonationSummary(donationSummary);
				donationDetails.add(donationDetail2);
			}
			if (donationDetail3 != null && donationDetail3.getDonationValue() != null
					&& donationDetail3.getDonationValue().doubleValue() > 0) {
				donationDetail3.setDonationSummary(donationSummary);
				donationDetails.add(donationDetail3);
			}

			if ((donationType.equalsIgnoreCase("Item") || donationType.equalsIgnoreCase("Activity"))
					&& donationDetail4.getDonationValue() != null
					&& donationDetail4.getDonationValue().doubleValue() > 0) {
				donationDetail4.setDonationSummary(donationSummary);
				donationDetails.clear();
				donationDetails.add(donationDetail4);
			}

			donationSummary.setDonationDetails(donationDetails);

			donationSummary = donationSummaryDAO.saveOrUpdate(donationSummary);
		}

		if (StringUtils.isNotBlank(donationSummary.getEpayTrackingID())) {
			Facility facility = donationSummary.getFacility();
			int numOutstandingDonationLogs = donationLogDAO.countByCriteria(facility.getStationNumber(), true);
			if (numOutstandingDonationLogs == 0)
				notificationDAO.deleteByCriteria(facility.getId(), NotificationType.DONATION);
		}

		return donationSummary;
	}

	public void deleteDonationDetail(long donationDetailId) {
		donationDetailDAO.delete(donationDetailId);
	}

	public void deleteDonationSummary(long donationSummaryId) {
		donationSummaryDAO.delete(donationSummaryId);
	}

}
