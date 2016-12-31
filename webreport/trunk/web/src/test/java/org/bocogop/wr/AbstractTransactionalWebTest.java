package org.bocogop.wr;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.sql.DataSource;

import org.bocogop.wr.config.WebAppConfig;
import org.bocogop.wr.config.testOnly.AppTestConfig;
import org.bocogop.wr.persistence.AppUserDAO;
import org.bocogop.wr.persistence.AppUserPrecinctDAO;
import org.bocogop.wr.persistence.dao.ApplicationParametersDAO;
import org.bocogop.wr.persistence.dao.precinct.PrecinctDAO;
import org.bocogop.wr.persistence.dao.voter.VoterDAO;
import org.bocogop.wr.persistence.lookup.GenderDAO;
import org.bocogop.wr.service.PrecinctService;
import org.bocogop.wr.test.AbstractTransactionalAppTest;
import org.bocogop.wr.test.util.TestObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.PlatformTransactionManager;

@ContextConfiguration(classes = { WebAppConfig.class, AppTestConfig.class })
public abstract class AbstractTransactionalWebTest extends AbstractTransactionalAppTest {

	@Autowired
	protected ApplicationParametersDAO applicationParameterDAO;
	@Autowired
	protected AppUserPrecinctDAO appUserPrecinctDAO;
	@Autowired
	protected AppUserDAO appUserDAO;
	@Autowired
	protected GenderDAO genderDAO;
	@Autowired
	protected PrecinctDAO precinctDAO;
	@Autowired
	protected VoterDAO voterDAO;

	@Autowired
	protected PrecinctService precinctService;

	@Autowired
	protected DataSource dataSource;
	@Autowired
	protected TestObjectFactory testObjectFactory;

	@Autowired
	protected MessageSource messageSource;

	@PersistenceContext
	protected EntityManager em;

	@Autowired
	@Qualifier("transactionManager")
	protected PlatformTransactionManager tm;

}
