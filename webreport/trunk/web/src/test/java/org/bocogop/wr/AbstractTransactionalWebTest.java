package org.bocogop.wr;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.sql.DataSource;

import org.bocogop.shared.config.testOnly.AppTestConfig;
import org.bocogop.shared.persistence.AppUserDAO;
import org.bocogop.shared.persistence.dao.ApplicationParametersDAO;
import org.bocogop.shared.persistence.dao.CountryDAO;
import org.bocogop.shared.persistence.dao.GenderDAO;
import org.bocogop.shared.persistence.dao.StateDAO;
import org.bocogop.shared.persistence.dao.precinct.PrecinctDAO;
import org.bocogop.shared.persistence.dao.voter.VoterDAO;
import org.bocogop.shared.service.PrecinctService;
import org.bocogop.wr.config.WebAppConfig;
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
	protected AppUserDAO appUserDAO;
	@Autowired
	protected CountryDAO countryDAO;
	@Autowired
	protected GenderDAO genderDAO;
	@Autowired
	protected PrecinctDAO precinctDAO;
	@Autowired
	protected StateDAO stateDAO;
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
