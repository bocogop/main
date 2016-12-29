package org.bocogop.shared.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.bocogop.shared.model.AppUser;
import org.bocogop.shared.service.AppUserDetailsService;
import org.bocogop.shared.service.validation.ServiceValidationException;

@Service
@Transactional(rollbackFor = ServiceValidationException.class)
public class AppUserDetailsServiceImpl extends AbstractAppServiceImpl implements AppUserDetailsService {
	private static final Logger log = LoggerFactory.getLogger(AppUserDetailsServiceImpl.class);

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		AppUser appUser = appUserDAO.findByUsername(username, true);
		if (appUser == null)
			throw new UsernameNotFoundException("Sorry, the user with username '" + username + "' could not be found.");
		/*
		 * Need to proactively initialize remaining children that weren't
		 * join-fetched by the DAO so that we can switch stations and all
		 * roles/permissions are already populated - CPB
		 */
		appUser.initializeAll();
		appUser = AppUserServiceImpl.populatePreferencesIfNecessary(appUserDAO, appUser);
		return appUser;
	}

}
