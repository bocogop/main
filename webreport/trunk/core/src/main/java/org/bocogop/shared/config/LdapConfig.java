package org.bocogop.shared.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.ldap.repository.config.EnableLdapRepositories;

@Configuration
@EnableLdapRepositories(basePackages = "org.bocogop.shared.persistence")
public class LdapConfig {

	@Autowired
	private Environment env;
	
	public String[] ldapServerURLs() {
		return new String[] { env.getRequiredProperty("ldapReadServerUrl1"),
				env.getRequiredProperty("ldapReadServerUrl2") };
	}

	@Bean
	public LdapContextSource contextSource() {
		LdapContextSource contextSource = new LdapContextSource();
		contextSource.setUrls(ldapServerURLs());
		contextSource.setBase(env.getRequiredProperty("ldapBase"));
		contextSource.setUserDn(env.getRequiredProperty("ldapUserId"));
		contextSource.setPassword(env.getRequiredProperty("ldapPassword"));
		contextSource.setReferral("follow");
		return contextSource;
	}

	@Bean
	public LdapTemplate ldapTemplate() {
		LdapTemplate ldapTemplate = new LdapTemplate(contextSource());
		ldapTemplate.setDefaultTimeLimit(env.getRequiredProperty("ldapTimeout", Integer.class));
		return ldapTemplate;
	}

}
