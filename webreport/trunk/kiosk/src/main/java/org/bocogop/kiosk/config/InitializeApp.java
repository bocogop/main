package org.bocogop.kiosk.config;

import org.bocogop.shared.config.AppContainerConfig;
import org.bocogop.shared.config.CommonWebConfig;
import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

public class InitializeApp extends AbstractAnnotationConfigDispatcherServletInitializer {

	@Override
	protected Class<?>[] getRootConfigClasses() {
		return new Class[] { WebAppConfig.class, AppContainerConfig.class };
	}

	@Override
	protected Class<?>[] getServletConfigClasses() {
		return new Class[] { WebConfig.class };
	}

	@Override
	protected String[] getServletMappings() {
		return new String[] { "*.htm", CommonWebConfig.AJAX_CONTEXT_PATH_PREFIX + "/*" };
	}

}