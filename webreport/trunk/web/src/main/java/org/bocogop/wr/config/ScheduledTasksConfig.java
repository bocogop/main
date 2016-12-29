package org.bocogop.wr.config;

import java.util.concurrent.Executor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.DefaultManagedTaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import org.bocogop.shared.config.AbstractConfig;

@Configuration
@EnableScheduling
public class ScheduledTasksConfig implements SchedulingConfigurer {

	@Autowired
	private Environment env;

	@Bean
	public Executor taskExecutor() {
		if (AbstractConfig.isUnitTest(env)) {
			ThreadPoolTaskScheduler s = new ThreadPoolTaskScheduler();
			s.setPoolSize(5);
			s.initialize();
			return s;
		} else {
			return new DefaultManagedTaskScheduler();
		}
	}

	@Override
	public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
		taskRegistrar.setScheduler(taskExecutor());
	}

}
