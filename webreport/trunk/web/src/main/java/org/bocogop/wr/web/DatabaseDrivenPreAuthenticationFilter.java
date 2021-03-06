package org.bocogop.wr.web;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.bocogop.shared.model.AppUser;
import org.bocogop.shared.service.AppUserDetailsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;

public class DatabaseDrivenPreAuthenticationFilter extends AbstractPreAuthenticatedProcessingFilter {
	private static final Logger log = LoggerFactory.getLogger(DatabaseDrivenPreAuthenticationFilter.class);

	/* Allow application code to manually override this if desired - CPB */
	public static boolean preauthDisabled = false;

	@Value("${app.production:false}")
	private boolean isProduction;
	@Value("${bocogop.wr.preauth.username:_none}")
	private String preauthUsername;
	@Autowired
	private AppUserDetailsService appUserDetailsService;

	private List<String> excludedPrefixes = new ArrayList<>();

	public DatabaseDrivenPreAuthenticationFilter(String... excludedPrefixes) {
		if (excludedPrefixes != null)
			this.excludedPrefixes = Arrays.asList(excludedPrefixes);
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		if (request instanceof HttpServletRequest == false) {
			super.doFilter(request, response, chain);
			return;
		}

		HttpServletRequest hsr = (HttpServletRequest) request;
		for (String excludedPrefix : excludedPrefixes)
			if (hsr.getServletPath().startsWith(excludedPrefix)) {
				chain.doFilter(request, response);
				return;
			}

		super.doFilter(request, response, chain);
	}

	@Override
	protected Object getPreAuthenticatedPrincipal(HttpServletRequest request) {
		if (!isRunPreauth())
			return null;

		AppUser appUser = (AppUser) appUserDetailsService.loadUserByUsername(preauthUsername);
		if (appUser == null) {
			log.error("The username {} was not found in the database; bypassing pre-authentication", preauthUsername);
			return null;
		}

		return appUser;
	}

	private boolean isRunPreauth() {
		return !preauthDisabled && !isProduction && !"_none".equals(preauthUsername);
	}

	protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response,
			Authentication authResult) throws IOException, ServletException {
		super.successfulAuthentication(request, response, authResult);
	}

	@Override
	protected Object getPreAuthenticatedCredentials(HttpServletRequest request) {
		return "N/A";
	}

}
