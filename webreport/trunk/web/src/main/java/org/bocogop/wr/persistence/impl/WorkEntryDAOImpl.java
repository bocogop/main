package org.bocogop.wr.persistence.impl;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.persistence.Query;

import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import com.google.common.collect.Lists;

import org.bocogop.shared.model.core.AbstractAuditedPersistent;
import org.bocogop.wr.model.facility.Facility;
import org.bocogop.wr.model.facility.Location;
import org.bocogop.wr.model.time.WorkEntry;
import org.bocogop.wr.model.volunteer.Volunteer;
import org.bocogop.wr.model.volunteer.VolunteerAssignment;
import org.bocogop.wr.persistence.dao.WorkEntryDAO;
import org.bocogop.wr.persistence.queryCustomization.QueryCustomization;
import org.bocogop.wr.persistence.queryCustomization.fieldTypes.FacilityAssociationFieldType;
import org.bocogop.wr.persistence.queryCustomization.fieldTypes.VolunteerAssignmentAssociationFieldType;
import org.bocogop.wr.persistence.queryCustomization.fieldTypes.WorkEntryAssociationFieldType;

@Repository
public class WorkEntryDAOImpl extends GenericHibernateSortedDAOImpl<WorkEntry> implements WorkEntryDAO {
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(WorkEntryDAOImpl.class);

	@SuppressWarnings("unchecked")
	@Override
	public List<WorkEntry> findByCriteria(Long volunteerId, Long volunteerAssignmentId, Long facilityId,
			Long organizationId, LocalDate date, LocalDate fiscalYearAsOfDate, QueryCustomization... customization) {
		StringBuilder sb = new StringBuilder("select o from ").append(WorkEntry.class.getName()).append(" o");

		QueryCustomization cust = ArrayUtils.isEmpty(customization) ? new QueryCustomization() : customization[0];
		cust.appendRequiredJoin(sb, false, "o", WorkEntryAssociationFieldType.VOLUNTEER_ASSIGNMENT, "va");
		if (facilityId != null) {
			cust.appendRequiredJoin(sb, false, "va", VolunteerAssignmentAssociationFieldType.FACILITY, "f");
			cust.appendRequiredJoin(sb, true, "f", FacilityAssociationFieldType.PARENT, "fp");
		}
		cust.appendRemainingJoins(sb, "o");

		if (cust.getOrderBy() == null)
			cust.setOrderBy("o.dateWorked");

		List<String> whereClauseItems = new ArrayList<>();
		Map<String, Object> params = new HashMap<>();

		if (volunteerId != null) {
			whereClauseItems.add("va.volunteer.id = :volunteerId");
			params.put("volunteerId", volunteerId);
		}

		if (volunteerAssignmentId != null) {
			whereClauseItems.add("va.id = :volunteerAssignmentId");
			params.put("volunteerAssignmentId", volunteerAssignmentId);
		}

		if (facilityId != null) {
			whereClauseItems.add(
					"((TYPE(f) = :facilityType and f.id = :facilityId) or (TYPE(f) = :locationType and fp.id = :facilityId))");
			params.put("facilityId", facilityId);
			params.put("facilityType", Facility.class);
			params.put("locationType", Location.class);
		}

		if (organizationId != null) {
			whereClauseItems.add("o.organization.id = :organizationId");
			params.put("organizationId", organizationId);
		}

		if (date != null) {
			whereClauseItems.add("o.dateWorked = :date");
			params.put("date", date);
		}

		if (fiscalYearAsOfDate != null) {
			whereClauseItems.add("o.dateWorked >= :startDate");
			whereClauseItems.add("o.dateWorked <= :endDate");
			params.put("startDate", dateUtil.getFiscalYearStartDateForDate(fiscalYearAsOfDate));
			params.put("endDate", dateUtil.getFiscalYearEndDateForDate(fiscalYearAsOfDate));
		}

		Query q = constructQuery(em, sb, whereClauseItems, params, null, cust);

		return q.getResultList();
	}

	@Override
	public int getNumYearsWorked(long volunteerId) {
		Number n = (Number) em
				.createNativeQuery("select YearsWorked from VolunteerYearsWorked where VolunteerId = :volunteerId")
				.setParameter("volunteerId", volunteerId).getSingleResult();
		return n == null ? 0 : n.intValue();
	}

	@Override
	public Map<Long, LocalDate> getMostRecentVolunteeredDateByFacilityMap(long volunteerId) {
		Map<Long, LocalDate> results = new TreeMap<>();
		@SuppressWarnings("unchecked")
		List<Object[]> queryResults = query("select w.volunteerAssignment.facility.id, max(w.dateWorked) from " //
				+ WorkEntry.class.getName() //
				+ " w where w.volunteerAssignment.volunteer.id = :volunteerId" //
				+ " group by w.volunteerAssignment.facility.id").setParameter("volunteerId", volunteerId)
						.getResultList();
		for (Object[] row : queryResults) {
			results.put(((Number) row[0]).longValue(), (LocalDate) row[1]);
		}
		return results;
	}

