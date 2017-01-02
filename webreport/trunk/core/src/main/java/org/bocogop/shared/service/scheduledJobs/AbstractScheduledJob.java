package org.bocogop.shared.service.scheduledJobs;

import java.util.Collection;
import java.util.concurrent.Callable;

import org.apache.commons.collections15.CollectionUtils;
import org.bocogop.shared.model.AppUser;
import org.bocogop.shared.model.Permission.PermissionType;
import org.bocogop.shared.model.Role.RoleType;
import org.bocogop.shared.model.SystemUserDetails;
import org.bocogop.shared.persistence.AppUserDAO;
import org.bocogop.shared.persistence.dao.ApplicationParametersDAO;
import org.bocogop.shared.persistence.dao.precinct.PrecinctDAO;
import org.bocogop.shared.service.AppUserService;
import org.bocogop.shared.service.voter.VoterService;
import org.bocogop.shared.util.SecurityUtil;
import org.bocogop.shared.util.context.BasicContextManager;
import org.bocogop.shared.util.context.SessionUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.PlatformTransactionManager;

public abstract class AbstractScheduledJob {

	@Autowired
	protected ApplicationParametersDAO applicationParametersDAO;
	@Autowired
	protected AppUserDAO appUserDAO;
	@Autowired
	protected AppUserService appUserService;
	@Autowired
	protected PrecinctDAO precinctDAO;
	@Autowired
	protected PrecinctDAO vAPrecinctDAO;
	@Autowired
	protected VoterService voterService;

	@Autowired
	@Qualifier("transactionManager")
	protected PlatformTransactionManager tm;

	protected AppUser queryUser() {
		return appUserDAO.findByUsername(SecurityUtil.getCurrentUserName(), false);
	}

	protected <T> void runAsBatchJobUser(Callable<T> c) throws Exception {
		Collection<GrantedAuthority> allRolesAndPermissions = CollectionUtils
				.union(PermissionType.getAllAsGrantedAuthorities(), RoleType.getAllAsGrantedAuthorities());
		SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
				new SystemUserDetails(allRolesAndPermissions), "unimportant", allRolesAndPermissions));

		BasicContextManager contextManager = new BasicContextManager();

		SessionUtil.runWithContext(c, contextManager);
	}

}
