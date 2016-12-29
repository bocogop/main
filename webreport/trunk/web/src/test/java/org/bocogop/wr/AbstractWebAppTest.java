package org.bocogop.wr;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.security.auth.login.LoginContext;
import javax.sql.DataSource;

import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.springframework.transaction.PlatformTransactionManager;

import org.bocogop.shared.model.AppUser;
import org.bocogop.shared.persistence.AppUserDAO;
import org.bocogop.shared.persistence.lookup.sds.GenderDAO;
import org.bocogop.shared.service.VAFacilityService;
import org.bocogop.shared.test.AbstractAppTest;
import org.bocogop.wr.config.WebAppConfig;
import org.bocogop.wr.config.testOnly.AppTestConfig;
import org.bocogop.wr.persistence.dao.views.FacilityAndVisnDAO;
import org.bocogop.wr.test.util.TestObjectFactory;

@ContextConfiguration(classes = { WebAppConfig.class, AppTestConfig.class })
public abstract class AbstractWebAppTest extends AbstractJUnit4SpringContextTests {

	protected LoginContext vistaLoginContext = null;

	@Autowired
	protected AppUserDAO appUserDAO;
	@Autowired
	protected FacilityAndVisnDAO facilityAndVISNDAO;
	@Autowired
	protected GenderDAO genderDAO;

	@Autowired
	protected VAFacilityService vaFacilityService;

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