package org.bocogop.kiosk.config;

import java.time.LocalDate;

import org.bocogop.kiosk.service.VoterUserDetailsProvider;
import org.bocogop.shared.model.voter.Voter;
import org.bocogop.shared.util.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.AbstractUserDetailsAuthenticationProvider;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Transactional;

public class VoterDaoAuthenticationProvider extends AbstractUserDetailsAuthenticationProvider {

	@Autowired
	private VoterUserDetailsProvider voterUserDetailsProvider;

	@Value("${maxAllowedFailedLogins}")
	private int maxAllowedFailedLogins;
	@Value("${autoUnlockMinutes}")
	private int autoUnlockMinutes;
	@Value("${autoDisableAfterDays}")
	private int autoDisableAfterDays;

	public VoterDaoAuthenticationProvider() {
		setHideUserNotFoundExceptions(false);
	}

	@Override
	protected void additionalAuthenticationChecks(UserDetails userDetails,
			UsernamePasswordAuthenticationToken authentication) throws AuthenticationException {
		try {
			String dob = (String) authentication.getCredentials();
			LocalDate d = LocalDate.parse(dob, DateUtil.TWO_DIGIT_DATE_ONLY_FORMAT);
			Voter v = (Voter) userDetails;
			if (v.getBirthYear() == null || !v.getBirthYear().equals(d))
				throw new BadCredentialsException("No voter found with that identifying code and birth year.");
		} catch (Exception e) {
			if (e instanceof BadCredentialsException)
				throw e;

			throw new BadCredentialsException("No voter found with that identifying code and birth year.");
		}
	}

	@Override
	@Transactional(readOnly = true)
	protected UserDetails retrieveUser(String username, UsernamePasswordAuthenticationToken authentication)
			throws AuthenticationException {
		Voter v = voterUserDetailsProvider.retrieveUser(username, authentication);
		return v;
	}

}
