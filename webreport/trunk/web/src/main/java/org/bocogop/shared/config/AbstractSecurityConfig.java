package org.bocogop.shared.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
@EnableGlobalMethodSecurity(securedEnabled = true, prePostEnabled = true)
public abstract class AbstractSecurityConfig extends WebSecurityConfigurerAdapter {

	public static final String MEDIA_DIR = "/media";

}
