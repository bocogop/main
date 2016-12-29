package org.bocogop.shared.persistence.impl;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;

import org.bocogop.shared.model.AppUser;
import org.bocogop.shared.model.GrantableRole;
import org.bocogop.shared.model.Role;
import org.bocogop.shared.persistence.GrantableRoleDAO;
import org.bocogop.shared.util.cache.CacheNames;

@Repository
public class GrantableRoleDAOImpl extends AbstractAppDAOImpl<GrantableRole> implements GrantableRoleDAO {
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(GrantableRoleDAOImpl.class);

	@SuppressWarnings("unchecked")
	@Override
	@Cacheable(value = CacheNames.QUERIES_GRANTABLE_ROLE_DAO)
	public List<Role> findGrantableRolesForRole(long roleId) {
		return query(
				"select gr.assignableRole from " + GrantableRole.class.getName() + " gr where gr.role.id = :roleId")
						.setParameter("roleId", roleId).getResultList();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Role> findAllGrantableRolesForUser(long appUserId) {
	
		return query("select distinct ar from " + AppUser.class.getName() + " u" //
				+ " join u.globalRoles gr" //
				+ " join gr.role r" //
				+ " join r.grantableRoles grantRole" //
				+ " join grantRole.assignableRole ar" //
				+ " where u.id = :appUserId") //
						.setParameter("appUserId", appUserId).getResultList();
	}
}
