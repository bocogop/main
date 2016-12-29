package org.bocogop.wr.persistence.impl.views;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

public abstract class AbstractDerivedPersistentDAOImpl {

	@PersistenceContext
	protected EntityManager em;

	protected Query query(String hql) {
		return em.createQuery(hql);
	}

}
