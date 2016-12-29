package org.bocogop.wr.persistence.dao;

import java.time.LocalDate;
import java.util.List;
import java.util.SortedSet;

import org.bocogop.wr.model.donation.DonationLogFile;

public interface DonationLogFileDAO extends CustomizableSortedDAO<DonationLogFile> {

	DonationLogFile getByDate(LocalDate date);

	SortedSet<LocalDate> getExistingDatesOnOrAfter(LocalDate onOrAfterDate);
	
}
