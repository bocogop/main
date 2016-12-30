package org.bocogop.wr.config;

import java.util.List;

import javax.persistence.EntityManagerFactory;

import org.bocogop.wr.web.ajax.AjaxRequestHandler.CustomJsonObjectMapper;
import org.bocogop.wr.web.conversion.Jsr310DateTimeFormatAnnotationFormatterFactory;
import org.bocogop.wr.web.interceptor.BreadcrumbsInterceptor;
import org.bocogop.wr.web.interceptor.CommonReferenceDataInterceptor;
import org.bocogop.wr.web.interceptor.StoreLastGetRequestInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.format.FormatterRegistry;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.orm.jpa.support.OpenEntityManagerInViewInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ViewResolverRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import org.springframework.web.servlet.view.tiles3.TilesConfigurer;
import org.springframework.web.servlet.view.tiles3.TilesViewResolver;

import com.fasterxml.jackson.databind.ObjectMapper;

@Configuration
@ComponentScan({ "org.bocogop.shared.web", "org.bocogop.wr.web" })
@EnableAspectJAutoProxy(proxyTargetClass = true)
@PropertySource("file:///${AppPropertiesDir:" + AbstractConfig.DEFAULT_APP_PROPERTIES_DIR + "}/app.properties")
@PropertySource("file:///${AppPropertiesDir:" + AbstractConfig.DEFAULT_APP_PROPERTIES_DIR + "}/version.properties")
public class WebConfig extends WebMvcConfigurationSupport {

	@Autowired
	private Jsr310DateTimeFormatAnnotationFormatterFactory jsr310DateTimeFormatAnnotationFormatterFactory;
	@Autowired
	private CommonReferenceDataInterceptor commonReferenceDataInterceptor;
	@Autowired
	private BreadcrumbsInterceptor breadcrumbsInterceptor;
	@Autowired
	private StoreLastGetRequestInterceptor storeLastGetRequestInterceptor;
	@Autowired
	private EntityManagerFactory entityManagerFactory;

	@Bean
	/* Has to be static per Spring's bootstrap process - CPB */
	public static PropertySourcesPlaceholderConfigurer webPlaceHolderConfigurer() {
		return new PropertySourcesPlaceholderConfigurer();
	}

	@Override
	protected void configureViewResolvers(ViewResolverRegistry registry) {
		TilesViewResolver viewResolver = new TilesViewResolver();
		viewResolver.setAlwaysInclude(false);
		viewResolver.setOrder(1);
		registry.viewResolver(viewResolver);

		// InternalResourceViewResolver internalResolver = new
		// InternalResourceViewResolver();
		// internalResolver.setPrefix("/WEB-INF/jsp/");
		// internalResolver.setSuffix(".jsp");
		// internalResolver.setOrder(2);
		// registry.viewResolver(internalResolver);
	}

	@Override
	protected void addFormatters(FormatterRegistry registry) {
		registry.addFormatterForFieldAnnotation(jsr310DateTimeFormatAnnotationFormatterFactory);
		/*
		 * Converters extending AbstractStringToPersistentConverter will be
		 * auto-added per its initializingBean() method - CPB
		 */
	}

	@Override
	protected void addInterceptors(InterceptorRegistry registry) {
		registry.addWebRequestInterceptor(openSessionInViewInterceptor());

		registry.addInterceptor(breadcrumbsInterceptor).addPathPatterns("/**").excludePathPatterns("/kiosk/**",
				"/index.htm", WebSecurityConfig.URI_LOGIN, WebSecurityConfig.URI_LOGOUT, "/selectStation.htm");
		registry.addInterceptor(commonReferenceDataInterceptor).addPathPatterns("/**").excludePathPatterns("/index.htm",
				WebSecurityConfig.URI_LOGOUT);
		registry.addInterceptor(storeLastGetRequestInterceptor).addPathPatterns("/**").excludePathPatterns("/index.htm",
				WebSecurityConfig.URI_LOGIN, WebSecurityConfig.URI_LOGOUT, "/selectStation.htm");
	}

	@Bean
	public OpenEntityManagerInViewInterceptor openSessionInViewInterceptor() {
		OpenEntityManagerInViewInterceptor i = new OpenEntityManagerInViewInterceptor();
		i.setEntityManagerFactory(entityManagerFactory);
		return i;
	}

	@Bean
	@Profile({ "attended" })
	public TilesConfigurer tilesConfigurer() {
		TilesConfigurer tc = new TilesConfigurer();
		tc.setDefinitions("/WEB-INF/tiles.xml", "/WEB-INF/tiles-reports.xml");
		return tc;
	}

	public MappingJackson2HttpMessageConverter jacksonMessageConverter() {
		MappingJackson2HttpMessageConverter messageConverter = new MappingJackson2HttpMessageConverter();
		ObjectMapper mapper = new CustomJsonObjectMapper();
		messageConverter.setObjectMapper(mapper);
		return messageConverter;
	}

	@Override
	public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
		converters.add(jacksonMessageConverter());
		super.configureMessageConverters(converters);
	}

}
