package org.bocogop.wr.persistence.impl.lookup;

import java.util.List;

import org.springframework.stereotype.Repository;

import org.bocogop.wr.model.organization.NACStatus;
import org.bocogop.wr.persistence.dao.lookup.NACStatusDAO;
import org.bocogop.wr.persistence.impl.GenericHibernateDAOImpl;

@Repository
public class NACStatusDAOImpl extends GenericHibernateDAOImpl<NACStatus> implements NACStatusDAO {

	@Override
	public NACStatus findByMembershipStatus(String membershipStatus) {
		@SuppressWarnings("unchecked")
		List<NACStatus> results = query(
				"from " + NACStatus.class.getName() + " where membershipStatus = :membershipStatus")
						.setParameter("membershipStatus", membershipStatus).getResultList();
		return results.isEmpty() ? null : results.get(0);
	}

}
