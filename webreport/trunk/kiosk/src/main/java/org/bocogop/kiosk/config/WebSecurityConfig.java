package org.bocogop.kiosk.config;

import java.util.HashMap;
import java.util.Map;

import org.bocogop.shared.config.AbstractSecurityConfig;
import org.bocogop.shared.config.AjaxAwareAuthenticationEntryPoint;
import org.bocogop.shared.config.CommonWebConfig;
import org.bocogop.shared.model.Permission;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.access.AccessDeniedHandlerImpl;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.ExceptionMappingAuthenticationFailureHandler;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends AbstractSecurityConfig {
	public static final String URI_LOGIN = "/login.htm";
	public static final String URI_LOGOUT = "/logout.htm";
	public static final String URI_DEFAULT = "/index.htm";
	public static final String URI_HELP = "/help.htm";
	public static final String URI_AUTH_EXCEPTION = "/processAuthorizationException.htm";

	@Value("${cookie.sessionId")
	private String cookieSessionId;
	@Value("${httpsRequired}")
	private boolean httpsRequired;
	@Value("${port.http}")
	private int httpPort;
	@Value("${port.https}")
	private int httpsPort;

	@Override
	public void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth.eraseCredentials(true) //
				.authenticationProvider(authenticationProvider());
	}

	@Bean
	public AuthenticationProvider authenticationProvider() {
		VoterDaoAuthenticationProvider p = new VoterDaoAuthenticationProvider();
		return p;
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.authorizeRequests() //
				.antMatchers("/selectEvent.htm", URI_LOGIN, URI_LOGOUT, URI_HELP, URI_AUTH_EXCEPTION, MEDIA_DIR + "/**"
				// uses HTTP basic auth from print client
						, "/rest/getNextPrintRequest") //
				.permitAll() //
				.antMatchers(URI_DEFAULT, CommonWebConfig.AJAX_CONTEXT_PATH_PREFIX + "/**") //
				.authenticated() //
				.antMatchers("/**/*.htm") //
				.hasAuthority(Permission.LOGIN_KIOSK) //

				.and().formLogin().loginPage(URI_LOGIN).defaultSuccessUrl(URI_DEFAULT, true)
				.failureHandler(myAuthenticationFailureHandler())

				.and() //
				.csrf().disable()
				//
				.logout().invalidateHttpSession(true).deleteCookies(cookieSessionId)
				
				// .headers() not working for requests that return Tiles views, not sure exactly why but
				// I'm implementing these in standalone.xml as response-headers - CPB
				
				.and() //
				.exceptionHandling() //
				.authenticationEntryPoint(new AjaxAwareAuthenticationEntryPoint(URI_LOGIN)) //
				.accessDeniedHandler(myAccessDeniedHandler());

		if (httpsRequired) {
			/*
			 * Use HTTPs for ALL requests
			 */
			http.requiresChannel().anyRequest().requiresSecure();
			http.portMapper().http(httpPort).mapsTo(httpsPort);
		}
	}
	
	@Override
	public void configure(WebSecurity web) throws Exception {
		web.ignoring().antMatchers(MEDIA_DIR + "/**");
	}

	@Bean
	@Override
	public AuthenticationManager authenticationManagerBean() throws Exception {
		return super.authenticationManagerBean();
	}

	@Bean
	public AccessDeniedHandler myAccessDeniedHandler() {
		AccessDeniedHandlerImpl adh = new AccessDeniedHandlerImpl();
		adh.setErrorPage(URI_AUTH_EXCEPTION);
		return adh;
	}

	@Bean
	public AuthenticationFailureHandler myAuthenticationFailureHandler() throws Exception {
		ExceptionMappingAuthenticationFailureHandler h = new ExceptionMappingAuthenticationFailureHandler();

		Map<String, String> failureUrlMap = new HashMap<>();
		failureUrlMap.put(AccountExpiredException.class.getName(), URI_LOGIN + "?error=expired");
		failureUrlMap.put(BadCredentialsException.class.getName(), URI_LOGIN + "?error=default");
		failureUrlMap.put(CredentialsExpiredException.class.getName(), URI_LOGIN + "?error=expired");
		failureUrlMap.put(DisabledException.class.getName(), URI_LOGIN + "?error=disabled");
		failureUrlMap.put(LockedException.class.getName(), URI_LOGIN + "?error=locked");
		h.setExceptionMappings(failureUrlMap);

		h.setDefaultFailureUrl(URI_LOGIN + "?error=default");
		return h;
	}

}