package org.bocogop.shared.persistence.impl;

import javax.persistence.NoResultException;

import org.bocogop.shared.model.ApplicationParameter;
import org.bocogop.shared.persistence.dao.ApplicationParametersDAO;
import org.springframework.stereotype.Repository;

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
