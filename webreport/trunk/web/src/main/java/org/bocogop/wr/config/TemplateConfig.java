package org.bocogop.wr.config;

import java.io.IOException;
import java.util.Properties;

import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.VelocityException;
import org.bocogop.wr.persistence.VelocityDataSourceResourceLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.ui.velocity.VelocityEngineFactoryBean;

@Configuration
public class TemplateConfig {

	@Autowired
	private Environment env;
	@Autowired
	private VelocityDataSourceResourceLoader loader;

	@Bean
	public VelocityEngine velocityEngine() throws VelocityException, IOException {
		VelocityEngineFactoryBean factory = new VelocityEngineFactoryBean();
		Properties props = new Properties();
		props.put("resource.loader", "ds");
		props.put("ds.resource.loader.instance", loader);
		props.put("ds.resource.loader.public.name", "DataSource");
		props.put("ds.resource.loader.description", "Velocity DataSource Resource Loader");
		props.put("ds.resource.loader.resource.table", "CORE.STD_TEMPLATE");
		props.put("ds.resource.loader.resource.keycolumn", "TEMPLATE_NAME");
		props.put("ds.resource.loader.resource.templatecolumn", "TEMPLATE_BODY");
		props.put("ds.resource.loader.resource.timestampcolumn", "MODIFIED_DATE");
		props.put("ds.resource.loader.cache", env.getProperty("velocity.cacheTemplates"));
		props.put("ds.resource.loader.modificationCheckInterval",
				env.getProperty("velocity.modificationCheckInterval"));
		props.put("velocimacro.permissions.allow.inline.to.replace.global", "true");
		props.put("velocimacro.permissions.allow.inline.local.scope", "true");
		props.put("velocimacro.context.localscope", "true");
		factory.setVelocityProperties(props);

		return factory.createVelocityEngine();
	}

}
