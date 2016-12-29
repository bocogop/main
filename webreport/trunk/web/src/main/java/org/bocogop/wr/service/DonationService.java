package org.bocogop.wr.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import org.bocogop.shared.service.validation.ServiceValidationException;
import org.bocogop.wr.model.donation.DonationDetail;
import org.bocogop.wr.model.donation.DonationLog;
import org.bocogop.wr.model.donation.DonationSummary;

public interface DonationService {

	/**
	 * @param donationDetail
	 *            The donationDetail to save or update
	 * @return The updated donationDetail after it's been persisted / updated
	 * @throws ServiceValidationException
	 *             If a business-level validation exception occurred during the
	 *             save/update process
	 */
	DonationDetail saveOrUpdateDonationDetail(DonationDetail donationDetail) throws ServiceValidationException;

	/**
	 * @param donationSummary
	 *            The donationSummary to save or update
	 * @return The updated donationSummary after it's been persisted / updated
	 * @throws ServiceValidationException
	 *             If a business-level validation exception occurred during the
	 *             save/update process
	 */
	DonationSummary saveOrUpdateDonationSummary(DonationSummary donationSummay,
			boolean runExtraCleanupForDonationDetails, DonationDetail donationDetail1, DonationDetail donationDetail2,
			DonationDetail donationDetail3, DonationDetail donationDetail4) throws ServiceValidationException;

	/**
	 * Deletes the DonationDetail with the specified volunteerId
	 * 
	 * @param donationDetailId
	 *            The ID of the donation detail to delete
	 */
	void deleteDonationDetail(long donationDetailId);

	/**
	 * Deletes the DonationDetail with the specified volunteerId
	 * 
	 * @param donationSummayId
	 *            The ID of the donation detail to delete
	 */
	void deleteDonationSummary(long donationSummaryId);

}
