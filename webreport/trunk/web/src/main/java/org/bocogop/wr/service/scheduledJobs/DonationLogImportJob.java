package org.bocogop.wr.service.scheduledJobs;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.xml.sax.SAXException;

import org.bocogop.wr.model.donation.DonationLog;
import org.bocogop.wr.util.DateUtil;

@Service
@ManagedResource
public class DonationLogImportJob extends AbstractScheduledJob {
	private static final Logger log = LoggerFactory.getLogger(DonationLogImportJob.class);

	@Scheduled(cron = "${scheduledJobs.donationLogImport.cron}")
	@ManagedOperation
	public void importNewDonations() {
		log.info(Thread.currentThread().toString() + ": Running scheduled job {}",
				DonationLogImportJob.class.getSimpleName());
		try {
			Map<LocalDate, List<DonationLog>> newItems = donationLogService.updateExternalDonations(null);
			if (log.isInfoEnabled()) {
				log.info("Donation log import report:");
				for (Entry<LocalDate, List<DonationLog>> entry : newItems.entrySet()) {
					log.info("\t{}: {} donations imported", entry.getKey().format(DateUtil.DATE_ONLY_FORMAT),
							entry.getValue().size());
				}
			}
		} catch (IOException | ParserConfigurationException | SAXException e) {
			log.error("There was an error attempting to update external donations:", e);
		}
	}
}
