package org.bocogop.wr.persistence.dao.volunteer;

import java.time.ZonedDateTime;
import java.util.List;

import org.bocogop.wr.model.volunteer.VolunteerAssignment;
import org.bocogop.wr.persistence.dao.CustomizableAppDAO;
import org.bocogop.wr.persistence.queryCustomization.QueryCustomization;

public interface VolunteerAssignmentDAO extends CustomizableAppDAO<VolunteerAssignment> {

	/**
	 * 
	 * @param activeStatus
	 *            TODO
	 * @param benefitingServiceRoleId
	 *            TODO
	 * @param facilityOrLocationId
	 *            TODO
	 * @param facilityOrParentFacilityId
	 *            TODO
	 * @return The list of Volunteers matching the above criteria
	 */
	List<VolunteerAssignment> findByCriteria(Long volunteerId, Boolean activeStatus, Long benefitingServiceRoleId,
			Long facilityOrLocationId, Long facilityOrParentFacilityId, QueryCustomization... customization);

	int bulkInactivateByCriteria(Long benefitingServiceTemplateId, Long benefitingServiceRoleTemplateId,
			Long benefitingServiceId, Long benefitingServiceRoleId, Long locationId);

	int bulkDeleteByCriteria(Long benefitingServiceTemplateId, Long benefitingServiceRoleTemplateId,
			Long benefitingServiceId, Long benefitingServiceRoleId);

	int bulkChangeForBenefitingServiceRoleMerge(long fromBenefitingServiceRoleId, long toBenefitingServiceRoleId);

	int[] countByCriteria(Long locationId);

	int bulkDeleteDuplicatesAfterChange(long fromBenefitingServiceRoleId, long toBenefitingServiceRoleId);

	int inactivateStaleAssignments(ZonedDateTime cutoffDate, ZonedDateTime gracePeriodCutoff);

	int bulkUpdateBenefitingServiceForRoleMove(long benefitingServiceRoleId);

}
