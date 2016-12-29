package org.bocogop.wr.persistence.impl;

import javax.persistence.NoResultException;

import org.springframework.stereotype.Repository;

import org.bocogop.wr.model.ApplicationParameter;
import org.bocogop.wr.persistence.dao.ApplicationParametersDAO;

@Repository
public class ApplicationParametersDAOImpl extends GenericHibernateDAOImpl<ApplicationParameter>
		implements ApplicationParametersDAO {

	@Override
	public ApplicationParameter findByName(String paramName) {
		try {
			return (ApplicationParameter) query(
					"from " + ApplicationParameter.class.getName() + " where parameterName = :parameterName")
							.setParameter("parameterName", paramName).getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}
}
