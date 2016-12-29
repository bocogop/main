package org.bocogop.wr.service.scheduledJobs;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import javax.batch.operations.JobExecutionIsRunningException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@ManagedResource
public class VolunteerAutoInactivationJob extends AbstractScheduledJob {
	private static final Logger log = LoggerFactory.getLogger(VolunteerAutoInactivationJob.class);

	private static boolean autoInactIsRunning = false;

	@Value("${scheduledJobs.volunteerAutoInactivation.switchToTimeZone}")
	private ZoneId switchToTimeZone;

	public String inactivateStaleVolunteersWithStatus() {
		try {
			inactivateStaleVolunteers();
			return "Completed";
		} catch (JobExecutionIsRunningException e) {
			return "Already Running as of " + ZonedDateTime.now() + "...";
		}
	}

	@Scheduled(cron = "${scheduledJobs.volunteerAutoInactivation.cron}")
	@ManagedOperation
	public void inactivateStaleVolunteers() {
		if (autoInactIsRunning == true) {
			throw new JobExecutionIsRunningException();
		}

		try {
			autoInactIsRunning = true;
			log.info(Thread.currentThread().toString() + ": Running scheduled job {}",
					VolunteerAutoInactivationJob.class.getSimpleName());
			int numInactivated = volunteerService.inactivateStaleVolunteers(switchToTimeZone);
			log.info("{} volunteers inactivated", numInactivated);
		} finally {
			autoInactIsRunning = false;
		}
	}
}
