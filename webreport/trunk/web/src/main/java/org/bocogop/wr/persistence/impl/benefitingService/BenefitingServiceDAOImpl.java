package org.bocogop.wr.persistence.impl.benefitingService;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.persistence.Query;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import org.bocogop.shared.model.core.AbstractAuditedPersistent;
import org.bocogop.wr.model.benefitingService.BenefitingService;
import org.bocogop.wr.model.time.OccasionalWorkEntry;
import org.bocogop.wr.persistence.dao.benefitingService.BenefitingServiceDAO;
import org.bocogop.wr.persistence.impl.GenericHibernateSortedDAOImpl;
import org.bocogop.wr.persistence.queryCustomization.QueryCustomization;

@Repository
public class BenefitingServiceDAOImpl extends GenericHibernateSortedDAOImpl<BenefitingService>
		implements BenefitingServiceDAO {
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(BenefitingServiceDAOImpl.class);

	@SuppressWarnings("unchecked")
	@Override
	public List<BenefitingService> findByCriteria(String name, String subdivision, String abbreviation,
			Collection<Long> facilityIds, Boolean templateIsNull, Boolean gamesRelated, Boolean activeStatus,
			Boolean includeInactive, QueryCustomization... customization) {
	
		StringBuilder sb = new StringBuilder("select o from ").append(BenefitingService.class.getName()).append(" o");
		sb.append(" join o.facility i");

		QueryCustomization cust = ArrayUtils.isEmpty(customization) ? new QueryCustomization() : customization[0];
		cust.appendRemainingJoins(sb, "o");

		if (cust.getOrderBy() == null)
			cust.setOrderBy("o.scope desc, i.stationNumber, o.name");

		List<String> whereClauseItems = new ArrayList<>();
		Map<String, Object> params = new HashMap<>();

		if (StringUtils.isNotBlank(name)) {
			whereClauseItems.add("LOWER(o.name) like :name1");
			params.put("name1", "%" + name.toLowerCase() + "%");
		}

		if (CollectionUtils.isNotEmpty(facilityIds)) {
			whereClauseItems.add("i.id in (:facilityIds)");
			params.put("facilityIds", facilityIds);
		}

		if (gamesRelated != null) {
			whereClauseItems.add("o.gamesRelated = :gamesRelated");
			params.put("gamesRelated", gamesRelated);
		}

		if (templateIsNull != null) {
			whereClauseItems.add("o.template is" + (!templateIsNull ? " not" : "") + " null");
		}

		if (activeStatus != null && (includeInactive == null || includeInactive == false)) {
			whereClauseItems.add("o.inactive = :inactive");
			params.put("inactive", !activeStatus);
		}

		Query q = constructQuery(em, sb, whereClauseItems, params, null, cust);

		return q.getResultList();
	}

	@Override
	public Map<Long, Integer[]> countVolunteersForBenefitingServiceIds(List<Long> benefitingServiceIds) {
		String q = "select b.id, sum(case when vfa.inactive = false and v.status.volunteerActive = true then 1 else 0 end), count(v) from "
				+ BenefitingService.class.getName() + " b" //
				+ " join b.volunteerAssignments vfa" //
				+ " join vfa.volunteer v" //
				+ " where b.id in (:ids)" //
				+ " group by b.id";

		Map<Long, Integer[]> results = new HashMap<>();

		for (int i = 0; i < benefitingServiceIds.size(); i += 2000) {
			List<Long> batchChunk = benefitingServiceIds.subList(i, Math.min(benefitingServiceIds.size(), i + 2000));

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

	@Override
	public Map<Long, Integer> countOccasionalHoursForBenefitingServiceIds(List<Long> benefitingServiceIds) {
		String q = "select b.id, sum(w.hoursWorked) from " + OccasionalWorkEntry.class.getName()
				+ " w join w.benefitingService b where b.id in (:ids) group by b.id";

		Map<Long, Integer> results = new HashMap<>();

		for (int i = 0; i < benefitingServiceIds.size(); i += 2000) {
			List<Long> batchChunk = benefitingServiceIds.subList(i, Math.min(benefitingServiceIds.size(), i + 2000));

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
	public Map<Long, Integer> countOccasionalHoursForBenefitingServiceRoleIds(List<Long> benefitingServiceRoleIds) {
		String q = "select b.id, sum(w.hoursWorked) from " + OccasionalWorkEntry.class.getName()
				+ " w join w.benefitingServiceRole b where b.id in (:ids) group by b.id";

		Map<Long, Integer> results = new HashMap<>();

		for (int i = 0; i < benefitingServiceRoleIds.size(); i += 2000) {
			List<Long> batchChunk = benefitingServiceRoleIds.subList(i,
					Math.min(benefitingServiceRoleIds.size(), i + 2000));

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
	public int bulkUpdateByCriteria(Long benefitingServiceTemplateId, Long facilityOrLocationId, boolean updateName,
			String name, boolean updateAbbreviation, String abbreviation, boolean updateSubdivision, String subdivision,
			Boolean activeStatus, Boolean gamesRelated) {
		if (benefitingServiceTemplateId == null && facilityOrLocationId == null)
			throw new IllegalArgumentException("No restriction criteria specified");
		if (name == null && abbreviation == null && subdivision == null && activeStatus == null)
			throw new IllegalArgumentException("No updates specified");

		flush();

		List<String> updates = new ArrayList<>();
		Map<String, Object> params = new HashMap<>();

		if (updateName) {
			updates.add("name = :name");
			params.put("name", name);
		}

		if (updateAbbreviation) {
			updates.add("abbreviation = :abbreviation");
			params.put("abbreviation", abbreviation);
		}

		if (updateSubdivision) {
			updates.add("subdivision = :subdivision");
			params.put("subdivision", subdivision);
		}

		if (activeStatus != null) {
			updates.add("inactive = :inactiveStatus");
			params.put("inactiveStatus", !activeStatus);
		}

		if (gamesRelated != null) {
			updates.add("gamesRelated = :gamesRelated");
			params.put("gamesRelated", gamesRelated);
		}

		Query q = query("update " + BenefitingService.class.getName() //
				+ " set " + StringUtils.join(updates, ", ") //
				+ ", modifiedBy = :myUser, modifiedDate = :nowUTC, version = version + 1" // "
				+ " where id in (select bs.id from " + BenefitingService.class.getName() //
				+ " bs where 1=1" //
				+ (benefitingServiceTemplateId != null ? " and bs.template.id = :benefitingServiceTemplateId" : "") //
				+ (facilityOrLocationId != null ? " and bs.facility.id = :facilityOrLocationId" : "") //
				+ ")");

		for (Entry<String, Object> param : params.entrySet())
			q.setParameter(param.getKey(), param.getValue());

		if (benefitingServiceTemplateId != null)
			q.setParameter("benefitingServiceTemplateId", benefitingServiceTemplateId);
		if (facilityOrLocationId != null)
			q.setParameter("facilityOrLocationId", facilityOrLocationId);

		return q.setParameter("myUser", AbstractAuditedPersistent.getCurrentUserIdForAudit()) //
				.setParameter("nowUTC", ZonedDateTime.now(ZoneId.of("Z"))) //
				.executeUpdate();
	}

	@Override
	public int bulkDeleteByCriteria(Long benefitingServiceTemplateId) {
		if (benefitingServiceTemplateId == null)
			throw new IllegalArgumentException("Must specify at least one piece of filtering criteria");

		flush();

		Query q = query("delete from " + BenefitingService.class.getName() + " where id in (select bs.id from "
				+ BenefitingService.class.getName() + " bs" //
				+ " left join bs.template bst" //
				+ " where (1=2" //
				+ (benefitingServiceTemplateId != null ? " or bst.id = :benefitingServiceTemplateId" : "") //
				+ "))");
		if (benefitingServiceTemplateId != null)
			q.setParameter("benefitingServiceTemplateId", benefitingServiceTemplateId);
		return q.executeUpdate();
	}

}
