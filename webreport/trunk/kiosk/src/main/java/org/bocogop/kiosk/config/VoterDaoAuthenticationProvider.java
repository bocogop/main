package org.bocogop.kiosk.config;

import org.bocogop.kiosk.service.VoterUserDetailsProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.AbstractUserDetailsAuthenticationProvider;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Transactional;

public class VoterDaoAuthenticationProvider extends AbstractUserDetailsAuthenticationProvider {

	@Autowired
	private VoterUserDetailsProvider voterUserDetailsProvider;

	public VoterDaoAuthenticationProvider() {
		setHideUserNotFoundExceptions(false);
	}

	@Override
	protected void additionalAuthenticationChecks(UserDetails userDetails,
			UsernamePasswordAuthenticationToken authentication) throws AuthenticationException {
	}

	@Override
	@Transactional(readOnly = true)
	protected UserDetails retrieveUser(String username, UsernamePasswordAuthenticationToken authentication)
			throws AuthenticationException {
		UserDetails v = voterUserDetailsProvider.retrieveUser(username, authentication);
		return v;
	}

}
