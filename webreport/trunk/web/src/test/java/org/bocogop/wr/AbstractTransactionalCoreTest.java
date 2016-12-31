package org.bocogop.wr;

import org.bocogop.wr.config.CoreTestConfig;
import org.bocogop.wr.config.testOnly.BaseTestConfig;
import org.bocogop.wr.test.AbstractTransactionalAppTest;
import org.springframework.test.context.ContextConfiguration;

/**
 * Base class for transactional unit tests in the core module
 * 
 * @author barrycon
 */
@ContextConfiguration(classes = { CoreTestConfig.class, BaseTestConfig.class })
public abstract class AbstractTransactionalCoreTest extends AbstractTransactionalAppTest {

}
