package org.bocogop.kiosk.config;

import java.io.File;

import org.bocogop.shared.config.AbstractConfig;
import org.bocogop.shared.config.CacheConfig;
import org.bocogop.shared.config.EmailConfig;
import org.bocogop.shared.config.TemplateConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.scheduling.annotation.EnableAsync;

@Configuration
@PropertySource("file:///${AppEventPropertiesDir:" + AbstractConfig.DEFAULT_APP_PROPERTIES_DIR + "}/app.properties")
@PropertySource("file:///${AppEventPropertiesDir:" + AbstractConfig.DEFAULT_APP_PROPERTIES_DIR + "}/version.properties")
@ComponentScan(basePackages = { "org.bocogop.shared",
		"org.bocogop.kiosk" }, excludeFilters = @ComponentScan.Filter(type = FilterType.REGEX, pattern = {
				"org.bocogop\\..*\\.config\\..*", "org.bocogop\\..*\\.web\\..*" }))
@EnableAsync(proxyTargetClass = true)
@Import({ AOPConfig.class, CacheConfig.class, DataConfig.class, EmailConfig.class, TemplateConfig.class,
		WebSecurityConfig.class })
public class WebAppConfig extends AbstractConfig {

	public static final String DEFAULT_KIOSK_MESSAGES_DIR = DEFAULT_BASEDIR + "/kiosk/src/main/resources";

	@Value("${AppKioskPropertiesDir:" + DEFAULT_KIOSK_MESSAGES_DIR + "}")
	private String messagesDir;
	@Value("${AppKioskPropertiesDir:" + DEFAULT_APP_PROPERTIES_DIR + "}")
	private String propertiesDir;

	@Bean
	public MessageSource messageSource() {
		ReloadableResourceBundleMessageSource ms = new ReloadableResourceBundleMessageSource();
		ms.setCacheSeconds(Integer.parseInt(env.getProperty("messages.cache.expirySeconds")));
		ms.setBasenames("file:///" + messagesDir + File.separator + "messages",
				"file:///" + propertiesDir + File.separator + "app");
		return ms;
	}

}
