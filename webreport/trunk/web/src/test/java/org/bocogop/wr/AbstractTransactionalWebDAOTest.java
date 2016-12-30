package org.bocogop.wr;

import javax.security.auth.login.LoginContext;

import org.bocogop.shared.test.AbstractTransactionalDAOTest;
import org.bocogop.wr.config.WebAppConfig;
import org.bocogop.wr.config.testOnly.AppTestConfig;
import org.bocogop.wr.model.core.IdentifiedPersistent;
import org.bocogop.wr.persistence.dao.precinct.PrecinctDAO;
import org.bocogop.wr.persistence.dao.voter.VoterDAO;
import org.bocogop.wr.persistence.lookup.StateDAO;
import org.bocogop.wr.test.util.TestObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = { WebAppConfig.class, AppTestConfig.class })
public abstract class AbstractTransactionalWebDAOTest<T extends IdentifiedPersistent>
		extends AbstractTransactionalDAOTest<T> {

	protected LoginContext vistaLoginContext = null;

	@Autowired
	protected PrecinctDAO precinctDAO;
	@Autowired
	protected StateDAO stateDAO;
	@Autowired
	protected VoterDAO voterDAO;

	@Autowired
	protected TestObjectFactory testObjectFactory;

}
