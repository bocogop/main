package org.bocogop.wr.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.ImportResource;
import org.springframework.core.env.Environment;

@Configuration
@EnableAspectJAutoProxy(proxyTargetClass = true)
@ImportResource("classpath:spring/config-aop.xml")
public class AOPConfig {

	@Autowired
	private Environment env;

}
