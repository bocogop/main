package org.bocogop.wr.persistence.dao.benefitingService;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.PUBLIC_ONLY;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

import org.bocogop.wr.model.benefitingService.BenefitingServiceRoleTemplate;
import org.bocogop.wr.model.benefitingService.BenefitingServiceTemplate;
import org.bocogop.wr.persistence.dao.CustomizableSortedDAO;
import org.bocogop.wr.persistence.queryCustomization.QueryCustomization;

public interface BenefitingServiceTemplateDAO extends CustomizableSortedDAO<BenefitingServiceTemplate> {

	List<BenefitingServiceTemplate> findByCriteria(String name, Boolean activeStatus, Boolean gamesRelated, Boolean includeInactive,
			QueryCustomization... customization);

	Map<Long, Integer[]> countVolunteersForBenefitingServiceTemplateIds(Collection<Long> benefitingServiceIds);

	SortedSet<BenefitingServiceAndRoleTemplates> getAssignableBenefitingServiceAndRoleTemplates(long facilityId,
			Long benefitingServiceId, boolean unusedOnly, boolean skipRequiredAndReadOnlyRoles,
			QueryCustomization... customization);

	Map<Long, Integer> countOccasionalHoursForBenefitingServiceTemplateIds(
			Collection<Long> benefitingServiceTemplateIds);

	Map<Long, Integer> countOccasionalHoursForBenefitingServiceRoleTemplateIds(Collection<Long> allRoleIds);

	@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = PUBLIC_ONLY, isGetterVisibility = PUBLIC_ONLY)
	public static class BenefitingServiceAndRoleTemplates implements Comparable<BenefitingServiceAndRoleTemplates> {

		private BenefitingServiceTemplate serviceTemplate;
		private SortedSet<BenefitingServiceRoleTemplate> serviceRoleTemplates = new TreeSet<>();

		public BenefitingServiceAndRoleTemplates(BenefitingServiceTemplate serviceTemplate) {
			this.serviceTemplate = serviceTemplate;
		}

		@Override
		public int compareTo(BenefitingServiceAndRoleTemplates o) {
			return getServiceTemplate().compareTo(o.getServiceTemplate());
		}

		public BenefitingServiceTemplate getServiceTemplate() {
			return serviceTemplate;
		}

		public SortedSet<BenefitingServiceRoleTemplate> getServiceRoleTemplates() {
			return serviceRoleTemplates;
		}

	}

}