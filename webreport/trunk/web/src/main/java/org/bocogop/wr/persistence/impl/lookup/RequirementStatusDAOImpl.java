package org.bocogop.wr.persistence.impl.lookup;


import org.springframework.stereotype.Repository;

import org.bocogop.wr.model.requirement.RequirementStatus;
import org.bocogop.wr.persistence.dao.lookup.RequirementStatusDAO;
import org.bocogop.wr.persistence.impl.GenericHibernateLookupDAOImpl;

@Repository
public class RequirementStatusDAOImpl extends GenericHibernateLookupDAOImpl<RequirementStatus>
		implements RequirementStatusDAO {

}
