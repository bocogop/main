package org.bocogop.kiosk.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.ImportResource;

@Configuration
@EnableAspectJAutoProxy(proxyTargetClass = true)
@ImportResource("classpath:spring/config-aop.xml")
public class AOPConfig {

}
