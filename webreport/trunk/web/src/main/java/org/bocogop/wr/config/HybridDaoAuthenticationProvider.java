package org.bocogop.wr.config;

import org.bocogop.shared.model.AppUser;
import org.bocogop.shared.service.AppUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

public class HybridDaoAuthenticationProvider extends DaoAuthenticationProvider {

	@Autowired
	private AppUserDetailsService appUserDetailsService;

	public HybridDaoAuthenticationProvider(PasswordEncoder passwordEncoder) {
		setHideUserNotFoundExceptions(false);
		setPasswordEncoder(passwordEncoder);
	}

	@Override
	protected void additionalAuthenticationChecks(UserDetails userDetails,
			UsernamePasswordAuthenticationToken authentication) throws AuthenticationException {
		if (userDetails instanceof AppUser == false)
			throw new IllegalStateException("The " + UserDetailsService.class.getSimpleName() + " of type "
					+ getUserDetailsService().getClass().getName() + " returned a " + userDetails.getClass().getName()
					+ " which is not handled by this provider.");

		AppUser appUser = (AppUser) userDetails;

		super.additionalAuthenticationChecks(appUser, authentication);
	}

	@Override
	public void doAfterPropertiesSet() throws Exception {
		setUserDetailsService(appUserDetailsService);
		super.doAfterPropertiesSet();
	}
}
