package org.bocogop.wr.persistence.impl;

import java.time.LocalDate;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import org.bocogop.wr.model.donation.DonationLogFile;
import org.bocogop.wr.persistence.dao.DonationLogFileDAO;

@Repository
public class DonationLogFileDAOImpl extends GenericHibernateSortedDAOImpl<DonationLogFile>
		implements DonationLogFileDAO {
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(DonationLogFileDAOImpl.class);

	@Override
	public DonationLogFile getByDate(LocalDate date) {
		@SuppressWarnings("unchecked")
		List<DonationLogFile> results = em
				.createQuery("from " + DonationLogFile.class.getName() + " where fileDate = :fileDate")
				.setParameter("fileDate", date).getResultList();
		return results.isEmpty() ? null : results.get(0);
	}

	@Override
	public SortedSet<LocalDate> getExistingDatesOnOrAfter(LocalDate d) {
		@SuppressWarnings("unchecked")
		List<LocalDate> results = em.createQuery("select fileDate from " + DonationLogFile.class.getName()
				+ " where fileDate >= :onOrAfterDate order by fileDate").setParameter("onOrAfterDate", d)
				.getResultList();
		return new TreeSet<>(results);
	}

}
