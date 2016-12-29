package org.bocogop.wr.service.scheduledJobs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@ManagedResource
public class VolunteerRequirementStatusSyncJob extends AbstractScheduledJob {
	private static final Logger log = LoggerFactory.getLogger(VolunteerRequirementStatusSyncJob.class);

	@Scheduled(cron = "${scheduledJobs.volunteerRequirementStatusSync.cron}")
	@ManagedOperation
	public void syncVolunteerRequirementStatuses() {
		log.info(Thread.currentThread().toString() + ": Running scheduled job {}",
				VolunteerRequirementStatusSyncJob.class.getSimpleName());
		int numUpdated = volunteerRequirementService.updateAllIncorrectStatuses();
		log.info("{} volunteer requirements updated", numUpdated);
	}
}
