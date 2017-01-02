package org.bocogop.kiosk.config;

import java.io.File;

import org.bocogop.shared.config.AbstractConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;

@Configuration
@Import({ WebSecurityConfig.class })
@PropertySource("file:///${AppEventPropertiesDir:" + AbstractConfig.DEFAULT_APP_PROPERTIES_DIR + "}/app.properties")
@PropertySource("file:///${AppEventPropertiesDir:" + AbstractConfig.DEFAULT_APP_PROPERTIES_DIR + "}/version.properties")
public class WebAppConfig extends AbstractConfig {

	public static final String DEFAULT_EVENT_MESSAGES_DIR = DEFAULT_BASEDIR + "/event/src/main/resources";

	@Value("${AppEventPropertiesDir:" + DEFAULT_EVENT_MESSAGES_DIR + "}")
	private String messagesDir;
	@Value("${AppEventPropertiesDir:" + DEFAULT_APP_PROPERTIES_DIR + "}")
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
