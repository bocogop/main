package org.bocogop.wr.config;

import org.bocogop.wr.model.Role.RoleType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configurers.provisioning.InMemoryUserDetailsManagerConfigurer;

@Configuration
public class CoreTestSecurityConfig extends AbstractSecurityConfig {

	@Bean
	@Override
	public AuthenticationManager authenticationManagerBean() throws Exception {
		return super.authenticationManagerBean();
	}
	
	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth.eraseCredentials(true);
		InMemoryUserDetailsManagerConfigurer<AuthenticationManagerBuilder> memAuth = auth.inMemoryAuthentication();
		memAuth.withUser("Ciss1").password("UnitTestPassword").roles(RoleType.NATIONAL_ADMIN.getName());
		memAuth.withUser("Ciss2").password("UnitTestPassword2").roles(RoleType.USER.getName());
	}

}
