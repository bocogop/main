package org.bocogop.shared;

import org.bocogop.shared.config.testOnly.BaseTestConfig;
import org.bocogop.shared.test.AbstractTransactionalDAOTest;
import org.bocogop.wr.config.CoreTestConfig;
import org.bocogop.wr.model.core.IdentifiedPersistent;
import org.springframework.test.context.ContextConfiguration;

/**
 * Base class for transactional DAO tests in the core module
 * 
 * @author barrycon
 */
@ContextConfiguration(classes = { CoreTestConfig.class, BaseTestConfig.class })
public abstract class AbstractTransactionalCoreDAOTest<T extends IdentifiedPersistent>
		extends AbstractTransactionalDAOTest<T> {

}
