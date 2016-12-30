package org.bocogop.wr.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.AsyncAnnotationBeanPostProcessor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

public abstract class AbstractConfig {

	protected static final String DEFAULT_BASEDIR = "C:/dev/code/bocogop/main/webreport/trunk";
	public static final String DEFAULT_APP_PROPERTIES_DIR = DEFAULT_BASEDIR + "/properties/local";

	@Autowired
	protected Environment env;

	public static boolean isUnitTest(Environment env) {
		return env.acceptsProfiles("default");
	}

	@Bean
	/* Has to be static per Spring's bootstrap process - CPB */
	public static PropertySourcesPlaceholderConfigurer placeHolderConfigurer() {
		return new PropertySourcesPlaceholderConfigurer();
	}

	@Bean
	public AsyncAnnotationBeanPostProcessor asyncPostProcessor() {
		return new AsyncAnnotationBeanPostProcessor();
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

}
