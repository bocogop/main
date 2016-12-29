package org.bocogop.wr.service;

import java.util.Collections;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.bocogop.shared.service.validation.ServiceValidationException;
import org.bocogop.wr.model.benefitingService.BenefitingServiceRoleTemplate;
import org.bocogop.wr.model.facility.AbstractUpdateableLocation;

public interface BenefitingServiceRoleTemplateService {

	static class MergeErrorReport {
		private SortedMap<AbstractUpdateableLocation<?>, Exception> locationMergeErrors = Collections
				.synchronizedSortedMap(new TreeMap<>());
		private Exception executionException;

		public Map<AbstractUpdateableLocation<?>, Exception> getLocationMergeErrors() {
			return locationMergeErrors;
		}

		public void setExecutionException(Exception executionException) {
			this.executionException = executionException;
		}

		public Exception getExecutionException() {
			return executionException;
		}

		public boolean hasErrors() {
			return !locationMergeErrors.isEmpty() || executionException != null;
		}

		public Exception getException() {
			if (!locationMergeErrors.isEmpty()) {
				AbstractUpdateableLocation<?> loc = locationMergeErrors.firstKey();
				Exception e = locationMergeErrors.get(loc);
				return new ServiceValidationException("benefitingServiceRoleTemplate.error.mergeIncompleteAtFacility",
						loc.getDisplayName()).withCause(e);
			} else if (executionException != null) {
				return new ServiceValidationException("benefitingServiceRoleTemplate.error.mergeDeletionError")
						.withCause(executionException);
			}
			return null;
		}

	}

	BenefitingServiceRoleTemplate saveOrUpdate(BenefitingServiceRoleTemplate benefitingServiceRoleTemplate)
			throws ServiceValidationException;

	boolean canBeDeleted(long benefitingServiceRoleTemplateId);

	MergeErrorReport merge(long fromBenefitingServiceRoleTemplateId, long toBenefitingServiceRoleTemplateId)
			throws ServiceValidationException;

	void reactivate(long benefitingServiceRoleTemplateId) throws ServiceValidationException;

	void deleteBenefitingServiceRoleTemplate(long benefitingServiceRoleTemplateId);

	void inactivateBenefitingServiceRoleTemplate(long benefitingServiceRoleTemplateId)
			throws ServiceValidationException;

	void deleteOrInactivateBenefitingServiceRoleTemplate(long benefitingServiceRoleTemplateId)
			throws ServiceValidationException;
}
