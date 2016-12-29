package org.bocogop.wr.persistence.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import org.bocogop.wr.model.facility.StationParameters;
import org.bocogop.wr.persistence.dao.StationParametersDAO;

@Repository
public class StationParametersDAOImpl extends GenericHibernateDAOImpl<StationParameters>
		implements StationParametersDAO {
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(StationParametersDAOImpl.class);

}
