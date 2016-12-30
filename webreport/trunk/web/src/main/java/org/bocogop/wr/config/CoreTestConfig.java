package org.bocogop.wr.config;

import static org.springframework.context.annotation.FilterType.REGEX;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;

@Configuration
@ComponentScan(basePackages = {
		"org.bocogop.shared", }, excludeFilters = @ComponentScan.Filter(type = REGEX, pattern = { "org.bocogop.shared.web.*" }) )
@EnableAspectJAutoProxy(proxyTargetClass = true)
@Import({ CoreTestDataConfig.class, CoreTestSecurityConfig.class, LdapConfig.class })
@PropertySource("file:///${AppPropertiesDir:" + AbstractConfig.DEFAULT_APP_PROPERTIES_DIR + "}/app.properties")
@PropertySource("file:///${AppPropertiesDir:" + AbstractConfig.DEFAULT_APP_PROPERTIES_DIR + "}/version.properties")
public class CoreTestConfig extends AbstractConfig {

}
