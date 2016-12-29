package org.bocogop.wr.persistence.dao;

import java.time.LocalDate;
import java.util.List;

import org.bocogop.wr.model.time.OccasionalWorkEntry;
import org.bocogop.wr.persistence.queryCustomization.QueryCustomization;

public interface OccasionalWorkEntryDAO extends CustomizableSortedDAO<OccasionalWorkEntry> {

	List<OccasionalWorkEntry> findByCriteria(LocalDate onOrAfterDate, LocalDate onOrBeforeDate, Long organizationId,
			Long benefitingServiceId, Long benefitingServiceRoleId, Long facilityId, QueryCustomization... customization);

	boolean existsForCriteria(Long benefitingServiceTemplateId, Long benefitingServiceRoleTemplateId, Long benefitingServiceId, Long benefitingServiceRoleId);

	int bulkMove(long fromBenefitingServiceRoleId, long toBenefitingServiceRoleId);

	int bulkUpdateBenefitingServiceForRoleMove(long benefitingServiceRoleId);

}
