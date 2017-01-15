package org.bocogop.wr.test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Random;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContext;
import javax.sql.DataSource;

import org.bocogop.shared.model.AppUser;
import org.bocogop.shared.model.Role;
import org.bocogop.shared.model.Role.RoleType;
import org.bocogop.shared.model.precinct.Precinct;
import org.bocogop.shared.persistence.AppUserDAO;
import org.bocogop.shared.persistence.dao.CountryDAO;
import org.bocogop.shared.persistence.dao.GenderDAO;
import org.bocogop.shared.persistence.dao.RoleDAO;
import org.bocogop.shared.persistence.dao.StateDAO;
import org.bocogop.shared.persistence.dao.precinct.PrecinctDAO;
import org.bocogop.shared.service.AppUserService;
import org.bocogop.shared.service.AppUserTestService;
import org.bocogop.shared.service.PrecinctService;
import org.bocogop.shared.util.SecurityUtil;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.security.test.context.support.WithSecurityContextTestExecutionListener;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Base class that provides common beans within the application context for
 * integration testing. The parent Spring class establishes a transaction before
 * each test method and rolls it back after completion, to prevent database
 * changes from being saved.
 * 
 * @author barrycon
 * 
 */
@TestExecutionListeners({ WithSecurityContextTestExecutionListener.class })
public abstract class AbstractTransactionalAppTest extends AbstractTransactionalJUnit4SpringContextTests {
	private static final Logger log = LoggerFactory.getLogger(AbstractTransactionalAppTest.class);

	protected static final String UNIT_TEST_USER = "VHACISSWRTEST1";
	public static final String TEST_STATION_NUMBER = "442";

	// ----------------------------- Shared Fields

	@Autowired
	protected DataSource dataSource;
	@PersistenceContext
	protected EntityManager em;
	@Resource
	protected EntityManagerFactory emFactory;
	@Autowired
	@Qualifier("transactionManager")
	protected PlatformTransactionManager tm;

	// DAOs
	@Autowired
	protected AppUserDAO appUserDAO;
	@Autowired
	protected CountryDAO countryDAO;
	@Autowired
	protected GenderDAO genderDAO;
	@Autowired
	protected RoleDAO roleDAO;
	@Autowired
	protected PrecinctDAO precinctDAO;
	@Autowired
	protected PrecinctService precinctService;
	@Autowired
	protected StateDAO stateDAO;

	// Services
	@Autowired
	protected AppUserService appUserService;
	@Autowired
	protected AppUserTestService appUserTestService;
	@Autowired
	protected MessageSource messageSource;

	// --------------------------------- Test Data

	protected Random random = new Random();

	protected AppUser user;

	// --------------------------------- Test Methods

	@Before
	@WithUserDetails(UNIT_TEST_USER)
	public void login() throws Exception {
		System.out.println("Logged in as mock user " + UNIT_TEST_USER);
		user = appUserDAO.findByUsername(UNIT_TEST_USER, false);
	}

	protected AppUser createTestUser() {
		return recreateDummyPersistentNationalAdminUser(appUserDAO, roleDAO, appUserTestService, tm);
	}

	static AppUser recreateDummyPersistentNationalAdminUser(final AppUserDAO appUserDAO, final RoleDAO roleDAO,
			final AppUserTestService appUserService, final PlatformTransactionManager tm) {
		AppUser persistentUser = new TransactionTemplate(tm).execute(new TransactionCallback<AppUser>() {
			public AppUser doInTransaction(TransactionStatus ts) {
				String username = SecurityUtil.getCurrentUserName();
				appUserService.deleteIfExists(username);
				AppUser user = new AppUser(username);

				Role role = roleDAO.findByLookup(RoleType.NATIONAL_ADMIN);
				user.addRole(role);

				user.setEmail("unittest-ciss1@va.gov");
				user.setLastName("CISS1");
				user.setFirstName("UnitTest");
				user = appUserDAO.saveOrUpdate(user);
				return user;
			}
		});
		return persistentUser;
	}

	// ----------------------------- Utility Methods

	protected Precinct getPrecinct() {
		return precinctDAO.findByCriteria(TEST_STATION_NUMBER, null).first();
	}

	protected synchronized Precinct createNewPrecinct(String name) {
		final String INSERT_SQL = "insert into sdsadm.std_institution(id, name, vistaName, stationNumber, precinctType_id,"
				+ " mfn_zeg_recipient, version, created, createdBy) select max(f.id) + 1," //
				+ " ?, ?, ?, max(c.id), 0, 0, current_timestamp, 'UnitTest'" //
				+ " from sdsadm.std_institution f, sdsadm.std_precincttype c" //
				+ " where c.code = 'CBOC'";

		jdbcTemplate.update(new PreparedStatementCreator() {
			public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
				PreparedStatement ps = connection.prepareStatement(INSERT_SQL, new String[] { "id" });
				ps.setString(1, name);
				ps.setString(2, name);
				ps.setString(3, name);
				return ps;
			}
		});

		Long newId = jdbcTemplate.query("select max(id) from sdsadm.std_institution", new ResultSetExtractor<Long>() {
			@Override
			public Long extractData(ResultSet rs) throws SQLException, DataAccessException {
				rs.next();
				return rs.getLong(1);
			}
		});

		return precinctDAO.findRequiredByPrimaryKey(newId);
	}

	@SuppressWarnings("unchecked")
	protected List<Object[]> querySQL(String sql) {
		return em.createNativeQuery(sql).getResultList();
	}

	protected int executeSQL(String sql) {
		return em.createNativeQuery(sql).executeUpdate();
	}

	protected void dumpTable(String table) {
		dumpTable(table, null);
	}

	protected void dumpTable(String table, String customSQL) {
		List<Object[]> resultList = querySQL(customSQL != null ? customSQL : "select * from " + table);
		for (Object[] row : resultList) {
			for (Object col : row)
				System.out.print("\t" + col);
			System.out.print("\n");
		}
	}

	/*
	 * Generically merges any specified item into the DB and immediately
	 * requests the entitymanager to flush the change. This is helpful while
	 * unit testing DAOs (but service methods should accommodate flushing as
	 * needed)
	 */
	protected <T> T saveAndFlush(T p2) {
		T newPA = em.merge(p2);
		em.flush();
		return newPA;
	}

}
