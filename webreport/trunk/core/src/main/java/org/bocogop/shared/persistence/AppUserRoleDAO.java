package org.bocogop.shared.persistence;

import java.util.Collection;
import java.util.List;

import org.bocogop.shared.model.AppUserRole;

public interface AppUserRoleDAO extends AppDAO<AppUserRole> {

	void bulkAdd(long userId, Collection<Long> roleIdsToAdd);

	int deleteByRoleIds(long userId, List<Long> roleIds);

	int deleteByUsers(List<Long> userIds);

}
