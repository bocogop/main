package org.bocogop.shared;

import org.bocogop.shared.config.CoreTestConfig;
import org.bocogop.shared.config.testOnly.BaseTestConfig;
import org.bocogop.shared.test.AbstractAppTest;
import org.springframework.test.context.ContextConfiguration;

/**
 * Base class for unit tests in the core module
 * 
 * @author barrycon
 */
@ContextConfiguration(classes = { CoreTestConfig.class, BaseTestConfig.class })
public abstract class AbstractCoreAppTest extends AbstractAppTest {

}
