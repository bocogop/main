package org.bocogop.wr.persistence.impl;

import org.springframework.stereotype.Repository;

import org.bocogop.wr.model.volunteer.AvailableIdentifyingCode;
import org.bocogop.wr.persistence.dao.AvailableIdentifyingCodeDAO;

@Repository
public class AvailableIdentifyingCodeDAOImpl extends GenericHibernateDAOImpl<AvailableIdentifyingCode>
		implements AvailableIdentifyingCodeDAO {

	@Override
	public AvailableIdentifyingCode getFirstUnused() {
		return (AvailableIdentifyingCode) em
				.createQuery("from " + AvailableIdentifyingCode.class.getName() + " order by id").setMaxResults(1)
				.getSingleResult();
	}

}
