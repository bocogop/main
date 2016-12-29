package org.bocogop.wr.persistence.impl.volunteer;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Query;

import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import org.bocogop.shared.model.core.AbstractAuditedPersistent;
import org.bocogop.wr.model.benefitingService.BenefitingServiceRole;
import org.bocogop.wr.model.facility.Facility;
import org.bocogop.wr.model.facility.Location;
import org.bocogop.wr.model.time.WorkEntry;
import org.bocogop.wr.model.volunteer.VolunteerAssignment;
import org.bocogop.wr.persistence.dao.benefitingService.BenefitingServiceRoleDAO;
import org.bocogop.wr.persistence.dao.volunteer.VolunteerAssignmentDAO;
import org.bocogop.wr.persistence.impl.GenericHibernateDAOImpl;
import org.bocogop.wr.persistence.queryCustomization.QueryCustomization;
import org.bocogop.wr.persistence.queryCustomization.fieldTypes.FacilityAssociationFieldType;

@Repository
public class VolunteerAssignmentDAOImpl extends GenericHibernateDAOImpl<VolunteerAssignment>
		implements VolunteerAssignmentDAO {
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(VolunteerAssignmentDAOImpl.class);

	@Autowired
	private BenefitingServiceRoleDAO benefitingServiceRoleDAO;

	@Override
	public int[] countByCriteria(Long facilityOrLocationId) {
		if (facilityOrLocationId == null)
			throw new IllegalArgumentException("No filter criteria specified");

		String qStr = "select sum(case when b.inactive = false then 1 else 0 end), count(b) from "
				+ VolunteerAssignment.class.getName() + " b" //
				+ " where 1=1" //
				+ (facilityOrLocationId != null ? " and b.facility.id = :facilityOrLocationId" : "");

		Query q = query(qStr);

		if (facilityOrLocationId != null) {
			q.setParameter("facilityOrLocationId", facilityOrLocationId);
		}

		@SuppressWarnings("unchecked")
		List<Object[]> resultList = q.getResultList();

		if (resultList.isEmpty()) {
			return new int[] { 0, 0 };
		} else {
			Object[] queryResults = resultList.get(0);
			int activeCount = queryResults[0] == null ? 0 : ((Number) queryResults[0]).intValue();
			return new int[] { activeCount, ((Number) queryResults[1]).intValue() };
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<VolunteerAssignment> findByCriteria(Long volunteerId, Boolean activeStatus,
			Long benefitingServiceRoleId, Long facilityOrLocationId, Long facilityOrParentFacilityId,
			QueryCustomization... customization) {
		if (facilityOrLocationId != null && facilityOrParentFacilityId != null)
			throw new IllegalArgumentException(
					"Either facilityOrLocationId or facilityOrParentFacilityId supported, but not both");

		StringBuilder sb = new StringBuilder("select v from ").append(VolunteerAssignment.class.getName()).append(" v");
		if (facilityOrLocationId != null || facilityOrParentFacilityId != null)
			sb.append(" join v.facility f");

		QueryCustomization cust = ArrayUtils.isEmpty(customization) ? new QueryCustomization() : customization[0];
		if (facilityOrParentFacilityId != null)
			cust.appendRequiredJoin(sb, true, "f", FacilityAssociationFieldType.PARENT, "fp");
		cust.appendRemainingJoins(sb, "v");

		List<String> whereClauseItems = new ArrayList<>();
		Map<String, Object> params = new HashMap<>();

		if (volunteerId != null) {
			whereClauseItems.add("v.volunteer.id = :volunteerId");
			params.put("volunteerId", volunteerId);
		}

		if (activeStatus != null) {
			whereClauseItems.add("v.inactive = :inactiveStatus");
			params.put("inactiveStatus", !activeStatus);
		}

		if (benefitingServiceRoleId != null) {
			whereClauseItems.add("v.benefitingServiceRole.id = :benefitingServiceRoleId");
			params.put("benefitingServiceRoleId", benefitingServiceRoleId);
		}

		if (facilityOrLocationId != null) {
			whereClauseItems.add("f.id = :facilityOrLocationId");
			params.put("facilityOrLocationId", facilityOrLocationId);
		} else if (facilityOrParentFacilityId != null) {
			whereClauseItems.add("((TYPE(f) = :facilityType and f.id = :facilityId)" //
					+ " or (TYPE(f) = :locationType and fp.id = :facilityId))");
			params.put("facilityId", facilityOrParentFacilityId);
			params.put("facilityType", Facility.class);
			params.put("locationType", Location.class);
		}

		Query q = constructQuery(em, sb, whereClauseItems, params, null, cust);

		int maxResults = -1;

		if (cust.getRowLimitation() != null)
			maxResults = cust.getRowLimitation();

		if (maxResults > 0)
			q.setMaxResults(maxResults);

		List<VolunteerAssignment> resultList = q.getResultList();
		return resultList;
	}

	@Override
	public int bulkInactivateByCriteria(Long benefitingServiceTemplateId, Long benefitingServiceRoleTemplateId,
			Long benefitingServiceId, Long benefitingServiceRoleId, Long facilityOrLocationId) {
		if (benefitingServiceId == null && benefitingServiceTemplateId == null && benefitingServiceRoleId == null
				&& benefitingServiceRoleTemplateId == null && facilityOrLocationId == null)
			throw new IllegalArgumentException("Must specify one piece of filter criteria");

		/* Must flush before any bulk command - CPB */
		flush();

		Query q = query("update " + VolunteerAssignment.class.getName() + " set inactive = true" //
				+ ", modifiedBy = :myUser, modifiedDate = :nowUTC, version = version + 1" //
				+ " where id in (select vfa.id from " + VolunteerAssignment.class.getName() + " vfa where 1 = 1" // ;
				+ (benefitingServiceId != null ? " and vfa.benefitingService.id = :benefitingServiceId" : "")
				+ (benefitingServiceTemplateId != null
						? " and vfa.benefitingService.template.id = :benefitingServiceTemplateId" : "")
				+ (benefitingServiceRoleId != null ? " and vfa.benefitingServiceRole.id = :benefitingServiceRoleId"
						: "")
				+ (benefitingServiceRoleTemplateId != null
						? " and vfa.benefitingServiceRole.template.id = :benefitingServiceRoleTemplateId" : "")
				+ (facilityOrLocationId != null ? " and vfa.facility.id = :facilityOrLocationId" : "") + ")");

		if (benefitingServiceId != null)
			q.setParameter("benefitingServiceId", benefitingServiceId);
		if (benefitingServiceTemplateId != null)
			q.setParameter("benefitingServiceTemplateId", benefitingServiceTemplateId);
		if (benefitingServiceRoleId != null)
			q.setParameter("benefitingServiceRoleId", benefitingServiceRoleId);
		if (benefitingServiceRoleTemplateId != null)
			q.setParameter("benefitingServiceRoleTemplateId", benefitingServiceRoleTemplateId);
		if (facilityOrLocationId != null) {
			q.setParameter("facilityOrLocationId", facilityOrLocationId);
		}

		q.setParameter("myUser", AbstractAuditedPersistent.getCurrentUserIdForAudit()) //
				.setParameter("nowUTC", ZonedDateTime.now(ZoneId.of("Z")));

		int recordsModified = q.executeUpdate();
		return recordsModified;
	}

	@Override
	public int bulkDeleteByCriteria(Long benefitingServiceTemplateId, Long benefitingServiceRoleTemplateId,
			Long benefitingServiceId, Long benefitingServiceRoleId) {
		if (benefitingServiceId == null && benefitingServiceRoleId == null && benefitingServiceTemplateId == null
				&& benefitingServiceRoleTemplateId == null)
			throw new IllegalArgumentException("Must specify at least one piece of filtering criteria");

		flush();

		Query q = query("delete from " + VolunteerAssignment.class.getName() + " where id in (select vfa.id from "
				+ VolunteerAssignment.class.getName() + " vfa" //
				+ " left join vfa.benefitingService bs" //
				+ " left join vfa.benefitingServiceRole bsr" //
				+ " left join bs.template bst" //
				+ " left join bsr.template bsrt" //
				+ " where (1=2" //
				+ (benefitingServiceId != null ? " or bs.id = :benefitingServiceId" : "") //
				+ (benefitingServiceTemplateId != null ? " or bst.id = :benefitingServiceTemplateId" : "") //
				+ (benefitingServiceRoleId != null ? " or bsr.id = :benefitingServiceRoleId" : "") //
				+ (benefitingServiceRoleTemplateId != null ? " or bsrt.id = :benefitingServiceRoleTemplateId" : "") //
				+ "))");
		if (benefitingServiceId != null)
			q.setParameter("benefitingServiceId", benefitingServiceId);
		if (benefitingServiceTemplateId != null)
			q.setParameter("benefitingServiceTemplateId", benefitingServiceTemplateId);
		if (benefitingServiceRoleId != null)
			q.setParameter("benefitingServiceRoleId", benefitingServiceRoleId);
		if (benefitingServiceRoleTemplateId != null)
			q.setParameter("benefitingServiceRoleTemplateId", benefitingServiceRoleTemplateId);
		return q.executeUpdate();
	}

	@Override
	public int bulkDeleteDuplicatesAfterChange(long fromBenefitingServiceRoleId, long toBenefitingServiceRoleId) {
		flush();

		BenefitingServiceRole fromBsr = benefitingServiceRoleDAO.findRequiredByPrimaryKey(fromBenefitingServiceRoleId);
		BenefitingServiceRole bsr = benefitingServiceRoleDAO.findRequiredByPrimaryKey(toBenefitingServiceRoleId);

		return em
				.createQuery("delete from " + VolunteerAssignment.class.getName() + " where id in (" //
						+ "select va.id from " + VolunteerAssignment.class.getName() + " va" //
						+ " where va.benefitingServiceRole = :fromBsr and exists (" //
						+ "		select va2 from " + VolunteerAssignment.class.getName() + " va2" //
						+ "		where va2.volunteer = va.volunteer" //
						+ "		and va2.benefitingServiceRole = :bsr" //
						+ "		and va2.benefitingService = :bs" //
						+ "		and va2.facility = :f" //
						+ "))") //
				.setParameter("bsr", bsr) //
				.setParameter("bs", bsr.getBenefitingService()) //
				.setParameter("f", bsr.getFacility()) //
				.setParameter("fromBsr", fromBsr) //
				.executeUpdate();
	}

	@Override
	public int bulkChangeForBenefitingServiceRoleMerge(long fromBenefitingServiceRoleId,
			long toBenefitingServiceRoleId) {
		flush();

		BenefitingServiceRole fromBsr = benefitingServiceRoleDAO.findRequiredByPrimaryKey(fromBenefitingServiceRoleId);
		BenefitingServiceRole bsr = benefitingServiceRoleDAO.findRequiredByPrimaryKey(toBenefitingServiceRoleId);

		return em
				.createQuery("update " + VolunteerAssignment.class.getName() + " va" //
						+ " set va.benefitingServiceRole = :bsr," //
						+ " va.benefitingService = :bs," //
						+ " va.facility = :f," //
						+ " va.rootFacility = :rootF" //
						+ ", modifiedBy = :myUser, modifiedDate = :nowUTC, version = version + 1" //
						+ " where va.benefitingServiceRole = :fromBsr and not exists (" //
						+ "		select va2 from " + VolunteerAssignment.class.getName() + " va2" //
						+ "		where va2.volunteer = va.volunteer" //
						+ "		and va2.benefitingServiceRole = :bsr" //
						+ "		and va2.benefitingService = :bs" //
						+ "		and va2.facility = :f" //
						+ ")") //
				.setParameter("bsr", bsr) //
				.setParameter("bs", bsr.getBenefitingService()) //
				.setParameter("f", bsr.getFacility()) //
				.setParameter("rootF", bsr.getBenefitingService().getFacility())
				.setParameter("fromBsr", fromBsr) //
				.setParameter("myUser", AbstractAuditedPersistent.getCurrentUserIdForAudit()) //
				.setParameter("nowUTC", ZonedDateTime.now(ZoneId.of("Z"))) //
				.executeUpdate();
	}

	@Override
	public int bulkUpdateBenefitingServiceForRoleMove(long benefitingServiceRoleId) {
		flush();

		BenefitingServiceRole bsr = benefitingServiceRoleDAO.findRequiredByPrimaryKey(benefitingServiceRoleId);

		return em
				.createQuery("update " + VolunteerAssignment.class.getName() + " va" //
						+ " set va.benefitingService = :bs" //
						+ ", modifiedBy = :myUser, modifiedDate = :nowUTC, version = version + 1" //
						+ " where va.benefitingServiceRole = :bsr") //
				.setParameter("bsr", bsr) //
				.setParameter("bs", bsr.getBenefitingService()) //
				.setParameter("myUser", AbstractAuditedPersistent.getCurrentUserIdForAudit()) //
				.setParameter("nowUTC", ZonedDateTime.now(ZoneId.of("Z"))) //
				.executeUpdate();
	}

	@Override
	public int inactivateStaleAssignments(ZonedDateTime cutoffDate, ZonedDateTime gracePeriodCutoff) {
		/* Must flush before any bulk command - CPB */
		flush();

		Query q = query("update " + VolunteerAssignment.class.getName() + " set inactive = true" //
				+ ", modifiedBy = :myUser, modifiedDate = :nowUTC, version = version + 1" //
				+ " where id in (" //
				+ "		select w.volunteerAssignment.id from " + WorkEntry.class.getName() + " w" //
				+ "		where w.volunteerAssignment.inactive = false" //
				+ "		and w.volunteerAssignment.modifiedDate < :gracePeriodCutoff" //
				+ "		group by w.volunteerAssignment.id" //
				+ "		having max(w.dateWorked) < :cutoffDate)" //
				+ " or id in (" //
				+ "		select id from " + VolunteerAssignment.class.getName() + " va" //
				+ "		where va.inactive = false" //
				+ "		and va.volunteer.entryDate < :cutoffDate" //
				+ "		and va.modifiedDate < :gracePeriodCutoff" //
				+ "		and va.workEntries is empty)") //
						.setParameter("cutoffDate", cutoffDate.toLocalDate()) //
						.setParameter("gracePeriodCutoff", gracePeriodCutoff) //
						.setParameter("myUser", AbstractAuditedPersistent.getCurrentUserIdForAudit()) //
						.setParameter("nowUTC", ZonedDateTime.now(ZoneId.of("Z")));

		int recordsModified = q.executeUpdate();
		return recordsModified;
	}
}
