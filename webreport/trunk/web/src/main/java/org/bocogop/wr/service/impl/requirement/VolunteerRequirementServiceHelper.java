package org.bocogop.wr.service.impl.requirement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import org.bocogop.wr.service.impl.AbstractServiceImpl;

@Service
public class VolunteerRequirementServiceHelper extends AbstractServiceImpl {
	private static final Logger log = LoggerFactory.getLogger(VolunteerRequirementServiceHelper.class);

	public int bulkAddNecessaryRequirements(Long volunteerIdModified, Long requirementIdModified,
			Long benefitingServiceRoleIdModified, Long benefitingServiceRoleTemplateIdModified) {
		return volunteerRequirementDAO.bulkAddNecessaryRequirements(volunteerIdModified, requirementIdModified,
				benefitingServiceRoleIdModified, benefitingServiceRoleTemplateIdModified);
	}

}
