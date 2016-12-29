package org.bocogop.wr.persistence.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import org.bocogop.wr.model.lookup.Template;
import org.bocogop.wr.persistence.dao.TemplateDAO;

@Repository
public class TemplateDAOImpl extends GenericHibernateSortedDAOImpl<Template>implements TemplateDAO {
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(TemplateDAOImpl.class);

}
