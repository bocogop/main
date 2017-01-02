package org.bocogop.shared.persistence.impl;

import org.bocogop.shared.model.lookup.Template;
import org.bocogop.shared.persistence.dao.TemplateDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

@Repository
public class TemplateDAOImpl extends GenericHibernateSortedDAOImpl<Template>implements TemplateDAO {
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(TemplateDAOImpl.class);

}
