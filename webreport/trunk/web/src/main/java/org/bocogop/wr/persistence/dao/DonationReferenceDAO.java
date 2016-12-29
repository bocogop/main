package org.bocogop.wr.persistence.dao;

import java.util.List;

import org.bocogop.wr.model.donation.DonationReference;

public interface DonationReferenceDAO extends CustomizableSortedDAO<DonationReference> {

	public DonationReference findByInstitutionAndRef(String stationNum, String reference);
	public List<DonationReference> findDonReferenceByFacilityId(long facilityId);

}
