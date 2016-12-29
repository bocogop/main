package org.bocogop.wr.service;

import org.bocogop.shared.service.validation.ServiceValidationException;
import org.bocogop.wr.model.benefitingService.BenefitingServiceRoleType;
import org.bocogop.wr.model.requirement.AbstractRequirement;
import org.bocogop.wr.model.requirement.RequirementApplicationType;

public interface RequirementService {

	/**
	 * @param requirement
	 *            The Requirement to save or update
	 * @return The updated requirement after it's been merged
	 * @throws ServiceValidationException
	 */
	AbstractRequirement saveOrUpdate(AbstractRequirement requirement) throws ServiceValidationException;

	/**
	 * Deletes the Parking Sticker with the specified parkingStickerId
	 * 
	 * @param requirementId
	 *            The ID of the requirement to delete
	 */
	void delete(long requirementId);

	void changeType(long requirementId, RequirementApplicationType requirementChangeNewType,
			BenefitingServiceRoleType requirementChangeNewRoleType);

	void inactivateRequirement(long id);

	void reactivateRequirement(long id);

}
