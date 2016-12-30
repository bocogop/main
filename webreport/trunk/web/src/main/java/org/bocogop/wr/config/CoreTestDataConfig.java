package org.bocogop.wr.config;

import org.springframework.context.annotation.Configuration;

@Configuration
public class CoreTestDataConfig extends AbstractDataConfig {

	@Override
	protected String[] getAdditionalPackagesToScan() {
		return null;
	}

}
