package org.bocogop.shared.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.transaction.annotation.Transactional;

import org.bocogop.shared.persistence.AppUserDAO;
import org.bocogop.shared.persistence.AppUserFacilityDAO;
import org.bocogop.shared.persistence.AppUserFacilityRoleDAO;
import org.bocogop.shared.persistence.AppUserGlobalRoleDAO;
import org.bocogop.shared.persistence.GrantableRoleDAO;
import org.bocogop.shared.persistence.LdapPersonDAO;
import org.bocogop.shared.persistence.lookup.InactiveReasonDAO;
import org.bocogop.shared.persistence.lookup.RoleDAO;
import org.bocogop.shared.persistence.lookup.sds.VAFacilityDAO;

@Transactional
public abstract class AbstractAppServiceImpl {

	@Autowired
	private Environment env;

	@Autowired
	protected AppUserDAO appUserDAO;
	@Autowired
	protected AppUserFacilityDAO appUserFacilityDAO;
	@Autowired
	protected AppUserFacilityRoleDAO appUserFacilityRoleDAO;
	@Autowired
	protected AppUserGlobalRoleDAO appUserGlobalRoleDAO;
	@Autowired
	protected GrantableRoleDAO grantableRoleDAO;
	@Autowired
	protected InactiveReasonDAO inactiveReasonDAO;
	@Autowired
	protected LdapPersonDAO ldapPersonDAO;
	@Autowired
	protected RoleDAO roleDAO;
	@Autowired
	protected VAFacilityDAO vaFacilityDAO;

	protected boolean isUnitTest() {
		return env.acceptsProfiles("default");
	}

}
