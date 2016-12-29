package org.bocogop.wr.persistence.impl.volunteer;

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
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import org.bocogop.shared.model.core.AbstractAuditedPersistent;
import org.bocogop.wr.model.organization.AbstractBasicOrganization;
import org.bocogop.wr.model.volunteer.Volunteer;
import org.bocogop.wr.model.volunteer.VolunteerOrganization;
import org.bocogop.wr.persistence.dao.organization.OrganizationDAO;
import org.bocogop.wr.persistence.dao.volunteer.VolunteerOrganizationDAO;
import org.bocogop.wr.persistence.impl.GenericHibernateDAOImpl;
import org.bocogop.wr.persistence.queryCustomization.QueryCustomization;

@Repository
public class VolunteerOrganizationDAOImpl extends GenericHibernateDAOImpl<VolunteerOrganization>
		implements VolunteerOrganizationDAO {
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(VolunteerOrganizationDAOImpl.class);

	@Autowired
	private OrganizationDAO organizationDAO;
	
	@SuppressWarnings("unchecked")
	@Override
	public List<VolunteerOrganization> findByCriteria(Long volunteerId, Long organizationId, Boolean activeStatus,
			Long organizationFacilityId, QueryCustomization... customization) {
		if (volunteerId == null && organizationId == null && organizationFacilityId == null)
			throw new IllegalArgumentException("No criteria specified");

		StringBuilder sb = new StringBuilder("select v from ").append(VolunteerOrganization.class.getName())
				.append(" v");

		/* Don't bother with this yet - CPB */
		QueryCustomization cust = new QueryCustomization();

		List<String> whereClauseItems = new ArrayList<>();
		Map<String, Object> params = new HashMap<>();

		if (volunteerId != null) {
			whereClauseItems.add("v.volunteer.id = :volunteerId");
			params.put("volunteerId", volunteerId);
		}

		if (organizationId != null) {
			whereClauseItems.add("v.organization.id = :organizationId");
			params.put("organizationId", organizationId);
		}

		if (organizationFacilityId != null) {
			whereClauseItems
					.add("(v.organization.facility is null or v.organization.facility.id = :organizationFacilityId)");
			params.put("organizationFacilityId", organizationFacilityId);
		}

		if (activeStatus != null) {
			whereClauseItems.add("v.inactive = :inactiveStatus");
			params.put("inactiveStatus", !activeStatus);
		}

		Query q = constructQuery(em, sb, whereClauseItems, params, null, cust);

		List<VolunteerOrganization> resultList = q.getResultList();
		return resultList;
	}

	@Override
	public int bulkUpdatePrimaryOrganizationsByCriteria(Collection<Long> primaryOrganizationIds,
			boolean setPrimaryOrganization, Long newPrimaryOrganizationId) {
		if (primaryOrganizationIds == null)
			throw new IllegalArgumentException("No restriction criteria specified");
		if (!setPrimaryOrganization)
			throw new IllegalArgumentException("No updates specified");

		if (primaryOrganizationIds.isEmpty())
			return 0;

		flush();

		List<String> updates = new ArrayList<>();
		Map<String, Object> params = new HashMap<>();

		if (setPrimaryOrganization) {
			AbstractBasicOrganization primaryOrganization = newPrimaryOrganizationId == null ? null
					: organizationDAO.findRequiredByPrimaryKey(newPrimaryOrganizationId);
			updates.add("primaryOrganization = :primaryOrganization");
			params.put("primaryOrganization", primaryOrganization);
		}

		String jpql = "update " + Volunteer.class.getName() //
				+ " set " + StringUtils.join(updates, ", ") //
				+ ", modifiedBy = :myUser, modifiedDate = :nowUTC, version = version + 1" // "
				+ " where id in (select v.id from " + Volunteer.class.getName() + " v where 1=1" //
				+ (primaryOrganizationIds != null ? " and v.primaryOrganization.id in (:primaryOrganizationIds)" : "") //
				+ ")";

		Query q = query(jpql);

		for (Entry<String, Object> param : params.entrySet())
			q.setParameter(param.getKey(), param.getValue());
		if (primaryOrganizationIds != null)
			q.setParameter("primaryOrganizationIds", primaryOrganizationIds);
		q.setParameter("myUser", AbstractAuditedPersistent.getCurrentUserIdForAudit()) //
				.setParameter("nowUTC", ZonedDateTime.now(ZoneId.of("Z")));

		return q.executeUpdate();
	}

	@Override
	public int bulkUpdateByCriteria(Collection<Long> orgOrBranchIds, Boolean currentActiveStatus,
			Boolean newActiveStatus) {
		boolean hasOrgOrBranchIds = CollectionUtils.isNotEmpty(orgOrBranchIds);

		if (!hasOrgOrBranchIds && currentActiveStatus == null)
			throw new IllegalArgumentException("No restriction criteria specified");
		if (newActiveStatus == null)
			throw new IllegalArgumentException("No updates specified");

		flush();

		List<String> updates = new ArrayList<>();
		Map<String, Object> params = new HashMap<>();

		if (newActiveStatus != null) {
			updates.add("inactive = :inactiveStatus");
			params.put("inactiveStatus", !newActiveStatus);
		}

		String jpql = "update " + VolunteerOrganization.class.getName() //
				+ " set " + StringUtils.join(updates, ", ") //
				+ ", modifiedBy = :myUser, modifiedDate = :nowUTC, version = version + 1" // "
				+ " where id in (select vo.id from " + VolunteerOrganization.class.getName() + " vo where 1=1" //
				+ (hasOrgOrBranchIds ? " and vo.organization.id in (:orgOrBranchIds)" : "") //
				+ (newActiveStatus != null ? " and vo.inactive = :currentInactiveStatus" : "") //
				+ ")";

		Query q = query(jpql);

		for (Entry<String, Object> param : params.entrySet())
			q.setParameter(param.getKey(), param.getValue());
		if (hasOrgOrBranchIds)
			q.setParameter("orgOrBranchIds", orgOrBranchIds);
		if (currentActiveStatus != null)
			q.setParameter("currentInactiveStatus", !currentActiveStatus);
		q.setParameter("myUser", AbstractAuditedPersistent.getCurrentUserIdForAudit()) //
				.setParameter("nowUTC", ZonedDateTime.now(ZoneId.of("Z")));

		int recordsUpdated = q.executeUpdate();
		return recordsUpdated;
	}

	@Override
	public int inactivateForInactiveVolunteers() {
		flush();

		String jpql = "update " + VolunteerOrganization.class.getName() //
				+ " set inactive = true, modifiedBy = :myUser, modifiedDate = :nowUTC, version = version + 1" // "
				+ " where id in (" //
				+ "		select vo.id from " + VolunteerOrganization.class.getName() + " vo" //
				+ " 	where vo.inactive = false" //
				+ "		and vo.volunteer.status.volunteerActive = false)";

		Query q = query(jpql);

		q.setParameter("myUser", AbstractAuditedPersistent.getCurrentUserIdForAudit()) //
				.setParameter("nowUTC", ZonedDateTime.now(ZoneId.of("Z")));

		int recordsUpdated = q.executeUpdate();
		return recordsUpdated;
	}

}
