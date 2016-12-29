package org.bocogop.shared.persistence;

import java.util.List;

import org.bocogop.shared.model.GrantableRole;
import org.bocogop.shared.model.Role;

public interface GrantableRoleDAO extends AppDAO<GrantableRole> {

	List<Role> findGrantableRolesForRole(long roleId);

	List<Role> findAllGrantableRolesForUser(long appUserId);
	
}
