package org.bocogop.wr.persistence.impl.facility;

import org.springframework.stereotype.Repository;

import org.bocogop.wr.model.facility.AbstractUpdateableLocation;
import org.bocogop.wr.persistence.dao.facility.UpdateableLocationDAO;
import org.bocogop.wr.persistence.impl.GenericHibernateSortedDAOImpl;

@Repository
public class UpdateableLocationDAOImpl extends GenericHibernateSortedDAOImpl<AbstractUpdateableLocation<?>>
		implements UpdateableLocationDAO {

}
