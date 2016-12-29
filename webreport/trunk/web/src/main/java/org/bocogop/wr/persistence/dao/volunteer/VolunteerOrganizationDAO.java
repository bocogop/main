package org.bocogop.wr.persistence.dao.volunteer;

import java.util.Collection;
import java.util.List;

import org.bocogop.wr.model.volunteer.VolunteerOrganization;
import org.bocogop.wr.persistence.dao.CustomizableAppDAO;
import org.bocogop.wr.persistence.queryCustomization.QueryCustomization;

public interface VolunteerOrganizationDAO extends CustomizableAppDAO<VolunteerOrganization> {

	List<VolunteerOrganization> findByCriteria(Long volunteerId, Long organizationId, Boolean activeStatus,
			Long organizationFacilityId, QueryCustomization... customization);

	int bulkUpdateByCriteria(Collection<Long> orgOrBranchIds, Boolean currentActiveStatus, Boolean newActiveStatus);

	int bulkUpdatePrimaryOrganizationsByCriteria(Collection<Long> organizationIds, boolean setPrimaryOrganization,
			Long primaryOrganizationId);
	
	int inactivateForInactiveVolunteers();

}
