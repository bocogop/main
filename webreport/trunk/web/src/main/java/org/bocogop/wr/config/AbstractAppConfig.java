package org.bocogop.wr.config;

import javax.management.MBeanServer;

import org.bocogop.shared.config.AbstractConfig;
import org.bocogop.shared.config.LdapConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableMBeanExport;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.jmx.support.MBeanServerFactoryBean;
import org.springframework.scheduling.annotation.EnableAsync;

@ComponentScan(basePackages = { "org.bocogop.shared",
		"org.bocogop.wr" }, excludeFilters = @ComponentScan.Filter(type = FilterType.REGEX, pattern = {
				"org.bocogop\\..*\\.config\\..*", "org.bocogop\\..*\\.web\\..*" }))
@EnableAsync(proxyTargetClass = true)
@EnableMBeanExport
@Import({ AOPConfig.class, CacheConfig.class, DataConfig.class, EmailConfig.class, LdapConfig.class,
		TemplateConfig.class })
public abstract class AbstractAppConfig extends AbstractConfig {

	@Bean
	public MBeanServer mbeanServer() {
		return new MBeanServerFactoryBean().getObject();
	}

}
