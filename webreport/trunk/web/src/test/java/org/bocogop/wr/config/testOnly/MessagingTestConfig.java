package org.bocogop.wr.config.testOnly;

import javax.jms.ConnectionFactory;
import javax.naming.NamingException;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.EnableJms;

import com.mockrunner.jms.ConfigurationManager;
import com.mockrunner.jms.DestinationManager;
import com.mockrunner.mock.jms.MockQueueConnectionFactory;

@Configuration
@EnableJms
public class MessagingTestConfig {

	@Bean
	public ConnectionFactory jmsConnectionFactory() throws IllegalArgumentException, NamingException {
		MockQueueConnectionFactory cf = new MockQueueConnectionFactory(destinationManager(), configurationManager());
		return cf;
	}

	@Bean
	public ConfigurationManager configurationManager() {
		return new ConfigurationManager();
	}

	@Bean
	public DestinationManager destinationManager() {
		return new DestinationManager();
	}

}
