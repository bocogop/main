package org.bocogop.wr.service;

import java.util.List;

import org.bocogop.shared.service.validation.ServiceValidationException;
import org.bocogop.wr.model.benefitingService.BenefitingServiceRole;
import org.bocogop.wr.model.benefitingService.BenefitingServiceRoleType;

public interface BenefitingServiceRoleService {

	BenefitingServiceRole saveOrUpdate(BenefitingServiceRole benefitingServiceRole) throws ServiceValidationException;

	void saveOrUpdateAtLocations(List<Long> locationIds, Long benefitingServiceRoleId, Long benefitingServiceId,
			String name, String description, String contactName, String contactEmail, String contactPhone,
			BenefitingServiceRoleType roleType, boolean isEdit) throws ServiceValidationException;

	boolean canBeDeleted(long benefitingServiceRoleId);

	void merge(long fromBenefitingServiceRoleId, long toBenefitingServiceRoleId, boolean throwExceptionUponMergeFailure, boolean moveLocalSiblingsIfNecessary) throws ServiceValidationException;

	void reactivate(long benefitingServiceRoleId) throws ServiceValidationException;

	void deleteBenefitingServiceRole(long benefitingServiceRoleId);

	void inactivateBenefitingServiceRole(long benefitingServiceRoleId) throws ServiceValidationException;

	void deleteOrInactivateBenefitingServiceRole(long benefitingServiceRoleId) throws ServiceValidationException;

}
