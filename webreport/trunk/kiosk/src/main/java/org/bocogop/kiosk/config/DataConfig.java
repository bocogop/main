package org.bocogop.kiosk.config;

import org.bocogop.shared.config.AbstractDataConfig;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataConfig extends AbstractDataConfig {

	@Override
	protected String[] getAdditionalPackagesToScan() {
		return new String[] { "org.bocogop.kiosk.model" };
	}

}