package org.bocogop.wr.persistence.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import org.bocogop.wr.model.volunteer.ShirtSize;
import org.bocogop.wr.persistence.dao.ShirtSizeDAO;

@Repository
public class ShirtSizeDAOImpl extends GenericHibernateSortedDAOImpl<ShirtSize> implements ShirtSizeDAO {
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(ShirtSizeDAOImpl.class);

}
