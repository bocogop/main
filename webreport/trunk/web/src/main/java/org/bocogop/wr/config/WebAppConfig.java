package org.bocogop.wr.config;

import java.io.File;

import javax.management.MBeanServer;

import org.bocogop.shared.config.AbstractConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableMBeanExport;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.jmx.support.MBeanServerFactoryBean;
import org.springframework.scheduling.annotation.EnableAsync;

@Configuration
@PropertySource("file:///${AppPropertiesDir:" + AbstractConfig.DEFAULT_APP_PROPERTIES_DIR + "}/app.properties")
@PropertySource("file:///${AppPropertiesDir:" + AbstractConfig.DEFAULT_APP_PROPERTIES_DIR + "}/version.properties")
@ComponentScan(basePackages = { "org.bocogop.shared",
		"org.bocogop.wr" }, excludeFilters = @ComponentScan.Filter(type = FilterType.REGEX, pattern = {
				"org.bocogop\\..*\\.config\\..*", "org.bocogop\\..*\\.web\\..*" }))
@EnableAsync(proxyTargetClass = true)
@EnableMBeanExport
@Import({ AOPConfig.class, CacheConfig.class, DataConfig.class, EmailConfig.class, TemplateConfig.class,
		WebSecurityConfig.class })
public class WebAppConfig extends AbstractConfig {

	public static final String DEFAULT_WEB_MESSAGES_DIR = DEFAULT_BASEDIR + "/web/src/main/resources";

	@Value("${AppPropertiesDir:" + DEFAULT_WEB_MESSAGES_DIR + "}")
	private String messagesDir;
	@Value("${AppPropertiesDir:" + DEFAULT_APP_PROPERTIES_DIR + "}")
	private String propertiesDir;

	@Bean
	public MessageSource messageSource() {
		ReloadableResourceBundleMessageSource ms = new ReloadableResourceBundleMessageSource();
		ms.setCacheSeconds(Integer.parseInt(env.getProperty("messages.cache.expirySeconds")));
		ms.setBasenames("file:///" + messagesDir + File.separator + "messages",
				"file:///" + propertiesDir + File.separator + "app");
		return ms;
	}

	@Bean
	public MBeanServer mbeanServer() {
		return new MBeanServerFactoryBean().getObject();
	}

}
