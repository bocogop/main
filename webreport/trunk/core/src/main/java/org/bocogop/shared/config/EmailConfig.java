package org.bocogop.shared.config;

import java.util.Properties;
import java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class EmailConfig {

	@Autowired
	protected Environment env;

	@Bean
	public ThreadPoolTaskExecutor emailServiceTaskExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(Integer.parseInt(env.getProperty("emailService.taskExecutor.corePoolSize")));
		executor.setMaxPoolSize(Integer.parseInt(env.getProperty("emailService.taskExecutor.maxPoolSize")));
		executor.setQueueCapacity(Integer.parseInt(env.getProperty("emailService.taskExecutor.queueCapacity")));
		executor.setThreadNamePrefix("EmailTaskExecutor-");
		executor.setRejectedExecutionHandler(new CallerRunsPolicy());
		executor.initialize();
		return executor;
	}

	@Bean
	public JavaMailSenderImpl emailSender() {
		JavaMailSenderImpl mailSenderImpl = new JavaMailSenderImpl();
		mailSenderImpl.setHost(env.getProperty("email.smtpServer"));
		// mailSenderImpl.setPort(env.getProperty("emailPort", Integer.class));
		// mailSenderImpl.setProtocol(env.getProperty("smtp.protocol"));
		// mailSenderImpl.setUsername(env.getProperty("smtp.username"));
		// mailSenderImpl.setPassword(env.getProperty("smtp.password"));

		Properties javaMailProps = new Properties();
		// FIXWR need to test this, old config didn't use auth
		// javaMailProps.put("mail.smtp.auth", true);
		javaMailProps.put("mail.smtp.starttls.enable", true);

		mailSenderImpl.setJavaMailProperties(javaMailProps);

		return mailSenderImpl;
	}

}
