package org.bocogop.wr.config.testOnly;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import org.bocogop.shared.config.testOnly.AbstractTestConfig;

@Configuration
@Import({ MessagingTestConfig.class })
public class AppTestConfig extends AbstractTestConfig {

}
