package org.bocogop.shared.persistence.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Query;

import org.bocogop.shared.model.AbstractAuditedPersistent;
import org.bocogop.shared.model.Participation;
import org.bocogop.shared.persistence.dao.ParticipationDAO;
import org.bocogop.shared.persistence.queryCustomization.QueryCustomization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

@Repository
public class ParticipationDAOImpl extends GenericHibernateDAOImpl<Participation> implements ParticipationDAO {
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(ParticipationDAOImpl.class);

	@Override
	public List<Participation> findByCriteria(Long voterId, Long eventId) {
		if (voterId == null && eventId == null)
			throw new IllegalArgumentException("No restrictions entered");

		StringBuilder sb = new StringBuilder("select v from ").append(Participation.class.getName()).append(" v");

		/* Don't bother with this yet - CPB */
		QueryCustomization cust = new QueryCustomization();

		List<String> whereClauseItems = new ArrayList<>();
		Map<String, Object> params = new HashMap<>();

		if (voterId != null) {
			whereClauseItems.add("v.voter.id = :voterId");
			params.put("voterId", voterId);
		}

		if (eventId != null) {
			whereClauseItems.add("v.event.id = :eventId");
			params.put("eventId", eventId);
		}

		if (cust.getOrderBy() == null)
			cust.setOrderBy("v.event.date, v.voter.lastName, v.voter.firstName, v.voter.middleName, v.voter.suffix");

		Query q = constructQuery(em, sb, whereClauseItems, params, null, cust);

		@SuppressWarnings("unchecked")
		List<Participation> resultList = q.getResultList();
		return resultList;
	}

	@Override
	public int logParticipation(long voterId, long eventId) {
		return em.createNativeQuery(
				"INSERT INTO [dbo].[Participation](EventFK, VoterFK, CreatedBy, CreatedDate, ModifiedBy, ModifiedDate, Ver)" //
						+ "	select :eventId, :voterId, :userName, SYSUTCDATETIME(), :userName, SYSUTCDATETIME(), 1" //
						+ " where not exists (select * from dbo.Participation where EventFK = :eventId and VoterFK = :voterId)") //
				.setParameter("voterId", voterId) //
				.setParameter("eventId", eventId) //
				.setParameter("userName", AbstractAuditedPersistent.getCurrentUserIdForAudit()) //
				.executeUpdate();
	}

}
