package org.bocogop.shared.persistence;

import java.util.Collection;

import org.bocogop.shared.model.AppUserFacilityRole;

public interface AppUserFacilityRoleDAO extends AppDAO<AppUserFacilityRole> {

	void bulkAdd(long appUserId, Collection<Long> roleIDs, Collection<Long> vaFacilityIDs);

	int deleteByVAFacilityIDs(long appUserId, Collection<Long> vaFacilityIDs);

	int deleteByUsers(Collection<Long> userIDs);

}
