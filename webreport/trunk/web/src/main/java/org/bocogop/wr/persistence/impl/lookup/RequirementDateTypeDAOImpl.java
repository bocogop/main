package org.bocogop.wr.persistence.impl.lookup;


import org.springframework.stereotype.Repository;

import org.bocogop.wr.model.requirement.RequirementDateType;
import org.bocogop.wr.persistence.dao.lookup.RequirementDateTypeDAO;
import org.bocogop.wr.persistence.impl.GenericHibernateLookupDAOImpl;

@Repository
public class RequirementDateTypeDAOImpl extends GenericHibernateLookupDAOImpl<RequirementDateType>
		implements RequirementDateTypeDAO {

}
