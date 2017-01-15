package org.bocogop.wr.service.impl;

import org.bocogop.shared.model.AppUser;
import org.bocogop.shared.model.Permission;
import org.bocogop.shared.service.AppUserService;
import org.bocogop.shared.service.validation.ServiceValidationException;
import org.bocogop.wr.AbstractTransactionalCoreTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.test.context.support.WithMockUser;

public class TestAppUserServiceImpl extends AbstractTransactionalCoreTest {

	@Autowired
	private AppUserService appUserService;

	public void login() throws Exception {
		System.out.println("Skipping login");
	}

	@Test(expected = AuthenticationCredentialsNotFoundException.class)
	public void testSaveOrUpdateAnonymous() throws ServiceValidationException {
		appUserService.saveOrUpdate(new AppUser(), null, null);
	}

	@WithMockUser(authorities = Permission.USER_MANAGER)
	public void testSaveOrUpdateAuthenticated() throws ServiceValidationException {
		appUserService.saveOrUpdate(new AppUser(), null, null);
	}

}
