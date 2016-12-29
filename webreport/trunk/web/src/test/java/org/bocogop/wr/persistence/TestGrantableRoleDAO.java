package org.bocogop.wr.persistence;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.transaction.annotation.Transactional;

import org.bocogop.shared.model.AppUser;
import org.bocogop.shared.model.Role;
import org.bocogop.shared.model.Role.RoleType;
import org.bocogop.wr.AbstractTransactionalWebTest;

public class TestGrantableRoleDAO extends AbstractTransactionalWebTest {

	@Test
	@Transactional
	public void testFindAllGrantableRolesForUserId() {
		Long userId = null;
		List<AppUser> allUsers = appUserDAO.findAll();
		for (AppUser u : allUsers) {
			if (u.hasGlobalRole(RoleType.NATIONAL_ADMIN)) {
				userId = u.getId();
				break;
			}
		}
		
		Assert.assertNotNull(userId);
		// test get all grantable roles by user id
		List<Role> roleList = grantableRoleDAO.findAllGrantableRolesForUser(userId);
		Assert.assertFalse("Failed search for Grantable Roles - ", roleList.isEmpty());
	}

	@Test
	public void testFindAllGrantableRolesFoRoleId() {
		Long roleId = roleDAO.findSome(1).get(0).getId();
		// test get all grantable roles by user id
		grantableRoleDAO.findGrantableRolesForRole(roleId);
	}

}
