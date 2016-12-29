package org.bocogop.wr.persistence.impl.lookup;

import org.springframework.stereotype.Repository;

import org.bocogop.wr.model.organization.OrganizationType;
import org.bocogop.wr.persistence.dao.lookup.OrganizationTypeDAO;
import org.bocogop.wr.persistence.impl.GenericHibernateLookupDAOImpl;

@Repository
public class OrganizationTypeDAOImpl extends GenericHibernateLookupDAOImpl<OrganizationType>
		implements OrganizationTypeDAO {

}
