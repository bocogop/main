package org.bocogop.wr.persistence.dao.requirement;

import java.util.List;

import org.bocogop.wr.model.requirement.AbstractVolunteerRequirement;
import org.bocogop.wr.model.requirement.VolunteerRequirement;
import org.bocogop.wr.persistence.dao.CustomizableAppDAO;
import org.bocogop.wr.persistence.queryCustomization.QueryCustomization;

public interface VolunteerRequirementDAO extends CustomizableAppDAO<VolunteerRequirement> {

	int bulkAddNecessaryRequirements(Long volunteerIdCreatedOrUpdated, Long requirementIdCreatedOrUpdated,
			Long benefitingServiceRoleIdModified, Long benefitingServiceRoleTemplateIdModified);

	/**
	 * List all requirements for volunteer including national as well as as
	 * local facility for localFacilityId
	 * 
	 * @param localFacilityId
	 * @return
	 */
	<T extends AbstractVolunteerRequirement> List<T> findByCriteria(Class<T> clazz, long volunteerId, Long facilityId,
			QueryCustomization... customization);

	int bulkUpdateDateToNull(long requirementId);

	int bulkUpdateInvalidStatusesToNew(long requirementId);

	Integer removeUnnecessaryVolunteerRequirementsInNewStatus();

	List<AbstractVolunteerRequirement> findUnmetRequirements(long volunteerId, long facilityId);

	int countByCriteria(long requirementId);

	int deleteByCriteria(long requirementId);

	int updateAllIncorrectStatuses();

	List<VolunteerRequirement> findForExpiringRequirementsByFacility(long facilityId, int maxResults);

}
