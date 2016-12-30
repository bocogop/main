package org.bocogop.wr.config;

import org.springframework.context.annotation.Configuration;

@Configuration
public class DataConfig extends AbstractDataConfig {

	@Override
	protected String[] getAdditionalPackagesToScan() {
		return new String[] { "org.bocogop.wr.model" };
	}

}
