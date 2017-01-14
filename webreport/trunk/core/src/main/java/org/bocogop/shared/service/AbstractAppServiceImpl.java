package org.bocogop.shared.service;

import org.bocogop.shared.persistence.AppUserDAO;
import org.bocogop.shared.persistence.AppUserGlobalRoleDAO;
import org.bocogop.shared.persistence.dao.RoleDAO;
import org.bocogop.shared.persistence.dao.precinct.PrecinctDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public abstract class AbstractAppServiceImpl {

	@Autowired
	private Environment env;

	@Autowired
	protected AppUserDAO appUserDAO;
	@Autowired
	protected AppUserGlobalRoleDAO appUserGlobalRoleDAO;
	@Autowired
	protected RoleDAO roleDAO;
	@Autowired
	protected PrecinctDAO precinctDAO;

	protected boolean isUnitTest() {
		return env.acceptsProfiles("default");
	}

}
