package org.bocogop.wr.persistence.dao;

import java.util.List;

import org.bocogop.wr.model.donation.DonationLog;

public interface DonationLogDAO extends CustomizableSortedDAO<DonationLog> {

	List<DonationLog> findDonationLogByStationNumber(String stationNumber, boolean excludeProcessedItems);

	int countByCriteria(String stationNumber, boolean excludeProcessedItems);

}
