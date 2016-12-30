package org.bocogop.wr.persistence;

import java.util.Collection;
import java.util.List;

import org.bocogop.wr.model.AppUserGlobalRole;

public interface AppUserGlobalRoleDAO extends AppDAO<AppUserGlobalRole> {

	void bulkAdd(long userId, Collection<Long> roleIdsToAdd);

	int deleteByRoleIds(long userId, List<Long> roleIds);

	int deleteByUsers(List<Long> userIds);

}
