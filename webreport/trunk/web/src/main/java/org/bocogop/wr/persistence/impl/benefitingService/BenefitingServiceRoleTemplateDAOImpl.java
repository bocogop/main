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

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import org.bocogop.shared.model.core.AbstractAuditedPersistent;
import org.bocogop.wr.model.benefitingService.BenefitingServiceRoleTemplate;
import org.bocogop.wr.persistence.dao.benefitingService.BenefitingServiceRoleTemplateDAO;
import org.bocogop.wr.persistence.impl.GenericHibernateSortedDAOImpl;
import org.bocogop.wr.persistence.queryCustomization.QueryCustomization;

@Repository
public class BenefitingServiceRoleTemplateDAOImpl extends GenericHibernateSortedDAOImpl<BenefitingServiceRoleTemplate>
		implements BenefitingServiceRoleTemplateDAO {
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(BenefitingServiceRoleTemplateDAOImpl.class);

	@SuppressWarnings("unchecked")
	@Override
	public List<BenefitingServiceRoleTemplate> findByCriteria(String name, Boolean activeStatus,
			QueryCustomization... customization) {
		StringBuilder sb = new StringBuilder("select o from ").append(BenefitingServiceRoleTemplate.class.getName())
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

		Query q = constructQuery(em, sb, whereClauseItems, params, null, cust);

		return q.getResultList();
	}

	@Override
	public Map<Long, Integer[]> countVolunteersForBenefitingServiceRoleTemplateIds(
			Collection<Long> benefitingServiceRoleTemplateIds) {
		String q = "select bsrt.id, sum(case when vfa.inactive = false and bsr.inactive = false"
				+ " and v.status.volunteerActive = true then 1 else 0 end), count(v) from "
				+ BenefitingServiceRoleTemplate.class.getName() + " bsrt" //
				+ " join bsrt.benefitingServiceRoles bsr" //
				+ " join bsr.volunteerAssignments vfa" //
				+ " join vfa.volunteer v" //
				+ " where bsrt.id in (:ids)" //
				+ " group by bsrt.id";

		Map<Long, Integer[]> results = new HashMap<>();

		List<Long> finalIds = new ArrayList<>(benefitingServiceRoleTemplateIds);

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

	@Override
	public int bulkDeleteByCriteria(Long benefitingServiceTemplateId) {
		if (benefitingServiceTemplateId == null)
			throw new IllegalArgumentException("Must specify at least one piece of filtering criteria");

		flush();

		Query q = query("delete from " + BenefitingServiceRoleTemplate.class.getName()
				+ " where id in (select b.id from " + BenefitingServiceRoleTemplate.class.getName() + " b" //
				+ " left join b.benefitingServiceTemplate bst" //
				+ " where (1=2" //
				+ (benefitingServiceTemplateId != null ? " or bst.id = :benefitingServiceTemplateId" : "") //
				+ "))");
		if (benefitingServiceTemplateId != null)
			q.setParameter("benefitingServiceTemplateId", benefitingServiceTemplateId);
		return q.executeUpdate();
	}

	@Override
	public int bulkUpdateByCriteria(Long benefitingServiceTemplateId, Boolean requiredAndReadOnly,
			Boolean activeStatus) {
		if (benefitingServiceTemplateId == null && requiredAndReadOnly == null)
			throw new IllegalArgumentException("No restriction criteria specified");
		if (activeStatus == null)
			throw new IllegalArgumentException("No updates specified");

		flush();

		List<String> updates = new ArrayList<>();
		Map<String, Object> params = new HashMap<>();

		if (activeStatus != null) {
			updates.add("inactive = :inactiveStatus");
			params.put("inactiveStatus", !activeStatus);
		}

		String jpql = "update " + BenefitingServiceRoleTemplate.class.getName() //
				+ " set " + StringUtils.join(updates, ", ") //
				+ ", modifiedBy = :myUser, modifiedDate = :nowUTC, version = version + 1" // "
				+ " where id in (select bsrt.id from " + BenefitingServiceRoleTemplate.class.getName() + " bsrt where 1=1" //
				+ (benefitingServiceTemplateId != null ? " and bsrt.benefitingServiceTemplate.id = :benefitingServiceTemplateId" : "") //
				+ (requiredAndReadOnly != null ? " and bsrt.requiredAndReadOnly = :requiredAndReadOnly" : "") //
				+ ")";

		Query q = query(jpql);

		for (Entry<String, Object> param : params.entrySet())
			q.setParameter(param.getKey(), param.getValue());
		if (benefitingServiceTemplateId != null)
			q.setParameter("benefitingServiceTemplateId", benefitingServiceTemplateId);
		if (requiredAndReadOnly != null)
			q.setParameter("requiredAndReadOnly", requiredAndReadOnly);
		
		q.setParameter("myUser", AbstractAuditedPersistent.getCurrentUserIdForAudit()) //
				.setParameter("nowUTC", ZonedDateTime.now(ZoneId.of("Z")));

		return q.executeUpdate();
	}

}
