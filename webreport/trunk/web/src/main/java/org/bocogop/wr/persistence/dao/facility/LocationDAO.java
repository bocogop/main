package org.bocogop.wr.persistence.dao.facility;

import java.util.Collection;
import java.util.Map;
import java.util.SortedSet;

import org.bocogop.wr.model.facility.Location;
import org.bocogop.wr.persistence.dao.CustomizableSortedDAO;

public interface LocationDAO extends CustomizableSortedDAO<Location> {

	SortedSet<Location> findByCriteria(Long parentFacilityId);

	/**
	 * Return integer array represents {active, total}
	 * @param locationIds
	 * @return
	 */
	Map<Long, Integer[]> countVolunteersForLocations(Collection<Long> locationIds);
	
}
