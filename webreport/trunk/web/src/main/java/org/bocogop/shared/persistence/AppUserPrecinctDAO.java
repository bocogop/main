package org.bocogop.shared.persistence;

import java.util.Collection;
import java.util.List;

import org.bocogop.shared.model.AppUserPrecinct;
import org.bocogop.wr.model.precinct.Precinct;

public interface AppUserPrecinctDAO extends AppDAO<AppUserPrecinct> {

	void bulkAdd(final long userId, Collection<Long> precinctIdsToAdd);

	List<AppUserPrecinct> findByUserSorted(long userId);

	Precinct findPrimaryPrecinctForUser(long userId);

	void savePrimaryPrecinctForUser(long userId, long primaryPrecinctId);

	int deleteByPrecinctIDs(long appUserId, Collection<Long> precinctIDs);
	
	int deleteByUsers(Collection<Long> userIDs);

}
