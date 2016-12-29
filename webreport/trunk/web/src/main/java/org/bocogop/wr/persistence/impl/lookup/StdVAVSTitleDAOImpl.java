package org.bocogop.wr.persistence.impl.lookup;


import org.springframework.stereotype.Repository;

import org.bocogop.wr.model.organization.StdVAVSTitle;
import org.bocogop.wr.persistence.dao.lookup.StdVAVSTitleDAO;
import org.bocogop.wr.persistence.impl.GenericHibernateLookupDAOImpl;

@Repository
public class StdVAVSTitleDAOImpl extends GenericHibernateLookupDAOImpl<StdVAVSTitle>
		implements StdVAVSTitleDAO {

}
