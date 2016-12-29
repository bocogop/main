package org.bocogop.wr.service.requirement;

import java.util.concurrent.Future;

import org.bocogop.wr.model.requirement.VolunteerRequirement;

public interface VolunteerRequirementService {

	VolunteerRequirement saveOrUpdate(VolunteerRequirement volunteer);

	void delete(long volunteerRequirementId);

	Future<Integer> bulkAddNecessaryRequirementsLater(Long volunteerIdModified, Long requirementIdModified,
			Long benefitingServiceRoleIdModified, Long benefitingServiceRoleTemplateIdModified);

	int removeUnnecessaryVolunteerRequirementsInNewStatus();

	int updateAllIncorrectStatuses();

}
