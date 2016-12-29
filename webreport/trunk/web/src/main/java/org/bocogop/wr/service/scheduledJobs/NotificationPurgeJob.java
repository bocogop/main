package org.bocogop.wr.service.scheduledJobs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@ManagedResource
public class NotificationPurgeJob extends AbstractScheduledJob {
	private static final Logger log = LoggerFactory.getLogger(NotificationPurgeJob.class);

	@Scheduled(cron = "${scheduledJobs.notificationPurge.cron}")
	@ManagedOperation
	public void purgeExpiredNotifications() {
		log.info(Thread.currentThread().toString() + ": Running scheduled job {}",
				NotificationPurgeJob.class.getSimpleName());
		int numPurged = notificationService.purgeExpiredNotifications();
		log.info("{} notifications purged", numPurged);
	}
}
