package org.bocogop.wr.persistence.dao.facility;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;

import org.bocogop.shared.model.lookup.sds.VAFacility;
import org.bocogop.shared.persistence.lookup.sds.VAFacilityDAO.QuickSearchResult;
import org.bocogop.wr.model.facility.AbstractLocation;
import org.bocogop.wr.model.facility.Facility;
import org.bocogop.wr.persistence.dao.CustomizableSortedDAO;

/**
 *
 */
public interface FacilityDAO extends CustomizableSortedDAO<Facility> {

	Facility findByVAFacility(long vaFacilityId);

	/**
	 * @param vaFacilityId
	 * @return Map of <VAFacility.id, Facility>
	 */
	Map<Long, Facility> findByVAFacilityIds(Collection<Long> vaFacilityId);

	/**
	 * @param vaFacilities
	 * @return Map of <VAFacility.id, Facility>
	 */
	Map<Long, Facility> findByVAFacilities(Collection<VAFacility> vaFacilities);

	Facility findByStationNumber(String stationNumber);

	SortedSet<VAFacility> findVAFacilitiesWithLinkToFacility();

	SortedSet<Facility> findWithLinkToVAFacility();

	/**
	 * Returns all facilities whose parent is either null or set to themselves
	 */
	SortedSet<Facility> findRootFacilities();

	void updateFieldsWithoutVersionIncrement(long facilityId, boolean setVAFacilityId, Long vaFacilityId,
			boolean setAdministrativeUnitId, Long administrativeUnitId, boolean setStationNumber, String stationNumber);

	<T extends AbstractLocation> SortedSet<T> findByCriteria(Long facilityOrLocationId, Long parentFacilityId,
			Class<T> requiredClassType, Boolean activeStatus);

	List<QuickSearchResult> findUnlinkedMatchingVAFacilities(String searchValue, int length);

}
