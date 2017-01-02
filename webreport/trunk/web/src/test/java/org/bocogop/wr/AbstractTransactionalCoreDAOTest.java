package org.bocogop.wr;

import org.bocogop.shared.config.CoreTestConfig;
import org.bocogop.shared.model.IdentifiedPersistent;
import org.bocogop.wr.config.testOnly.BaseTestConfig;
import org.bocogop.wr.test.AbstractTransactionalDAOTest;
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
