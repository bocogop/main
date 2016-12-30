package org.bocogop.wr.service.impl;

import java.util.Arrays;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.bocogop.wr.model.AppUser;
import org.bocogop.wr.service.AppUserTestService;
import org.springframework.stereotype.Service;

@Service
public class AppUserTestServiceImpl extends AbstractAppServiceImpl implements AppUserTestService {

	@PersistenceContext
	protected EntityManager em;

	@Override
	public void deleteIfExists(String username) {
		/*
		 * FIXWR may need to run custom JDBC here to delete items in all other
		 * tables that reference this user, specific to the DB schema for this
		 * app - CPB
		 */
		AppUser test = appUserDAO.findByUsername(username, false);
		if (test != null)
			appUserDAO.deleteAll(Arrays.asList(test));
		appUserDAO.flush();
	}

}
