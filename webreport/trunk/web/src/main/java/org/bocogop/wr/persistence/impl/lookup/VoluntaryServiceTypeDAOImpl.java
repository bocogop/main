package org.bocogop.wr.persistence.impl.lookup;


import org.springframework.stereotype.Repository;

import org.bocogop.wr.model.voluntaryService.VoluntaryServiceType;
import org.bocogop.wr.persistence.dao.lookup.VoluntaryServiceTypeDAO;
import org.bocogop.wr.persistence.impl.GenericHibernateLookupDAOImpl;

@Repository
public class VoluntaryServiceTypeDAOImpl extends GenericHibernateLookupDAOImpl<VoluntaryServiceType>
		implements VoluntaryServiceTypeDAO {

}
