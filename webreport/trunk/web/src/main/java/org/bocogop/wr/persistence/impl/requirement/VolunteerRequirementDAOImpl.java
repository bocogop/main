package org.bocogop.wr.persistence.impl.requirement;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;

import javax.persistence.Query;

import org.apache.commons.lang3.ArrayUtils;
import org.hibernate.Session;
import org.hibernate.jdbc.Work;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import org.bocogop.shared.model.core.AbstractAuditedPersistent;
import org.bocogop.wr.model.requirement.AbstractVolunteerRequirement;
import org.bocogop.wr.model.requirement.GlobalRequirement;
import org.bocogop.wr.model.requirement.GlobalRoleRequirement;
import org.bocogop.wr.model.requirement.GlobalRoleTypeRequirement;
import org.bocogop.wr.model.requirement.RequirementAvailableStatus;
import org.bocogop.wr.model.requirement.RequirementStatus.RequirementStatusValue;
import org.bocogop.wr.model.requirement.VolunteerRequirement;
import org.bocogop.wr.model.views.VolunteerRequirementActive;
import org.bocogop.wr.model.volunteer.Volunteer;
import org.bocogop.wr.model.volunteer.VolunteerAssignment;
import org.bocogop.wr.model.volunteer.VolunteerStatus.VolunteerStatusType;
import org.bocogop.wr.persistence.dao.lookup.RequirementStatusDAO;
import org.bocogop.wr.persistence.dao.requirement.VolunteerRequirementDAO;
import org.bocogop.wr.persistence.impl.GenericHibernateDAOImpl;
import org.bocogop.wr.persistence.queryCustomization.QueryCustomization;

