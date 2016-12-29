package org.bocogop.wr.service.scheduledJobs;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.ldap.CommunicationException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import org.bocogop.shared.model.AppUser;

@Service
@ManagedResource
public class AppUserUpdateJob extends AbstractScheduledJob {
	private static final Logger log = LoggerFactory.getLogger(AppUserUpdateJob.class);

	@Scheduled(cron = "${scheduledJobs.appUserUpdate.cron}")
	@ManagedOperation
	public void updateAllAppUsers() {
		log.info("{}: Running scheduled job {}", Thread.currentThread().toString(),
				AppUserUpdateJob.class.getSimpleName());

		try {
			List<AppUser> allUsers = appUserDAO.findAll();
			for (AppUser appUser : allUsers) {
				try {
					log.debug("Updating app user {} from LDAP...", appUser.getId());
					appUserService.updateUserFromLDAP(appUser.getId(), false);
				} catch (Exception e) {
					if (e instanceof CommunicationException)
						throw e;
					log.error("Error updating user from LDAP, moving onto next user...", e);
				}
			}
		} catch (Throwable t) {
			log.error("Error updating users", t);
		}

		log.info("Finished scheduled job {}", AppUserUpdateJob.class.getSimpleName());
	}
}
