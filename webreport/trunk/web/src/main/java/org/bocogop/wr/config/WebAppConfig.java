package org.bocogop.wr.config;

import java.io.File;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;

@Configuration
@Import({ ScheduledTasksConfig.class, WebSecurityConfig.class })
@PropertySource("file:///${AppPropertiesDir:" + AbstractConfig.DEFAULT_APP_PROPERTIES_DIR + "}/app.properties")
@PropertySource("file:///${AppPropertiesDir:" + AbstractConfig.DEFAULT_APP_PROPERTIES_DIR + "}/version.properties")
public class WebAppConfig extends AbstractAppConfig {

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

}
