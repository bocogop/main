package org.bocogop.wr;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.security.auth.login.LoginContext;
import javax.sql.DataSource;

import org.bocogop.shared.config.testOnly.AppTestConfig;
import org.bocogop.shared.model.AppUser;
import org.bocogop.shared.persistence.AppUserDAO;
import org.bocogop.shared.persistence.dao.CountryDAO;
import org.bocogop.shared.persistence.dao.GenderDAO;
import org.bocogop.shared.persistence.dao.StateDAO;
import org.bocogop.shared.service.PrecinctService;
import org.bocogop.wr.config.WebAppConfig;
import org.bocogop.wr.test.AbstractAppTest;
import org.bocogop.wr.test.util.TestObjectFactory;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.springframework.transaction.PlatformTransactionManager;

@ContextConfiguration(classes = { WebAppConfig.class, AppTestConfig.class })
public abstract class AbstractWebAppTest extends AbstractJUnit4SpringContextTests {

	protected LoginContext vistaLoginContext = null;

	@Autowired
	protected AppUserDAO appUserDAO;
	@Autowired
	protected CountryDAO countryDAO;
	@Autowired
	protected GenderDAO genderDAO;
	@Autowired
	protected StateDAO stateDAO;

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
	protected PlatformTransactionManager tm;

	protected String vistaTestDivision = "442";

	protected AppUser user;

	@Before
	@WithMockUser(AbstractAppTest.UNIT_TEST_USER)
	public void login() throws Exception {
		user = appUserDAO.findByUsername(AbstractAppTest.UNIT_TEST_USER, false);
	}

}
