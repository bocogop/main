package org.bocogop.shared;

import org.bocogop.shared.config.testOnly.BaseTestConfig;
import org.bocogop.shared.test.AbstractTransactionalAppTest;
import org.bocogop.wr.config.CoreTestConfig;
import org.springframework.test.context.ContextConfiguration;

/**
 * Base class for transactional unit tests in the core module
 * 
 * @author barrycon
 */
@ContextConfiguration(classes = { CoreTestConfig.class, BaseTestConfig.class })
public abstract class AbstractTransactionalCoreTest extends AbstractTransactionalAppTest {

}
