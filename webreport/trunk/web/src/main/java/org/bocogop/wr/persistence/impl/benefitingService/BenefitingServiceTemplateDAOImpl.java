package org.bocogop.wr.persistence.impl.benefitingService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.persistence.Query;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import org.bocogop.wr.model.benefitingService.BenefitingService;
import org.bocogop.wr.model.benefitingService.BenefitingServiceRole;
import org.bocogop.wr.model.benefitingService.BenefitingServiceRoleTemplate;
import org.bocogop.wr.model.benefitingService.BenefitingServiceTemplate;
import org.bocogop.wr.model.time.OccasionalWorkEntry;
import org.bocogop.wr.persistence.dao.benefitingService.BenefitingServiceTemplateDAO;
import org.bocogop.wr.persistence.impl.GenericHibernateSortedDAOImpl;
import org.bocogop.wr.persistence.queryCustomization.QueryCustomization;

@Repository
public class BenefitingServiceTemplateDAOImpl extends GenericHibernateSortedDAOImpl<BenefitingServiceTemplate>
		implements BenefitingServiceTemplateDAO {
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(BenefitingServiceTemplateDAOImpl.class);

	@SuppressWarnings("unchecked")
	@Override
	public SortedSet<BenefitingServiceAndRoleTemplates> getAssignableBenefitingServiceAndRoleTemplates(long facilityId,
			Long benefitingServiceId, boolean unusedOnly, boolean skipRequiredAndReadOnlyRoles,
			QueryCustomization... customization) {
		StringBuilder sb = new StringBuilder("select o, r from ");

		if (benefitingServiceId != null) {
			sb.append(BenefitingService.class.getName()).append(" bs join bs.template o");
		} else {
			sb.append(BenefitingServiceTemplate.class.getName()).append(" o");
		}

		sb.append(" join o.serviceRoleTemplates r");

		QueryCustomization cust = ArrayUtils.isEmpty(customization) ? new QueryCustomization() : customization[0];
		cust.appendRemainingJoins(sb, "o");

		if (cust.getOrderBy() == null)
			cust.setOrderBy("o.name");

		List<String> whereClauseItems = new ArrayList<>();
		Map<String, Object> params = new HashMap<>();

		boolean needFacilityParam = false;
		if (benefitingServiceId == null) {
			if (unusedOnly) {
				whereClauseItems.add("not exists (select x from " + BenefitingService.class.getName()
						+ " x where x.facility.id = :facilityId and x.template = o)");
				needFacilityParam = true;
			}
		} else {
			whereClauseItems.add("bs.id = :benefitingServiceId");
			params.put("benefitingServiceId", benefitingServiceId);
		}

		if (unusedOnly) {
			whereClauseItems.add("not exists (select y from " + BenefitingServiceRole.class.getName()
					+ " y where y.facility.id = :facilityId and y.template = r)");
			needFacilityParam = true;
		}

		if (needFacilityParam)
			params.put("facilityId", facilityId);

		whereClauseItems.add("o.inactive = false");
		whereClauseItems.add("r.inactive = false");

		if (skipRequiredAndReadOnlyRoles)
			whereClauseItems.add("r.requiredAndReadOnly = false");

		Query q = constructQuery(em, sb, whereClauseItems, params, null, cust);

		List<Object[]> queryResults = q.getResultList();
		Map<Long, BenefitingServiceAndRoleTemplates> results = new HashMap<>();
		for (Object[] r : queryResults) {
			BenefitingServiceTemplate template = (BenefitingServiceTemplate) r[0];
			BenefitingServiceRoleTemplate role = (BenefitingServiceRoleTemplate) r[1];

			BenefitingServiceAndRoleTemplates t = results.get(template.getId());
			if (t == null)
				results.put(template.getId(), t = new BenefitingServiceAndRoleTemplates(template));
			t.getServiceRoleTemplates().add(role);
		}
		return new TreeSet<>(results.values());
	}

	@Override
	public Map<Long, Integer[]> countVolunteersForBenefitingServiceTemplateIds(Collection<Long> benefitingServiceIds) {
		String q = "select bst.id, sum(case when vfa.inactive = false"
				+ " and b.inactive = false and v.status.volunteerActive = true then 1 else 0 end), count(v) from "
				+ BenefitingServiceTemplate.class.getName() + " bst" //
				+ " join bst.benefitingServices b" //
				+ " join b.volunteerAssignments vfa" //
				+ " join vfa.volunteer v" //
				+ " where bst.id in (:ids)" //
				+ " group by bst.id";

		Map<Long, Integer[]> results = new HashMap<>();
		List<Long> finalIds = new ArrayList<>(benefitingServiceIds);

		for (int i = 0; i < finalIds.size(); i += 2000) {
			List<Long> batchChunk = finalIds.subList(i, Math.min(finalIds.size(), i + 2000));

			@SuppressWarnings("unchecked")
			List<Object[]> queryResults = query(q).setParameter("ids", batchChunk).getResultList();
			for (Object[] r : queryResults) {
				int activeCount = r[1] == null ? 0 : ((Number) r[1]).intValue();
				Integer[] x = new Integer[] { activeCount, ((Number) r[2]).intValue() };
				results.put(((Number) r[0]).longValue(), x);
			}
		}

		return results;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<BenefitingServiceTemplate> findByCriteria(String name, Boolean activeStatus, Boolean gamesRelated, Boolean includeInactive,
			QueryCustomization... customization) {
		StringBuilder sb = new StringBuilder("select o from ").append(BenefitingServiceTemplate.class.getName())
				.append(" o");

		QueryCustomization cust = ArrayUtils.isEmpty(customization) ? new QueryCustomization() : customization[0];
		cust.appendRemainingJoins(sb, "o");

		if (cust.getOrderBy() == null)
			cust.setOrderBy("o.name");

		List<String> whereClauseItems = new ArrayList<>();
		Map<String, Object> params = new HashMap<>();

		if (StringUtils.isNotBlank(name)) {
			whereClauseItems.add("LOWER(o.name) like :name1");
			params.put("name1", "%" + name.toLowerCase() + "%");
		}

		if (activeStatus != null) {
			whereClauseItems.add("o.inactive = :inactive");
			params.put("inactive", !activeStatus);
		}

		if (gamesRelated != null) {
			whereClauseItems.add("o.gamesRelated = :gamesRelated");
			params.put("gamesRelated", gamesRelated);
		}
		Query q = constructQuery(em, sb, whereClauseItems, params, null, cust);

		return q.getResultList();
	}

	@Override
	public Map<Long, Integer> countOccasionalHoursForBenefitingServiceTemplateIds(
			Collection<Long> benefitingServiceTemplateIds) {
		List<Long> finalList = new ArrayList<>(benefitingServiceTemplateIds);

		String q = "select t.id, sum(w.hoursWorked) from " + OccasionalWorkEntry.class.getName()
				+ " w join w.benefitingService b join b.template t where t.id in (:ids) group by t.id";

		Map<Long, Integer> results = new HashMap<>();

		for (int i = 0; i < finalList.size(); i += 2000) {
			List<Long> batchChunk = finalList.subList(i, Math.min(finalList.size(), i + 2000));

			@SuppressWarnings("unchecked")
			List<Object[]> queryResults = query(q).setParameter("ids", batchChunk).getResultList();
			for (Object[] r : queryResults) {
				int hoursWorked = r[1] == null ? 0 : ((Number) r[1]).intValue();
				results.put(((Number) r[0]).longValue(), hoursWorked);
			}
		}

		return results;
	}
	
	@Override
	public Map<Long, Integer> countOccasionalHoursForBenefitingServiceRoleTemplateIds(
			Collection<Long> benefitingServiceRoleTemplateIds) {
		List<Long> finalList = new ArrayList<>(benefitingServiceRoleTemplateIds);

		String q = "select t.id, sum(w.hoursWorked) from " + OccasionalWorkEntry.class.getName()
				+ " w join w.benefitingServiceRole b join b.template t where t.id in (:ids) group by t.id";

		Map<Long, Integer> results = new HashMap<>();

		for (int i = 0; i < finalList.size(); i += 2000) {
			List<Long> batchChunk = finalList.subList(i, Math.min(finalList.size(), i + 2000));

			@SuppressWarnings("unchecked")
			List<Object[]> queryResults = query(q).setParameter("ids", batchChunk).getResultList();
			for (Object[] r : queryResults) {
				int hoursWorked = r[1] == null ? 0 : ((Number) r[1]).intValue();
				results.put(((Number) r[0]).longValue(), hoursWorked);
			}
		}

		return results;
	}

}
