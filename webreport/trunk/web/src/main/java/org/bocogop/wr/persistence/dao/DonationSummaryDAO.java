package org.bocogop.wr.persistence.dao;

import java.time.LocalDate;
import java.util.Collection;
import java.util.SortedSet;

import org.bocogop.wr.model.donation.DonationSummary;
import org.bocogop.wr.model.donation.DonorType;
import org.bocogop.wr.persistence.queryCustomization.QueryCustomization;

public interface DonationSummaryDAO extends CustomizableSortedDAO<DonationSummary> {

	/**
	 * 
	 * @param facilityId TODO
	 * @param donorName
	 *            TODO
	 * @param donationId TODO
	 * @param donationsOnOrAfter
	 *            TODO
	 * @param donationsOnOrBefore
	 *            TODO
	 * @param donorTypes
	 *            TODO
	 * @param includeAcknowledged
	 *            TODO
	 * @param includeUnacknowledged TODO
	 * @param customization
	 * @return
	 */
	SortedSet<DonationSummary> findByCriteria(Long facilityId, String donorName,
			Long donationId, LocalDate donationsOnOrAfter, LocalDate donationsOnOrBefore,
			Collection<DonorType> donorTypes, boolean includeAcknowledged, boolean includeUnacknowledged, QueryCustomization... customization);

	DonationSummary findByEpayTrackingID(String epayTrackingID);
}
