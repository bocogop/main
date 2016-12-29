package org.bocogop.shared;

import org.springframework.test.context.ContextConfiguration;

import org.bocogop.shared.config.CoreTestConfig;
import org.bocogop.shared.config.testOnly.BaseTestConfig;
import org.bocogop.shared.model.core.IdentifiedPersistent;
import org.bocogop.shared.test.AbstractTransactionalDAOTest;

/**
 * Base class for transactional DAO tests in the core module
 * 
 * @author barrycon
 */
@ContextConfiguration(classes = { CoreTestConfig.class, BaseTestConfig.class })
public abstract class AbstractTransactionalCoreDAOTest<T extends IdentifiedPersistent>
		extends AbstractTransactionalDAOTest<T> {

}
