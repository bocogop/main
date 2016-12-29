package org.bocogop.wr.service.scheduledJobs;

import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@ManagedResource
public class ExcludedEntityRefreshJob extends AbstractScheduledJob {
	private static final Logger log = LoggerFactory.getLogger(ExcludedEntityRefreshJob.class);

	@Value("${scheduledJobs.excludedEntityRefresh.enabled}")
	private boolean excludedEntityRefreshEnabled;
	@Value("${scheduledJobs.excludedEntityVolunteerUpdate.enabled}")
	private boolean excludedEntityVolunteerUpdateEnabled;

	@Scheduled(cron = "${scheduledJobs.excludedEntityRefresh.cron}")
	// or if it's on a delay, use this config:
	// @Scheduled(initialDelayString = "${scheduledJobs.startupDelayMillis}", //
	// fixedDelayString = "${scheduledJobs.cprsHeaderSync.fixedDelay}")
	@ManagedOperation
	public void refreshExcludedEntitiesAndUpdateVolunteers() {
		if (!excludedEntityRefreshEnabled) {
			log.debug(ExcludedEntityRefreshJob.class.getSimpleName() + " refresh not enabled; exiting");
			return;
		}

		log.info(Thread.currentThread().toString() + ": Running scheduled job {}",
				ExcludedEntityRefreshJob.class.getSimpleName() + " refresh");

		try {
			runAsBatchJobUser(new Callable<Object>() {
				@Override
				public Object call() throws Exception {
					excludedEntityService.refreshDataAndUpdateVolunteers();
					return null;
				}
			});
		} catch (Exception t) {
			log.error("Caught exception in " + ExcludedEntityRefreshJob.class.getName(), t);
		}
	}

	@Scheduled(cron = "${scheduledJobs.excludedEntityVolunteerUpdate.cron}")
	@ManagedOperation
	public void updateVolunteers() {
		if (!excludedEntityVolunteerUpdateEnabled) {
			log.debug(ExcludedEntityRefreshJob.class.getSimpleName() + " volunteer update not enabled; exiting");
			return;
		}

		log.info(Thread.currentThread().toString() + ": Running scheduled job {}",
				ExcludedEntityRefreshJob.class.getSimpleName() + " volunteer update");

		try {
			runAsBatchJobUser(new Callable<Object>() {
				@Override
				public Object call() throws Exception {
					excludedEntityService.updateVolunteers();
					return null;
				}
			});
		} catch (Exception t) {
			log.error("Caught exception in " + ExcludedEntityRefreshJob.class.getName(), t);
		}
	}

}
