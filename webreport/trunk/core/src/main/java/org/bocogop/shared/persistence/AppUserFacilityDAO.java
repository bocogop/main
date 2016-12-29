package org.bocogop.shared.persistence;

import java.util.Collection;
import java.util.List;

import org.bocogop.shared.model.AppUserFacility;
import org.bocogop.shared.model.lookup.sds.VAFacility;

public interface AppUserFacilityDAO extends AppDAO<AppUserFacility> {

	void bulkAdd(final long userId, Collection<Long> facilityIdsToAdd, boolean rolesCustomizedForFacilities);

	List<AppUserFacility> findByUserSorted(long userId);

	VAFacility findPrimaryFacilityForUser(long userId);

	void savePrimaryFacilityForUser(long userId, long primaryFacilityId);

	int deleteByVAFacilityIDs(long appUserId, Collection<Long> vaFacilityIDs);
	
	int deleteByUsers(Collection<Long> userIDs);

}
