package org.bocogop.wr.persistence.dao;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.bocogop.wr.model.time.WorkEntry;
import org.bocogop.wr.persistence.queryCustomization.QueryCustomization;

public interface WorkEntryDAO extends CustomizableSortedDAO<WorkEntry> {

	List<WorkEntry> findByCriteria(Long volunteerId, Long volunteerAssignmentId, Long facilityId, Long organizationId,
			LocalDate date, LocalDate fiscalYearAsOfDate, QueryCustomization... customization);

	Map<Long, LocalDate> getMostRecentVolunteeredDateByFacilityMap(long volunteerId);

	int getNumYearsWorked(long volunteerId);

	boolean existsForCriteria(Long benefitingServiceTemplateId, Long benefitingServiceRoleTemplateId,
			Long benefitingServiceId, Long benefitingServiceRoleId);

	/*
	 * Normally we just update VolunteerAssignments that point to the "from"
	 * BenefitingServiceRole to point to the "to" BenefitingServiceRole. However
	 * some volunteers may already have an assignment linked to the "to" role.
	 * So we don't create a duplicate. But we need to move all hours linked to
	 * the old role over to the new one, before we can delete the old one -
	 * that's what this method does. CPB
	 */
	int bulkChangeForBenefitingServiceRoleMerge(long fromBenefitingServiceRoleId, long toBenefitingServiceRoleId);

	Map<Long, Integer> countByVolunteerAssignmentIds(List<Long> volunteerAssignmentIds);

	Map<Long, Double> countByVolunteerAndBasicOrganizations(long volunteerId, List<Long> basicOrganizationIds);

}