@Repository
public class VolunteerRequirementDAOImpl extends GenericHibernateDAOImpl<VolunteerRequirement>
		implements VolunteerRequirementDAO {
	private static final Logger log = LoggerFactory.getLogger(VolunteerRequirementDAOImpl.class);

	@Autowired
	private RequirementStatusDAO requirementStatusDAO;

	static class MyWork implements Work {
		public int previousIsolationLevel;

		@Override
		public void execute(Connection connection) throws SQLException {
			previousIsolationLevel = connection.getTransactionIsolation();
			connection.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
		}

	}

	private <T> T runWithUncommittedTransactionIsolation(Callable<T> c) throws Exception {
		MyWork w = new MyWork();
		boolean workCompleted = false;
		try {
			em.unwrap(Session.class).doWork(w);
			workCompleted = true;
		} catch (Exception e) {
			log.warn("Couldn't set transaction isolation level", e);
		}

		try {
			return c.call();
		} finally {
			if (workCompleted) {
				em.unwrap(Session.class).doWork(new Work() {
					@Override
					public void execute(Connection connection) throws SQLException {
						connection.setTransactionIsolation(w.previousIsolationLevel);
					}
				});
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends AbstractVolunteerRequirement> List<T> findByCriteria(Class<T> clazz, long volunteerId,
			Long facilityId, QueryCustomization... customization) {
		try {
			return runWithUncommittedTransactionIsolation(new Callable<List<T>>() {
				@Override
				public List<T> call() throws Exception {
					List<String> whereClauseItems = new ArrayList<>();
					Map<String, Object> params = new HashMap<>();

					StringBuilder sb = new StringBuilder("select vr from ").append(clazz.getName())
							.append(" vr join vr.volunteer v ") //
							.append("join fetch vr.requirement r ") //
							.append("left join fetch r.facility f ");

					if (facilityId != null) {
						whereClauseItems.add("(f.id is null or f.id = :facilityId)");
						params.put("facilityId", facilityId);
					}

					whereClauseItems.add("v.id = :volunteerId ");
					params.put("volunteerId", volunteerId);

					whereClauseItems.add("r.inactive = false");

					QueryCustomization cust = ArrayUtils.isEmpty(customization) ? new QueryCustomization()
							: customization[0];
					cust.appendRemainingJoins(sb, "vr");

					if (cust.getOrderBy() == null)
						cust.setOrderBy("TYPE(r) desc, r.name");

					Query q = constructQuery(em, sb, whereClauseItems, params, null, cust);
					if (cust.getRowLimitation() != null)
						q.setMaxResults(cust.getRowLimitation());

					return q.getResultList();
				}
			});
		} catch (Exception e) {
			throw new RuntimeException("Unexpected exception", e);
		}
	}

	@Override
	public int bulkAddNecessaryRequirements(Long volunteerIdModified, Long requirementIdModified,
			Long benefitingServiceRoleIdModified, Long benefitingServiceRoleTemplateIdModified) {
		flush();

		Map<String, Object> params = new HashMap<>();
		String volRestrictionCriteria = "";

		if (volunteerIdModified != null) {
			volRestrictionCriteria = "where e.VolunteerFK = :volunteerId";
			params.put("volunteerId", volunteerIdModified);
		} else if (requirementIdModified != null) {
			volRestrictionCriteria = "where e.RequirementFK = :requirementId";
			params.put("requirementId", requirementIdModified);
		} else if (benefitingServiceRoleIdModified != null) {
			volRestrictionCriteria = "join wr.VolunteerAssignments va on va.WrVolunteersFK = e.VolunteerFK" //
					+ " and va.WrBenefitingServiceRolesFK = :benefitingServiceRoleIdModified";
			params.put("benefitingServiceRoleIdModified", benefitingServiceRoleIdModified);
		} else if (benefitingServiceRoleTemplateIdModified != null) {
			volRestrictionCriteria = "join wr.VolunteerAssignments va on va.WrVolunteersFK = e.VolunteerFK" //
					+ "	join wr.BenefitingServiceRoles bsr on va.WrBenefitingServiceRolesFK = bsr.id" //
					+ "		and bsr.BenefitingServiceRoleTemplatesFK = :benefitingServiceRoleTemplateIdModified";
			params.put("benefitingServiceRoleTemplateIdModified", benefitingServiceRoleTemplateIdModified);
		} else {
			throw new IllegalArgumentException("Must specify at least one restriction");
		}

		String q = "with scoped_requirements as (" //
				+ "		select e.VolunteerFK, e.RequirementFK" //
				+ "		from VolunteerRequirementsExpected e " //
				+ volRestrictionCriteria //
				+ " )" //
				+ " merge [wr].[VolunteerRequirement] as vr" //
				+ " using scoped_requirements mr" //
				+ " on vr.VolunteerFK = mr.VolunteerFK" //
				+ " 	and vr.RequirementFK = mr.RequirementFK" //
				+ " when not matched by target" //
				+ " then insert ([VolunteerFK]" //
				+ "			,[RequirementFK]" //
				+ "			,[WR_STD_RequirementStatusFK]" //
				+ "			,[RequirementDate]" //
				+ "			,[IsInactive]" //
				+ "			,[Comments]" //
				+ "			,[Ver]" //
				+ "			,[CREATED_BY]" //
				+ "			,[CREATED_DATE]" //
				+ "			,[MODIFIED_BY]" //
				+ "			,[MODIFIED_DATE])" //
				+ "		values (mr.VolunteerFK," //
				+ "			mr.RequirementFK," //
				+ "			:newStatusId," //
				+ "			null," //
				// + " SYSUTCDATETIME()," //
				+ "			0," //
				+ "			null," //
				+ "			0," //
				+ "			:changeUser," //
				+ "			SYSUTCDATETIME()," //
				+ "			:changeUser," //
				+ "			SYSUTCDATETIME());";

		Query query = em.createNativeQuery(q) //
				.setParameter("changeUser", AbstractAuditedPersistent.getCurrentUserIdForAudit()) //
				.setParameter("newStatusId", RequirementStatusValue.NEW.getId());

		for (Entry<String, Object> entry : params.entrySet())
			query.setParameter(entry.getKey(), entry.getValue());

		return query.executeUpdate();
	}

	@Override
	public int bulkUpdateDateToNull(long requirementId) {
		return query("update " + VolunteerRequirement.class.getName()
				+ " set requirementDate = null where requirement.id = :requirementId")
						.setParameter("requirementId", requirementId).executeUpdate();
	}

	@Override
	public Integer removeUnnecessaryVolunteerRequirementsInNewStatus() {
		String queryStr = "delete from wr.VolunteerRequirement where id in (" //
				+ "	select vr.id" //
				+ "	from wr.VolunteerRequirement vr" //
				+ "		left join VolunteerRequirementsExpected vre on vr.RequirementFK = vre.RequirementFK" //
				+ "			and vr.VolunteerFK = vre.VolunteerFK" //
				+ "	where vre.VolunteerFK is null" //
				+ "		and vr.WR_STD_RequirementStatusFK = :newStatus)";
		return em.createNativeQuery(queryStr) //
				.setParameter("newStatus", RequirementStatusValue.NEW.getId()) //
				.executeUpdate();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<AbstractVolunteerRequirement> findUnmetRequirements(long volunteerId, long facilityId) {
		return em
				.createQuery("select vr from " + VolunteerRequirementActive.class.getName() + " vr" //
						+ " join vr.requirement r"
						+ " where vr.status.id <> :notApplicableStatusId" //
						+ " and r.preventTimeposting = true" //
						+ " and (TYPE(r) in (:globalRequirementTypes) or r.facility.id = :facilityId)"
						+ " and vr.volunteer.id = :volunteerId" //
						+ " and (vr.status.id <> :metStatusId or (" //
						+ "		r.dateType.skipNotification = false" //
						+ "		and vr.requirementDate < :today))") //
				.setParameter("metStatusId", RequirementStatusValue.MET.getId()) //
				.setParameter("notApplicableStatusId", RequirementStatusValue.NOT_APPLICABLE.getId()) //
				.setParameter("volunteerId", volunteerId) //
				.setParameter("today", LocalDate.now()) //
				.setParameter("facilityId", facilityId) //
				.setParameter("globalRequirementTypes", Arrays.asList(GlobalRequirement.class,
						GlobalRoleRequirement.class, GlobalRoleTypeRequirement.class))
				.getResultList();
	}

	@Override
	public int countByCriteria(long requirementId) {
		return ((Long) em
				.createQuery("select count(*) from " + VolunteerRequirementActive.class.getName()
						+ " a where a.requirement.id = :requirementId")
				.setParameter("requirementId", requirementId).getSingleResult()).intValue();
	}

	@Override
	public int deleteByCriteria(long requirementId) {
		flush();

		return em
				.createQuery("delete from " + VolunteerRequirement.class.getName() + " where id in (" //
						+ "select va.id from " + VolunteerRequirement.class.getName() + " va" //
						+ " where va.requirement.id = :requirementId)") //
				.setParameter("requirementId", requirementId) //
				.executeUpdate();
	}

	@Override
	public int bulkUpdateInvalidStatusesToNew(long requirementId) {
		return query("update " + VolunteerRequirement.class.getName()
				+ " set status = :newStatus where requirement.id = :requirementId and status not in (select s.status from "
				+ RequirementAvailableStatus.class.getName() + " s where s.requirement.id = :requirementId)")
						.setParameter("requirementId", requirementId)
						.setParameter("newStatus", requirementStatusDAO.findByLookup(RequirementStatusValue.NEW))
						.executeUpdate();
	}

	@Override
	public int updateAllIncorrectStatuses() {
		return em
				.createNativeQuery("update vr" //
						+ "	set IsInactive = case when vre.VolunteerFK is not null then 0 else 1 end" //
						+ "	from wr.VolunteerRequirement vr" //
						+ "	left join dbo.VolunteerRequirementsExpected vre on vr.VolunteerFK = vre.VolunteerFK" //
						+ "		and vr.RequirementFK = vre.RequirementFK" //
						+ "	where IsInactive <> case when vre.VolunteerFK is not null then 0 else 1 end")
				.executeUpdate();
	}

	@Override
	public List<VolunteerRequirement> findForExpiringRequirementsByFacility(long facilityId, int maxResults) {
		try {
			return runWithUncommittedTransactionIsolation(new Callable<List<VolunteerRequirement>>() {
				@Override
				public List<VolunteerRequirement> call() throws Exception {
					LocalDate today = LocalDate.now();

					String jpql = "select vr" //
							+ "	from " + VolunteerRequirement.class.getName() + " vr" //
							+ "		join fetch vr.volunteer" //
							+ "		join fetch vr.requirement " //
							+ "		join fetch vr.status" //
							+ "	where vr.id in (" //
					/*
					 * need to do "where id in (...) so we can enforce a join
					 * order below but also allow ourselves to join fetch what
					 * objects we want above - CPB
					 */
							+ "		select vr1.id" //
							+ "		from " + Volunteer.class.getName() + " v" //
							+ "			join v.volunteerRequirements vr1" //
							+ "			join vr1.requirement r" //
							+ "			join vr1.status s" //
							+ "			left join r.dateType rdt" //
							+ "		where 1=1" //
							+ "			and (" //
							+ "				TYPE(r) in (:globalRequirementTypes) and (" //
							+ "					COALESCE(v.primaryFacility.id, v.originallyCreatedAt.id) = :facilityId OR" //
							+ "					exists (" //
							+ "						select va from " + VolunteerAssignment.class.getName() + " va" //
							+ "						where va.rootFacility.id = :facilityId" //
							+ "							and va.inactive = false" //
							+ "							and va.volunteer = v" //
							+ "					)" //
							+ "				) OR " //
							+ "				r.facility.id = :facilityId" //
							+ "			)" //
							+ "			and r.inactive = false" //
							+ "			and vr1.inactive = false" //
							+ "			and v.status.id = :volActiveStatusId" //
							+ "			and vr1.status.id <> :notApplicableStatusId" //
							+ "			and (vr1.status.id <> :metStatusId or (" //
							+ "				rdt.skipNotification = false and (" //
							+ "					:today > vr1.requirementDate or (" //
							+ "						r.daysNotification is not null and" //
							+ "						day_diff(:today, vr1.requirementDate) <= r.daysNotification))))" //
							+ "	)";
					Query q = query(jpql) //
							.setParameter("volActiveStatusId", VolunteerStatusType.ACTIVE.getId()) //
							.setParameter("notApplicableStatusId", RequirementStatusValue.NOT_APPLICABLE.getId()) //
							.setParameter("metStatusId", RequirementStatusValue.MET.getId()) //
							.setParameter("globalRequirementTypes",
									Arrays.asList(GlobalRequirement.class, GlobalRoleRequirement.class,
											GlobalRoleTypeRequirement.class))
							.setParameter("facilityId", facilityId) //
							.setParameter("today", today) //
							.setMaxResults(maxResults);
					/*
					 * Query optimizer isn't joining correctly - takes 60
					 * seconds to load unless I force it to reduce against the
					 * volunteer table first, then volReqs, then others. Tried
					 * updating stats but it didn't help. CPB
					 */
					q.unwrap(org.hibernate.Query.class).addQueryHint("FORCE ORDER");

					@SuppressWarnings("unchecked")
					List<VolunteerRequirement> volReqs = q.getResultList();
					return volReqs;
				}
			});
		} catch (Exception e) {
			throw new RuntimeException("Unexpected exception", e);
		}
	}
}
