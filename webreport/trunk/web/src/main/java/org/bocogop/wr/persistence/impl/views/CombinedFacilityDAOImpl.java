package org.bocogop.wr.persistence.impl.views;

import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import org.bocogop.wr.model.views.CombinedFacility;
import org.bocogop.wr.persistence.dao.views.CombinedFacilityDAO;
import org.bocogop.wr.persistence.impl.GenericHibernateSortedDAOImpl;

@Repository
public class CombinedFacilityDAOImpl extends GenericHibernateSortedDAOImpl<CombinedFacility>
		implements CombinedFacilityDAO {
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(CombinedFacilityDAOImpl.class);

	@Override
	public SortedSet<CombinedFacility> findActiveWithLinkToVAFacility() {
		@SuppressWarnings("unchecked")
		List<CombinedFacility> results = query(
				"select i from " + CombinedFacility.class.getName() + " i where vaFacility is not null and i.inactive = false")
						.getResultList();
		return new TreeSet<>(results);
	}

}
