package org.bocogop.shared.config.testOnly;

import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;

import com.microsoft.sqlserver.jdbc.SQLServerDriver;

public abstract class AbstractTestConfig {

	@Autowired
	private Environment env;

	@Bean
	@Profile("default")
	public DataSource dataSource() {
		BasicDataSource dataSource = new BasicDataSource();
		dataSource.setDriverClassName(SQLServerDriver.class.getName());
		dataSource.setUrl(env.getProperty("unittest.dataSource.url"));
		dataSource.setUsername(env.getProperty("unittest.dataSource.username"));
		dataSource.setPassword(env.getProperty("unittest.dataSource.password"));
		return dataSource;
	}

}
