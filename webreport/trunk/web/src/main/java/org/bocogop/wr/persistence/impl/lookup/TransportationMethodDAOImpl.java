package org.bocogop.wr.persistence.impl.lookup;

import org.springframework.stereotype.Repository;

import org.bocogop.wr.model.volunteer.TransportationMethod;
import org.bocogop.wr.persistence.dao.lookup.TransportationMethodDAO;
import org.bocogop.wr.persistence.impl.GenericHibernateLookupDAOImpl;

@Repository
public class TransportationMethodDAOImpl extends GenericHibernateLookupDAOImpl<TransportationMethod>
		implements TransportationMethodDAO {

}
