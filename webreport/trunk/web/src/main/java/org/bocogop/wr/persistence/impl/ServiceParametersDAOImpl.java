package org.bocogop.wr.persistence.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import org.bocogop.wr.model.voluntaryService.VoluntaryServiceParameters;
import org.bocogop.wr.persistence.dao.ServiceParametersDAO;

@Repository
public class ServiceParametersDAOImpl extends GenericHibernateDAOImpl<VoluntaryServiceParameters>
		implements ServiceParametersDAO {
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(ServiceParametersDAOImpl.class);

}