	public boolean existsForCriteria(Long benefitingServiceTemplateId, Long benefitingServiceRoleTemplateId,
			Long benefitingServiceId, Long benefitingServiceRoleId) {
		if (benefitingServiceId == null && benefitingServiceRoleId == null && benefitingServiceTemplateId == null
				&& benefitingServiceRoleTemplateId == null)
			throw new IllegalArgumentException("Must specify at least one piece of filtering criteria");

		Query q = query("select case when exists (select w from " + WorkEntry.class.getName() + " w" //
				+ " join w.volunteerAssignment va" //
				+ " left join va.benefitingService bs" //
				+ " left join va.benefitingServiceRole bsr" //
				+ " left join bs.template bst" //
				+ " left join bsr.template bsrt" //
				+ " where (1=2" //
				+ (benefitingServiceId != null ? " or bs.id = :benefitingServiceId" : "") //
				+ (benefitingServiceTemplateId != null ? " or bst.id = :benefitingServiceTemplateId" : "") //
				+ (benefitingServiceRoleId != null ? " or bsr.id = :benefitingServiceRoleId" : "") //
				+ (benefitingServiceRoleTemplateId != null ? " or bsrt.id = :benefitingServiceRoleTemplateId" : "") //
				+ ")) then true else false end from " + Volunteer.class.getName()).setMaxResults(1);

		if (benefitingServiceId != null)
			q.setParameter("benefitingServiceId", benefitingServiceId);
		if (benefitingServiceTemplateId != null)
			q.setParameter("benefitingServiceTemplateId", benefitingServiceTemplateId);
		if (benefitingServiceRoleId != null)
			q.setParameter("benefitingServiceRoleId", benefitingServiceRoleId);
		if (benefitingServiceRoleTemplateId != null)
			q.setParameter("benefitingServiceRoleTemplateId", benefitingServiceRoleTemplateId);

		Boolean b = (Boolean) q.getSingleResult();
		return b;
	}

	public int bulkChangeForBenefitingServiceRoleMerge(long fromBenefitingServiceRoleId,
			long toBenefitingServiceRoleId) {
		flush();

		return em
				.createNativeQuery("update h" //
						+ " set WrVolunteerAssignmentsFK = toVa.id" //
						+ "		, MODIFIED_BY = :myUser, MODIFIED_DATE = :nowUTC, Ver = h.Ver + 1"
						+ " from wr.VolunteerAssignments fromVa" //
						+ " 	join wr.Hours h on h.WrVolunteerAssignmentsFK = fromVa.id" //
						+ " 	cross join wr.VolunteerAssignments toVa" //
						+ " where fromVa.WrVolunteersFK = toVa.WrVolunteersFK" //
						+ " 	and fromVa.WrBenefitingServiceRolesFK = :fromBenefitingServiceRoleId" //
						+ " 	and toVa.WrBenefitingServiceRolesFK = :toBenefitingServiceRoleId") //
				.setParameter("fromBenefitingServiceRoleId", fromBenefitingServiceRoleId) //
				.setParameter("toBenefitingServiceRoleId", toBenefitingServiceRoleId) //
				.setParameter("myUser", AbstractAuditedPersistent.getCurrentUserIdForAudit()) //
				.setParameter("nowUTC", ZonedDateTime.now(ZoneId.of("Z"))) //
				.executeUpdate();
	}

	@Override
	public Map<Long, Integer> countByVolunteerAssignmentIds(List<Long> volunteerAssignmentIds) {
		String q = "select b.id, sum(w.hoursWorked) from " + WorkEntry.class.getName()
				+ " w join w.volunteerAssignment b where b.id in (:ids) group by b.id";

		Map<Long, Integer> results = new HashMap<>();
		for (List<Long> batchChunk : Lists.partition(new ArrayList<>(volunteerAssignmentIds), 2000)) {
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
	public Map<Long, Double> countByVolunteerAndBasicOrganizations(long volunteerId, List<Long> basicOrganizationIds) {
		// select WrOrganizationsFK, sum(hoursWorked) from wr.Hours where
		// WrOrganizationsFK in (67,43,539887)
		// and WrVolunteerAssignmentsFK in (select id from
		// wr.VolunteerAssignments where WrVolunteersFK = 662809)
		// group by WrOrganizationsFK

		String q = "select o.id, sum(w.hoursWorked) from " + WorkEntry.class.getName() //
				+ " w join w.organization o where o.id in (:ids) and w.volunteerAssignment.id in (select id from "
				+ VolunteerAssignment.class.getName() + " a where a.volunteer.id = :volunteerId)" //
				+ " group by o.id";

		Map<Long, Double> results = new HashMap<>();
		for (List<Long> batchChunk : Lists.partition(new ArrayList<>(basicOrganizationIds), 2000)) {
			@SuppressWarnings("unchecked")
			List<Object[]> queryResults = query(q) //
					.setParameter("volunteerId", volunteerId) //
					.setParameter("ids", batchChunk).getResultList();
			for (Object[] r : queryResults) {
				double hoursWorked = r[1] == null ? 0 : ((Number) r[1]).doubleValue();
				results.put(((Number) r[0]).longValue(), hoursWorked);
			}
		}

		return results;
	}

}
