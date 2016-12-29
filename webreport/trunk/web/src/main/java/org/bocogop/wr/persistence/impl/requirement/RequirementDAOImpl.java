package org.bocogop.wr.persistence.impl.requirement;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
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
import org.bocogop.wr.model.requirement.AbstractRequirement;
import org.bocogop.wr.model.requirement.FacilityRequirement;
import org.bocogop.wr.model.requirement.GlobalRequirement;
import org.bocogop.wr.persistence.dao.requirement.RequirementDAO;
import org.bocogop.wr.persistence.impl.GenericHibernateSortedDAOImpl;
import org.bocogop.wr.persistence.queryCustomization.QueryCustomization;

@Repository
public class RequirementDAOImpl extends GenericHibernateSortedDAOImpl<AbstractRequirement> implements RequirementDAO {
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(RequirementDAOImpl.class);

	@Override
	public List<AbstractRequirement> findByCriteria(Long facilityId, String name, QueryCustomization... customization) {

		List<String> whereClauseItems = new ArrayList<>();
		Map<String, Object> params = new HashMap<>();

		StringBuilder sb = new StringBuilder("select r from "); //

		if (facilityId != null) {
			sb.append(FacilityRequirement.class.getName()) //
					.append(" r left join fetch r.facility f "); //
			whereClauseItems.add("f.id = :facilityId ");
			params.put("facilityId", facilityId);
		} else {
			sb.append(GlobalRequirement.class.getName()) //
					.append(" r ");
		}

		QueryCustomization cust = ArrayUtils.isEmpty(customization) ? new QueryCustomization() : customization[0];
		cust.appendRemainingJoins(sb, "r");

		if (name != null) {
			whereClauseItems.add("lower(r.name) = :name ");
			params.put("name", name.toLowerCase());
		} else {
			if (cust.getOrderBy() == null)
				cust.setOrderBy("r.name");
		}

		Query q = constructQuery(em, sb, whereClauseItems, params, null, cust);

		@SuppressWarnings("unchecked")
		List<AbstractRequirement> results = q.getResultList();
		return results;

	}

	@Override
	public void changeType(long requirementId, String newTypeCode) {
		em.createNativeQuery("update wr.Requirement set type = :newType"
				+ ", MODIFIED_BY = :myUser, MODIFIED_DATE = :nowUTC, ver = ver + 1" //
				+ " where id = :id") //
				.setParameter("newType", newTypeCode) //
				.setParameter("id", requirementId) //
				.setParameter("myUser", AbstractAuditedPersistent.getCurrentUserIdForAudit()) //
				.setParameter("nowUTC", ZonedDateTime.now(ZoneId.of("Z"))) //
				.executeUpdate();
	}

	@Override
	public void updateFieldsWithoutVersionIncrement(long requirementId, boolean setRoleType, Long roleTypeId) {
		if (!setRoleType)
			throw new IllegalArgumentException("No update parameter was specified");

		/*
		 * Necessary in case we made changes prior to this that haven't been
		 * flushed yet - CPB
		 */
		em.flush();

		List<String> updates = new ArrayList<>();
		Map<String, Object> params = new HashMap<>();

		if (setRoleType) {
			updates.add("WR_STD_BenefitingServiceRoleTypeFK = :roleTypeId");
			params.put("roleTypeId", roleTypeId);
		}

		updates.add("MODIFIED_BY = :myUser");
		params.put("myUser", AbstractAuditedPersistent.getCurrentUserIdForAudit());
		updates.add("MODIFIED_DATE = :nowUTC");
		params.put("nowUTC", ZonedDateTime.now(ZoneId.of("Z")));

		StringBuilder sb = new StringBuilder("update wr.Requirement set ");
		sb.append(StringUtils.join(updates, ", "));
		sb.append(" where id = :requirementId");
		params.put("requirementId", requirementId);

		Query q = em.createNativeQuery(sb.toString());
		for (Entry<String, Object> paramEntry : params.entrySet())
			q.setParameter(paramEntry.getKey(), paramEntry.getValue());
		int numUpdated = q.executeUpdate();

		if (numUpdated == 0)
			throw new IllegalStateException("No requirement ID " + requirementId + " found.");
	}

}
