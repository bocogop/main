package org.bocogop.shared.service.impl;

import org.bocogop.shared.persistence.AppUserDAO;
import org.bocogop.shared.persistence.AppUserGlobalRoleDAO;
import org.bocogop.shared.persistence.AppUserPrecinctDAO;
import org.bocogop.shared.persistence.lookup.RoleDAO;
import org.bocogop.wr.persistence.dao.precinct.PrecinctDAO;
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
	protected AppUserPrecinctDAO appUserPrecinctDAO;
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
