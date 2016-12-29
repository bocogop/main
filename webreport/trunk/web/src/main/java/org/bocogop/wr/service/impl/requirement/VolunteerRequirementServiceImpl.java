package org.bocogop.wr.service.impl.requirement;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import org.bocogop.shared.model.Permission;
import org.bocogop.wr.model.requirement.VolunteerRequirement;
import org.bocogop.wr.service.impl.AbstractServiceImpl;
import org.bocogop.wr.service.requirement.VolunteerRequirementService;

@Service
public class VolunteerRequirementServiceImpl extends AbstractServiceImpl implements VolunteerRequirementService {
	private static final Logger log = LoggerFactory.getLogger(VolunteerRequirementServiceImpl.class);

	private ExecutorService singleVolunteerExecutor = Executors.newCachedThreadPool();

	private BlockingQueue<Runnable> multiVolunteerQueue = new LinkedBlockingQueue<>();
	private ExecutorService multiVolunteerExecutor = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS,
			multiVolunteerQueue);

	@Autowired
	private VolunteerRequirementServiceHelper helper;

	@Override
	public VolunteerRequirement saveOrUpdate(VolunteerRequirement volunteer) {
		return volunteerRequirementDAO.saveOrUpdate(volunteer);
	}

	@Override
	public void delete(long volunteerRequirementId) {
		volunteerRequirementDAO.delete(volunteerRequirementId);
	}

	@Override
	@PreAuthorize("hasAuthority('" + Permission.VOLUNTEER_CREATE + "')")
	public Future<Integer> bulkAddNecessaryRequirementsLater(Long volunteerIdModified, Long requirementIdModified,
			Long benefitingServiceRoleIdModified, Long benefitingServiceRoleTemplateIdModified) {
		Callable<Integer> c = new Callable<Integer>() {
			@Override
			public Integer call() throws Exception {
				return helper.bulkAddNecessaryRequirements(volunteerIdModified, requirementIdModified,
						benefitingServiceRoleIdModified, benefitingServiceRoleTemplateIdModified);
			}
		};
		if (volunteerIdModified != null && multiVolunteerQueue.isEmpty()) {
			return singleVolunteerExecutor.submit(c);
		} else {
			return multiVolunteerExecutor.submit(c);
		}
	}

	@Override
	public int removeUnnecessaryVolunteerRequirementsInNewStatus() {
		multiVolunteerExecutor.submit(new Callable<Integer>() {
			@Override
			public Integer call() throws Exception {
				return volunteerRequirementDAO.removeUnnecessaryVolunteerRequirementsInNewStatus();
			}
		});
		return -1;
	}

	@Override
	public int updateAllIncorrectStatuses() {
		return volunteerRequirementDAO.updateAllIncorrectStatuses();
	}

}
