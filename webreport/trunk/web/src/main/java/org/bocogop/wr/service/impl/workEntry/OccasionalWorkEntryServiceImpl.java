package org.bocogop.wr.service.impl.workEntry;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import org.bocogop.shared.service.validation.ServiceValidationException;
import org.bocogop.wr.model.benefitingService.BenefitingServiceRole;
import org.bocogop.wr.model.organization.AbstractBasicOrganization;
import org.bocogop.wr.model.time.OccasionalWorkEntry;
import org.bocogop.wr.service.impl.AbstractServiceImpl;
import org.bocogop.wr.service.workEntry.OccasionalWorkEntryService;
import org.bocogop.wr.util.DateUtil;

@Service
public class OccasionalWorkEntryServiceImpl extends AbstractServiceImpl implements OccasionalWorkEntryService {
	private static final Logger log = LoggerFactory.getLogger(OccasionalWorkEntryServiceImpl.class);

	@Override
	public OccasionalWorkEntry saveOrUpdate(OccasionalWorkEntry workEntry, boolean requireActiveOrganizationAndRole)
			throws ServiceValidationException {
		if (workEntry.getDateWorked().isBefore(dateUtil.getEarliestAcceptableDateEntryAsOfNow(getFacilityTimeZone())))
			throw new ServiceValidationException("occasionalTimePost.error.dateTooEarly");
		if (workEntry.getDateWorked().isAfter(getTodayAtFacility()))
			throw new ServiceValidationException("occasionalTimePost.error.futureDateDisallowed");

		if (workEntry.getNumberInGroup() <= 0)
			throw new ServiceValidationException("occasionalTimePost.error.requiredPositiveNumberInGroup");
		if (workEntry.getHoursWorked() <= 0)
			throw new ServiceValidationException("occasionalTimePost.error.requiredPositiveHoursWorked");

		AbstractBasicOrganization o = workEntry.getOrganization();
		BenefitingServiceRole role = workEntry.getBenefitingServiceRole();
		List<OccasionalWorkEntry> duplicates = occasionalWorkEntryDAO.findByCriteria(workEntry.getDateWorked(),
				workEntry.getDateWorked(), o.getId(), null, role.getId(), getRequiredFacilityContext().getId());
		if (workEntry.isPersistent()) {
			duplicates = duplicates.stream().filter(p -> !p.getId().equals(workEntry.getId()))
					.collect(Collectors.toList());
		}
		if (!duplicates.isEmpty()) {
			OccasionalWorkEntry duplicate = duplicates.get(0);
			duplicate.getBenefitingService();
			throw new ServiceValidationException("occasionalTimePost.error.duplicateEntryDetected",
					new Serializable[] { duplicate.getOrganization().getDisplayName(), role.getDisplayName(true),
							workEntry.getDateWorked().format(DateUtil.DATE_ONLY_FORMAT), });
		}

		if (requireActiveOrganizationAndRole && o.isInactive()) {
			throw new ServiceValidationException("occasionalTimePost.error.orgInactive",
					new Serializable[] { o.getDisplayName() });
		}

		if (requireActiveOrganizationAndRole && role.isInactive()) {
			throw new ServiceValidationException("occasionalTimePost.error.roleInactive",
					new Serializable[] { role.getDisplayName(false) });
		}

		return occasionalWorkEntryDAO.saveOrUpdate(workEntry);
	}

	@Override
	public void saveOrUpdateMultiple(List<OccasionalWorkEntry> workEntries) throws ServiceValidationException {
		for (OccasionalWorkEntry workEntry : workEntries) {
			saveOrUpdate(workEntry, true);
		}
	}

	@Override
	public void delete(long workEntryId) {
		occasionalWorkEntryDAO.delete(workEntryId);
	}

}
