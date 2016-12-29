package org.bocogop.wr.persistence.impl;

import java.time.LocalDate;
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
import org.bocogop.wr.model.benefitingService.BenefitingService;
import org.bocogop.wr.model.benefitingService.BenefitingServiceRole;
import org.bocogop.wr.model.facility.Facility;
import org.bocogop.wr.model.facility.Location;
import org.bocogop.wr.model.time.OccasionalWorkEntry;
import org.bocogop.wr.model.volunteer.Volunteer;
import org.bocogop.wr.persistence.dao.OccasionalWorkEntryDAO;
import org.bocogop.wr.persistence.dao.benefitingService.BenefitingServiceRoleDAO;
import org.bocogop.wr.persistence.queryCustomization.QueryCustomization;
import org.bocogop.wr.persistence.queryCustomization.fieldTypes.FacilityAssociationFieldType;
import org.bocogop.wr.persistence.queryCustomization.fieldTypes.OccasionalWorkEntryAssociationFieldType;

@Repository
public class OccasionalWorkEntryDAOImpl extends GenericHibernateSortedDAOImpl<OccasionalWorkEntry>
		implements OccasionalWorkEntryDAO {
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(OccasionalWorkEntryDAOImpl.class);

	@Autowired
	private BenefitingServiceRoleDAO benefitingServiceRoleDAO;

	@SuppressWarnings("unchecked")
	@Override
	public List<OccasionalWorkEntry> findByCriteria(LocalDate onOrAfterDate, LocalDate onOrBeforeDate,
			Long organizationId, Long benefitingServiceId, Long benefitingServiceRoleId, Long facilityId,
			QueryCustomization... customization) {
		StringBuilder sb = new StringBuilder("select o from ").append(OccasionalWorkEntry.class.getName()).append(" o");

		QueryCustomization cust = ArrayUtils.isEmpty(customization) ? new QueryCustomization() : customization[0];
		if (facilityId != null) {
			cust.appendRequiredJoin(sb, false, "o", OccasionalWorkEntryAssociationFieldType.FACILITY, "f");
			cust.appendRequiredJoin(sb, true, "f", FacilityAssociationFieldType.PARENT, "fp");
		}
		cust.appendRemainingJoins(sb, "o");

		if (cust.getOrderBy() == null)
			cust.setOrderBy("o.dateWorked, o.createdDate");

		List<String> whereClauseItems = new ArrayList<>();
		Map<String, Object> params = new HashMap<>();

		if (onOrAfterDate != null) {
			whereClauseItems.add("o.dateWorked >= :onOrAfterDate");
			params.put("onOrAfterDate", onOrAfterDate);
		}

		if (onOrBeforeDate != null) {
			whereClauseItems.add("o.dateWorked <= :onOrBeforeDate");
			params.put("onOrBeforeDate", onOrBeforeDate);
		}

		if (organizationId != null) {
			whereClauseItems.add("o.organization.id = :organizationId");
			params.put("organizationId", organizationId);
		}

		if (benefitingServiceId != null) {
			whereClauseItems.add("o.benefitingService.id = :benefitingServiceId");
			params.put("benefitingServiceId", benefitingServiceId);
		}

		if (benefitingServiceRoleId != null) {
			whereClauseItems.add("o.benefitingServiceRole.id = :benefitingServiceRoleId");
			params.put("benefitingServiceRoleId", benefitingServiceRoleId);
		}

		if (facilityId != null) {
			whereClauseItems.add(
					"((TYPE(f) = :facilityClass and f.id = :facilityId) or (TYPE(f) = :locationClass and fp.id = :facilityId))");
			params.put("facilityId", facilityId);
			params.put("facilityClass", Facility.class);
			params.put("locationClass", Location.class);
		}

		Query q = constructQuery(em, sb, whereClauseItems, params, null, cust);

		return q.getResultList();
	}

	public boolean existsForCriteria(Long benefitingServiceTemplateId, Long benefitingServiceRoleTemplateId,
			Long benefitingServiceId, Long benefitingServiceRoleId) {
		if (benefitingServiceTemplateId == null && benefitingServiceRoleTemplateId == null
				&& benefitingServiceId == null && benefitingServiceRoleId == null)
			throw new IllegalArgumentException("Must specify at least one piece of filtering criteria");

		Query q = query("select case when exists (select w from " + OccasionalWorkEntry.class.getName() + " w" //
				+ " left join w.benefitingService bs" //
				+ " left join w.benefitingServiceRole bsr" //
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

	@Override
	public int bulkMove(long fromBenefitingServiceRoleId, long toBenefitingServiceRoleId) {
		flush();

		BenefitingServiceRole toServiceRole = benefitingServiceRoleDAO
				.findRequiredByPrimaryKey(toBenefitingServiceRoleId);
		BenefitingService toService = toServiceRole.getBenefitingService();

		Query q = query("update " + OccasionalWorkEntry.class.getName() //
				+ " set benefitingService = :toService, benefitingServiceRole = :toServiceRole" //
				+ ", modifiedBy = :myUser, modifiedDate = :nowUTC, version = version + 1" // "
				+ " where id in (select bs.id from " + OccasionalWorkEntry.class.getName() //
				+ " bs where bs.benefitingServiceRole.id = :fromServiceRoleId)");

		q.setParameter("toService", toService);
		q.setParameter("toServiceRole", toServiceRole);
		q.setParameter("fromServiceRoleId", fromBenefitingServiceRoleId);

		return q.setParameter("myUser", AbstractAuditedPersistent.getCurrentUserIdForAudit()) //
				.setParameter("nowUTC", ZonedDateTime.now(ZoneId.of("Z"))) //
				.executeUpdate();
	}

	@Override
	public int bulkUpdateBenefitingServiceForRoleMove(long benefitingServiceRoleId) {
		flush();

		BenefitingServiceRole role = benefitingServiceRoleDAO.findRequiredByPrimaryKey(benefitingServiceRoleId);
		BenefitingService service = role.getBenefitingService();

		Query q = query("update " + OccasionalWorkEntry.class.getName() //
				+ " set benefitingService = :service" //
				+ ", modifiedBy = :myUser, modifiedDate = :nowUTC, version = version + 1" // "
				+ " where id in (select bs.id from " + OccasionalWorkEntry.class.getName() //
				+ " bs where bs.benefitingServiceRole = :role)");

		q.setParameter("service", service);
		q.setParameter("role", role);

		return q.setParameter("myUser", AbstractAuditedPersistent.getCurrentUserIdForAudit()) //
				.setParameter("nowUTC", ZonedDateTime.now(ZoneId.of("Z"))) //
				.executeUpdate();
	}

}
