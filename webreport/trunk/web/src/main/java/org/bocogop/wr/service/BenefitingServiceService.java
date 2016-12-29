package org.bocogop.wr.service;

import java.util.List;
import java.util.Map;

import org.bocogop.shared.service.validation.ServiceValidationException;
import org.bocogop.wr.model.benefitingService.BenefitingService;
import org.bocogop.wr.model.benefitingService.BenefitingServiceRole;

public interface BenefitingServiceService {

	void saveAtLocationsOrUpdate(Long facilityId, List<Long> locationIds, Long benefitingServiceId, String name,
			String abbreviation, String subdivision, boolean active, boolean gamesRelated)
			throws ServiceValidationException;

	BenefitingService saveOrUpdate(BenefitingService benefitingService) throws ServiceValidationException;

	boolean canBeDeleted(long benefitingServiceId);

	/**
	 * @param facilityId
	 * @param locationIds
	 *            IDs of locations within the facility (-1 for main facility)
	 * @param newServices
	 * @param newRoles
	 * @return A Map of <Location ID, List<BenefitingServiceRole>> of newly added
	 *         items (key = -1 for main facility)
	 * @throws ServiceValidationException
	 */
	Map<Long, List<BenefitingServiceRole>> linkBenefitingServicesAndRoles(long facilityId, List<Long> locationIds,
			List<Long> newServices, List<Long> newRoles) throws ServiceValidationException;

	void reactivate(long benefitingServiceId) throws ServiceValidationException;

	void deleteOrInactivateBenefitingService(long benefitingServiceId);

	void deleteBenefitingService(long benefitingServiceId);

	void inactivateBenefitingService(long benefitingServiceId);
}
