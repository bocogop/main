package org.bocogop.wr.persistence.dao.requirement;

import java.util.List;

import org.bocogop.wr.model.requirement.AbstractRequirement;
import org.bocogop.wr.persistence.dao.CustomizableSortedDAO;
import org.bocogop.wr.persistence.queryCustomization.QueryCustomization;

public interface RequirementDAO extends CustomizableSortedDAO<AbstractRequirement> {
	List<AbstractRequirement> findByCriteria(Long facilityId, String name, QueryCustomization... customization);

	void changeType(long requirementId, String newTypeCode);

	void updateFieldsWithoutVersionIncrement(long requirementId, boolean setRoleType, Long roleTypeId);

}
