package org.bocogop.wr.config;

import java.util.Properties;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.apache.commons.lang3.ArrayUtils;
import org.hibernate.cache.ehcache.SingletonEhCacheRegionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.instrument.classloading.InstrumentationLoadTimeWeaver;
import org.springframework.orm.hibernate5.HibernateExceptionTranslator;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.Database;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@EnableTransactionManagement(proxyTargetClass = true)
public abstract class AbstractDataConfig {
	private static final Logger log = LoggerFactory.getLogger(AbstractDataConfig.class);

	@Autowired
	private Environment env;
	@Autowired
	private DataSource dataSource;

	@Bean
	public PlatformTransactionManager transactionManager() {
		EntityManagerFactory factory = entityManagerFactory().getObject();
		return new JpaTransactionManager(factory);
	}

	@Bean
	public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
		String[] all = ArrayUtils.addAll(new String[] { "org.bocogop.shared.model", "org.bocogop.shared.persistence.usertype" },
				getAdditionalPackagesToScan());
		return buildEntityManagerFactory(env, dataSource, all);
	}

	protected abstract String[] getAdditionalPackagesToScan();

	protected boolean isUnitTest() {
		return AbstractConfig.isUnitTest(env);
	}

	public static LocalContainerEntityManagerFactoryBean buildEntityManagerFactory(Environment env,
			DataSource dataSource, String[] packagesToScan) {
		boolean unitTest = AbstractConfig.isUnitTest(env);

		LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
		factory.setDataSource(dataSource);

		HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
		vendorAdapter.setDatabase(Database.SQL_SERVER);
		if (unitTest)
			vendorAdapter.setShowSql(Boolean.TRUE);
		factory.setJpaVendorAdapter(vendorAdapter);

		factory.setPackagesToScan(packagesToScan);

		Properties jpaProperties = new Properties();
		jpaProperties.put("hibernate.cache.region.factory_class", SingletonEhCacheRegionFactory.class.getName());
		for (String p : new String[] { //
				"hibernate.cache.use_query_cache", //
				"hibernate.cache.use_second_level_cache", //
				"hibernate.connection.isolation", //
				"hibernate.dialect", //
				"hibernate.format_sql", //
				"hibernate.generate_statistics", //
				"hibernate.jdbc.batch_size", //
				"hibernate.jdbc.batch_versioned_data", //
				"hibernate.max_fetch_depth", //
				"hibernate.order_inserts", //
				"hibernate.order_updates", //
				"hibernate.show_sql", //
				"hibernate.use_sql_comments", //
		}) {
			if (env.containsProperty(p)) {
				jpaProperties.put(p, env.getProperty(p));
			}
		}

		/* Override these for unit tests */
		if (unitTest) {
			for (String[] p : new String[][] { //
					{ "hibernate.format_sql", "true" }, //
					{ "hibernate.generate_statistics", "true" }, //
					{ "hibernate.show_sql", "true" }, //
					{ "hibernate.use_sql_comments", "true" } }) {
				jpaProperties.put(p[0], p[1]);
			}
		}
		// jpaProperties.put("net.sf.ehcache.configurationResourceName",
		// "ehcache.xml");
		factory.setJpaProperties(jpaProperties);

		factory.setLoadTimeWeaver(new InstrumentationLoadTimeWeaver());
		return factory;
	}

	@Bean
	public HibernateExceptionTranslator hibernateExceptionTranslator() {
		return new HibernateExceptionTranslator();
	}

}
