package org.bocogop.wr.config;

import java.util.HashMap;
import java.util.Map;

import org.bocogop.shared.config.AbstractSecurityConfig;
import org.bocogop.shared.config.AjaxAwareAuthenticationEntryPoint;
import org.bocogop.shared.config.CommonWebConfig;
import org.bocogop.shared.model.Permission;
import org.bocogop.shared.service.AppUserDetailsService;
import org.bocogop.wr.web.DatabaseDrivenPreAuthenticationFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
import org.springframework.security.authentication.event.LoggerListener;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.authentication.AuthenticationManagerBeanDefinitionParser.NullAuthenticationProvider;
import org.springframework.security.core.userdetails.AuthenticationUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.access.AccessDeniedHandlerImpl;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.ExceptionMappingAuthenticationFailureHandler;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationProvider;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.security.web.authentication.preauth.RequestHeaderAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends AbstractSecurityConfig {
	private static final Logger log = LoggerFactory.getLogger(WebSecurityConfig.class);

	public static void main(String[] args) throws Exception {
		System.out.println(new BCryptPasswordEncoder().encode("WR_tester_NUMBER1!"));
		System.out.println(new BCryptPasswordEncoder().encode("WR_tester_NUMBER2!@"));
		System.out.println(new BCryptPasswordEncoder().encode("WR_tester_NUMBER3!@#"));
		System.out.println(new BCryptPasswordEncoder().encode("WR_tester_NUMBER4!@#$"));
		System.out.println(new BCryptPasswordEncoder().encode("WR_tester_NUMBER5!@#$%"));
		System.out.println(new BCryptPasswordEncoder().encode("WR_tester_NUMBER6!@#$%^"));
		System.out.println(new BCryptPasswordEncoder().encode("WR_tester_NUMBER7!@#$%^&"));
		System.out.println(new BCryptPasswordEncoder().encode("Yaseenzahra"));
	}

	public static final String URI_LOGIN = "/login.htm";
	public static final String URI_LOGOUT = "/logout.htm";
	public static final String URI_DEFAULT = "/index.htm";
	public static final String URI_AUTH_EXCEPTION = "/processAuthorizationException.htm";

	@Value("${cookie.sessionId")
	private String cookieSessionId;
	@Value("${httpsRequired}")
	private boolean httpsRequired;
	@Value("${port.http}")
	private int httpPort;
	@Value("${port.https}")
	private int httpsPort;

	@Value("${authProvider.localDevAuth.active}")
	private boolean devPreAuthActive;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private AppUserDetailsService appUserDetailsService;

	@Bean
	@Override
	public AuthenticationManager authenticationManagerBean() throws Exception {
		return super.authenticationManagerBean();
	}

	@Bean
	public LoggerListener loggerListener() {
		return new LoggerListener();
	}

	@Override
	public void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth.eraseCredentials(true) //
				.authenticationProvider(preAuthenticatedProvider()) //
				.authenticationProvider(hybridAuthenticationProvider());
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.authorizeRequests() //
				.antMatchers(URI_LOGIN, URI_LOGOUT, URI_AUTH_EXCEPTION, MEDIA_DIR + "/**") //
				.permitAll()
				.antMatchers(URI_DEFAULT, CommonWebConfig.AJAX_CONTEXT_PATH_PREFIX + "/**")
				.authenticated() //
				.antMatchers("/**/*.htm") //
				.hasAuthority(Permission.LOGIN_APPLICATION) //

				.and().formLogin().loginPage(URI_LOGIN).defaultSuccessUrl(URI_DEFAULT, true)
				.failureHandler(myAuthenticationFailureHandler());

		if (devPreAuthActive)
			http.addFilterBefore(devPreAuthFilter(), RequestHeaderAuthenticationFilter.class);

		http.logout().invalidateHttpSession(true).deleteCookies(cookieSessionId); //

		/*
		 * .headers() not working for requests that return Tiles views, not sure
		 * exactly why but I'm implementing the security-related ones in
		 * Apache's standalone.xml as response-headers - CPB
		 */

		http //
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
	public AccessDeniedHandler myAccessDeniedHandler() {
		AccessDeniedHandlerImpl adh = new AccessDeniedHandlerImpl();
		adh.setErrorPage(URI_AUTH_EXCEPTION);
		return adh;
	}

	@Bean
	public AuthenticationFailureHandler myAuthenticationFailureHandler() {
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

	// ---------------------------------------------- Providers

	@Bean
	public AuthenticationProvider preAuthenticatedProvider() {
		if (!devPreAuthActive)
			return new NullAuthenticationProvider();

		PreAuthenticatedAuthenticationProvider p = new PreAuthenticatedAuthenticationProvider();
		p.setPreAuthenticatedUserDetailsService(
				new AuthenticationUserDetailsService<PreAuthenticatedAuthenticationToken>() {
					@Override
					public UserDetails loadUserDetails(PreAuthenticatedAuthenticationToken token)
							throws UsernameNotFoundException {
						Object o = token.getPrincipal();
						if (o instanceof UserDetails)
							return (UserDetails) o;

						if (o instanceof String) {
							return appUserDetailsService.loadUserByUsername((String) o);
						}

						return null;
					}
				});
		return p;
	}

	@Bean
	public AuthenticationProvider hybridAuthenticationProvider() {
		HybridDaoAuthenticationProvider p = new HybridDaoAuthenticationProvider(passwordEncoder);
		return p;
	}

	// ------------------------------------------------- Filters

	@Bean
	public DatabaseDrivenPreAuthenticationFilter devPreAuthFilter() throws Exception {
		DatabaseDrivenPreAuthenticationFilter f = new DatabaseDrivenPreAuthenticationFilter(MEDIA_DIR);
		f.setAuthenticationManager(authenticationManagerBean());
		return f;
	}

}
