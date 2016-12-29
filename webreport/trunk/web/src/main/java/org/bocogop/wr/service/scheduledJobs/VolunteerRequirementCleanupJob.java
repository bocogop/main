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
public class VolunteerRequirementCleanupJob extends AbstractScheduledJob {
	private static final Logger log = LoggerFactory.getLogger(VolunteerRequirementCleanupJob.class);

	@Scheduled(cron = "${scheduledJobs.volunteerRequirementCleanup.cron}")
	@ManagedOperation
	public void cleanupUnusedRequirementsStillInNewStatus() {
		log.info(Thread.currentThread().toString() + ": Running scheduled job {}",
				VolunteerRequirementCleanupJob.class.getSimpleName());
		int numInactivated = volunteerRequirementService.removeUnnecessaryVolunteerRequirementsInNewStatus();
		log.info("{} volunteer requirements purged", numInactivated);
	}
}
