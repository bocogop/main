package org.bocogop.wr.persistence.impl.lookup;


import org.springframework.stereotype.Repository;

import org.bocogop.wr.model.volunteer.VolunteerStatus;
import org.bocogop.wr.persistence.dao.lookup.VolunteerStatusDAO;
import org.bocogop.wr.persistence.impl.GenericHibernateLookupDAOImpl;

@Repository
public class VolunteerStatusDAOImpl extends GenericHibernateLookupDAOImpl<VolunteerStatus>
		implements VolunteerStatusDAO {

}
