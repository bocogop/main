package org.bocogop.shared.config;

import static org.bocogop.shared.model.lookup.InactiveReason.InactiveReasonType.LACK_OF_ACTIVITY;

import java.time.ZonedDateTime;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ldap.CommunicationException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsChecker;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.bocogop.shared.model.AppUser;
import org.bocogop.shared.model.ldap.LdapPerson;
import org.bocogop.shared.model.lookup.InactiveReason;
import org.bocogop.shared.persistence.LdapPersonDAO;
import org.bocogop.shared.persistence.lookup.InactiveReasonDAO;
import org.bocogop.shared.service.AppUserDetailsService;
import org.bocogop.shared.service.AppUserService;

public class HybridDaoAuthenticationProvider extends DaoAuthenticationProvider {

	@Autowired
	private AppUserService appUserService;
	@Autowired
	private AppUserDetailsService appUserDetailsService;
	@Autowired
	private InactiveReasonDAO inactiveReasonDAO;
	@Autowired
	private LdapPersonDAO ldapPersonDAO;

	@Value("${maxAllowedFailedLogins}")
	private int maxAllowedFailedLogins;
	@Value("${autoUnlockMinutes}")
	private int autoUnlockMinutes;
	@Value("${autoDisableAfterDays}")
	private int autoDisableAfterDays;

	private UserDetailsChecker defaultPreAuthenticationChecks;

	public HybridDaoAuthenticationProvider(PasswordEncoder passwordEncoder) {
		setHideUserNotFoundExceptions(false);
		setPasswordEncoder(passwordEncoder);

		defaultPreAuthenticationChecks = getPreAuthenticationChecks();
		setPreAuthenticationChecks(new AutoUnlockingDetailsChecker());
	}

	@Override
	protected void additionalAuthenticationChecks(UserDetails userDetails,
			UsernamePasswordAuthenticationToken authentication) throws AuthenticationException {
		if (userDetails instanceof AppUser == false)
			throw new IllegalStateException("The " + UserDetailsService.class.getSimpleName() + " of type "
					+ getUserDetailsService().getClass().getName() + " returned a " + userDetails.getClass().getName()
					+ " which is not handled by this provider.");

		AppUser appUser = (AppUser) userDetails;

		try {
			LdapPerson ldapPerson = ldapPersonDAO.findBySAMAccountName(appUser.getUsername());

			Object credentials = authentication.getCredentials();
			if (credentials instanceof String == false)
				throw new BadCredentialsException("The credentials supplied were of type "
						+ credentials.getClass().getName() + " but only a String password is supported");

			String password = (String) credentials;

			if (StringUtils.isBlank(password))
				throw new BadCredentialsException("Bad credentials");

			boolean authenticated = ldapPersonDAO.authenticate(ldapPerson.getDn(), password);
			if (!authenticated) {
				Integer failedLoginCount = appUser.getFailedLoginCount();
				if (failedLoginCount == null)
					failedLoginCount = 0;
				appUser.setFailedLoginCount(failedLoginCount + 1);
				appUser.setLastFailedLoginDate(ZonedDateTime.now());

				if (appUser.getFailedLoginCount() >= maxAllowedFailedLogins) {
					appUser.setAccountLockDate(ZonedDateTime.now());
				}
				appUser = appUserService.saveOrUpdateWithoutAuthority(appUser);
				
				if (appUser.isLocked())
					throw new LockedException("User account is locked");
				
				throw new BadCredentialsException("Bad credentials");
			}

			appUser.setFromLdapPerson(ldapPerson);
			appUser.setLastSuccessfulLoginDate(ZonedDateTime.now());
			appUser.setFailedLoginCount(0);
			appUser = appUserService.saveOrUpdateWithoutAuthority(appUser);
			appUserService.updatePassword(appUser.getId(), password);
		} catch (CommunicationException e) {
			/*
			 * server offline or inaccessible - revert to checking cached
			 * password
			 */
		}

		super.additionalAuthenticationChecks(appUser, authentication);
	}

	class AutoUnlockingDetailsChecker implements UserDetailsChecker {
		@Override
		public void check(UserDetails userDetails) {
			if (userDetails instanceof AppUser == false) {
				defaultPreAuthenticationChecks.check(userDetails);
				return;
			}

			AppUser u = (AppUser) userDetails;

			ZonedDateTime lastSuccessfulLoginDate = u.getLastSuccessfulLoginDate();
			if (u.isAccountNonExpired() && lastSuccessfulLoginDate != null && autoDisableAfterDays > 0
					&& lastSuccessfulLoginDate.isBefore(ZonedDateTime.now().minusDays(autoDisableAfterDays))) {
				InactiveReason inactiveReason = inactiveReasonDAO.findByLookup(LACK_OF_ACTIVITY);
				u.inactivate(ZonedDateTime.now(), inactiveReason);
				u = appUserService.saveOrUpdateWithoutAuthority(u);
			} else if (!u.isAccountNonLocked() && autoUnlockMinutes > 0) {
				if (ZonedDateTime.now().isAfter(u.getAccountLockDate().plusMinutes(autoUnlockMinutes))) {
					/*
					 * Assumes it will proceed to additionalAuthenticationChecks
					 * above, where the user is saved - CPB
					 */
					u.unlock();
				}
			}

			defaultPreAuthenticationChecks.check(u);
		}
	}

	@Override
	public void doAfterPropertiesSet() throws Exception {
		setUserDetailsService(appUserDetailsService);
		super.doAfterPropertiesSet();
	}
}
