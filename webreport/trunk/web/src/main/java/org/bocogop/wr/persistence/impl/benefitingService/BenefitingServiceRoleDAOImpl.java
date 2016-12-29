package org.bocogop.wr.persistence.impl.benefitingService;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.persistence.Query;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import org.bocogop.shared.model.core.AbstractAuditedPersistent;
import org.bocogop.wr.model.benefitingService.BenefitingServiceRole;
import org.bocogop.wr.model.benefitingService.BenefitingServiceRoleType;
import org.bocogop.wr.model.facility.Facility;
import org.bocogop.wr.model.facility.Location;
import org.bocogop.wr.persistence.dao.benefitingService.BenefitingServiceRoleDAO;
import org.bocogop.wr.persistence.dao.benefitingService.BenefitingServiceRoleQuickSearchResult;
import org.bocogop.wr.persistence.impl.GenericHibernateSortedDAOImpl;
import org.bocogop.wr.persistence.queryCustomization.QueryCustomization;

@Repository
public class BenefitingServiceRoleDAOImpl extends GenericHibernateSortedDAOImpl<BenefitingServiceRole>
		implements BenefitingServiceRoleDAO {
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(BenefitingServiceRoleDAOImpl.class);

	@SuppressWarnings("unchecked")
	@Override
	public List<BenefitingServiceRole> findByCriteria(String name, Collection<Long> facilityIds,
			boolean includeLocationsUnderFacilities, Boolean activeStatus, QueryCustomization... customization) {
		StringBuilder sb = new StringBuilder("select o from ").append(BenefitingServiceRole.class.getName())
				.append(" o");
		sb.append(" left join o.facility i");

		QueryCustomization cust = ArrayUtils.isEmpty(customization) ? new QueryCustomization() : customization[0];
		cust.appendRemainingJoins(sb, "o");

		List<String> whereClauseItems = new ArrayList<>();
		Map<String, Object> params = new HashMap<>();

		if (StringUtils.isNotBlank(name)) {
			whereClauseItems.add("LOWER(o.name) like :name1");
			params.put("name1", "%" + name.toLowerCase() + "%");
		}

		if (CollectionUtils.isNotEmpty(facilityIds)) {
			whereClauseItems.add("COALESCE(i.id, -1) in (:facilityIds)" + (includeLocationsUnderFacilities
					? " or (TYPE(i) = :locationClassType and i.parent.id in (:facilityIds))" : ""));
			params.put("facilityIds", facilityIds);
			if (includeLocationsUnderFacilities)
				params.put("locationClassType", Location.class);
		}

		if (activeStatus != null) {
			whereClauseItems.add("o.inactive = :inactive");
			params.put("inactive", !activeStatus);
		}

		Query q = constructQuery(em, sb, whereClauseItems, params, null, cust);

		return q.getResultList();
	}

	@Override
	public Map<Long, Integer[]> countVolunteersForBenefitingServiceRoleIds(List<Long> benefitingServiceRoleIds) {
		String q = "select b.id, sum(case when vfa.inactive = false and v.status.volunteerActive = true then 1 else 0 end), count(v) from "
				+ BenefitingServiceRole.class.getName() + " b" //
				+ " join b.volunteerAssignments vfa" //
				+ " join vfa.volunteer v" //
				+ " where b.id in (:ids)" //
				+ " group by b.id";

		Map<Long, Integer[]> results = new HashMap<>();

		for (int i = 0; i < benefitingServiceRoleIds.size(); i += 2000) {
			List<Long> batchChunk = benefitingServiceRoleIds.subList(i,
					Math.min(benefitingServiceRoleIds.size(), i + 2000));

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
	public int bulkUpdateByCriteria(Long benefitingServiceTemplateId, Long benefitingServiceRoleTemplateId,
			Long benefitingServiceId, Long facilityOrLocationId, Boolean requiredAndReadOnly, String name,
			Boolean activeStatus, BenefitingServiceRoleType roleType) {
		if (benefitingServiceTemplateId == null && benefitingServiceRoleTemplateId == null
				&& benefitingServiceId == null && facilityOrLocationId == null)
			throw new IllegalArgumentException("No restriction criteria specified");
		if (name == null && activeStatus == null)
			throw new IllegalArgumentException("No updates specified");

		flush();

		List<String> updates = new ArrayList<>();
		Map<String, Object> params = new HashMap<>();

		if (name != null) {
			updates.add("name = :name");
			params.put("name", name);
		}

		if (activeStatus != null) {
			updates.add("inactive = :inactiveStatus");
			params.put("inactiveStatus", !activeStatus);
		}

		if (roleType != null) {
			updates.add("roleType = :roleType");
			params.put("roleType", roleType);
		}

		String jpql = "update " + BenefitingServiceRole.class.getName() //
				+ " set " + StringUtils.join(updates, ", ") //
				+ ", modifiedBy = :myUser, modifiedDate = :nowUTC, version = version + 1" // "
				+ " where id in (select bsr.id from " + BenefitingServiceRole.class.getName() + " bsr where 1=1" //
				+ (benefitingServiceTemplateId != null
						? " and bsr.benefitingService.template.id = :benefitingServiceTemplateId" : "") //
				+ (benefitingServiceRoleTemplateId != null ? " and bsr.template.id = :benefitingServiceRoleTemplateId"
						: "") //
				+ (benefitingServiceId != null ? " and bsr.benefitingService.id = :benefitingServiceId" : "") //
				+ (facilityOrLocationId != null ? " and bsr.facility.id = :facilityOrLocationId" : "") //
				+ (requiredAndReadOnly != null ? " and bsr.requiredAndReadOnly = :requiredAndReadOnly" : "") //
				+ ")";

		Query q = query(jpql);

		for (Entry<String, Object> param : params.entrySet())
			q.setParameter(param.getKey(), param.getValue());
		if (benefitingServiceTemplateId != null)
			q.setParameter("benefitingServiceTemplateId", benefitingServiceTemplateId);
		if (benefitingServiceRoleTemplateId != null)
			q.setParameter("benefitingServiceRoleTemplateId", benefitingServiceRoleTemplateId);
		if (benefitingServiceId != null)
			q.setParameter("benefitingServiceId", benefitingServiceId);
		if (facilityOrLocationId != null)
			q.setParameter("facilityOrLocationId", facilityOrLocationId);
		if (requiredAndReadOnly != null)
			q.setParameter("requiredAndReadOnly", requiredAndReadOnly);
		q.setParameter("myUser", AbstractAuditedPersistent.getCurrentUserIdForAudit()) //
				.setParameter("nowUTC", ZonedDateTime.now(ZoneId.of("Z")));

		return q.executeUpdate();
	}

	@Override
	public int bulkDeleteByCriteria(Long benefitingServiceTemplateId, Long benefitingServiceRoleTemplateId,
			Long benefitingServiceId) {
		if (benefitingServiceTemplateId == null && benefitingServiceRoleTemplateId == null
				&& benefitingServiceId == null)
			throw new IllegalArgumentException("Must specify at least one piece of filtering criteria");

		flush();

		Query q = query("delete from " + BenefitingServiceRole.class.getName() + " where id in (select b.id from "
				+ BenefitingServiceRole.class.getName() + " b" //
				+ " left join b.benefitingService bs" //
				// + " left join vfa.benefitingServiceRole bsr" //
				+ " left join bs.template bst" //
				+ " left join b.template bsrt" //
				+ " where (1=2" //
				+ (benefitingServiceTemplateId != null ? " or bst.id = :benefitingServiceTemplateId" : "") //
				+ (benefitingServiceRoleTemplateId != null ? " or bsrt.id = :benefitingServiceRoleTemplateId" : "") //
				+ (benefitingServiceId != null ? " or bs.id = :benefitingServiceId" : "") //
				+ "))");
		if (benefitingServiceId != null)
			q.setParameter("benefitingServiceId", benefitingServiceId);
		if (benefitingServiceTemplateId != null)
			q.setParameter("benefitingServiceTemplateId", benefitingServiceTemplateId);
		if (benefitingServiceRoleTemplateId != null)
			q.setParameter("benefitingServiceRoleTemplateId", benefitingServiceRoleTemplateId);
		return q.executeUpdate();
	}

	@Override
	public SortedSet<BenefitingServiceRoleQuickSearchResult> quickSearch(String searchValue, Long facilityIdRestriction,
			Integer maxResults) {
		StringBuilder sb = new StringBuilder();
		sb.append("select bsr.id, bsr.name, bs.name, f.name, TYPE(f), bsr.inactive, bs.subdivision from ");
		sb.append(BenefitingServiceRole.class.getName());
		sb.append(" bsr join bsr.benefitingService bs join bsr.facility f left join f.parent fp where 1=1");

		Map<String, Object> params = new HashMap<>();

		if (StringUtils.isNotBlank(searchValue)) {
			String[] tokens = searchValue.split("\\W");
			for (int i = 0; i < tokens.length; i++) {
				String token = tokens[i];
				if (StringUtils.isBlank(token))
					continue;

				sb.append(" and (lower(bsr.name) like :text").append(i);
				sb.append(" or lower(bs.name) like :text").append(i).append(")");
				params.put("text" + i, "%" + token.toLowerCase() + "%");
			}
		}

		if (facilityIdRestriction != null) {
			sb.append(
					" and ((TYPE(f) = :facilityClass and f.id = :facilityId) or (TYPE(f) = :locationClass and fp.id = :facilityId))");
			params.put("facilityId", facilityIdRestriction);
			params.put("facilityClass", Facility.class);
			params.put("locationClass", Location.class);
		}

		Query q = query(sb.toString());
		for (Entry<String, Object> entry : params.entrySet())
			q.setParameter(entry.getKey(), entry.getValue());

		if (maxResults != null && maxResults > 0)
			q.setMaxResults(maxResults);

		@SuppressWarnings("unchecked")
		List<Object[]> results = q.getResultList();

		SortedSet<BenefitingServiceRoleQuickSearchResult> returnResults = new TreeSet<>();
		for (Object[] result : results) {
			Class<?> facilityType = (Class<?>) result[4];
			returnResults.add(new BenefitingServiceRoleQuickSearchResult(((Number) result[0]).longValue(),
					(String) result[1], (String) result[2], (String) result[6],
					facilityType == Facility.class ? null : (String) result[3], !((Boolean) result[5])));
		}
		return returnResults;
	}

}
