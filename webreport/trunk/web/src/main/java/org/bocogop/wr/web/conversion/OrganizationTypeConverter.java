package org.bocogop.wr.web.conversion;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import org.bocogop.wr.model.organization.OrganizationType;
import org.bocogop.wr.persistence.dao.lookup.OrganizationTypeDAO;

@Component
public class OrganizationTypeConverter extends AbstractStringToPersistentConverter<OrganizationType> {

	@Autowired
	protected OrganizationTypeConverter(OrganizationTypeDAO dao) {
		super(dao);
	}
}
