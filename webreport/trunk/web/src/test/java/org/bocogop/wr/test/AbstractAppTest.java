package org.bocogop.wr.test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContext;

import org.bocogop.shared.model.AppUser;
import org.bocogop.shared.model.Permission.PermissionType;
import org.bocogop.shared.model.Role.RoleType;
import org.bocogop.shared.model.precinct.Precinct;
import org.bocogop.shared.persistence.AppUserDAO;
import org.bocogop.shared.persistence.dao.RoleDAO;
import org.bocogop.shared.persistence.dao.precinct.PrecinctDAO;
import org.bocogop.shared.service.AppUserService;
import org.bocogop.shared.util.WebUtil;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * Base class that provides common beans within the application context for
 * integration testing. Data changes performed in these unit tests are
 * permanently saved to the database.
 * 
 * @author barrycon
 */
// @EnableGlobalMethodSecurity(securedEnabled = true, prePostEnabled = true)
public abstract class AbstractAppTest extends AbstractJUnit4SpringContextTests {
	private static final Logger log = LoggerFactory.getLogger(AbstractAppTest.class);

	public static final String UNIT_TEST_USER = "barryc";
	public static final String TEST_PRECINCT = "1234";

	// ----------------------------- Shared Fields

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
	protected RoleDAO roleDAO;
	@Autowired
	protected PrecinctDAO precinctDAO;

	// Services
	@Autowired
	protected AppUserService appUserService;

	@Autowired
	protected MessageSource messageSource;

	// --------------------------------- Test Data

	protected Random random = new Random();

	protected AppUser user;

	// --------------------------------- Test Methods

	@Before
	@WithMockUser(UNIT_TEST_USER)
	public void login() throws Exception {
		user = appUserDAO.findByUsername(UNIT_TEST_USER, false);
	}

	/**
	 * @return the complete set of roles and permissions for use here and in
	 *         subclasses that may test security functions.
	 */
	public static Set<String> getAllDefaultAuthorities() {
		Set<String> allAuthorities = new HashSet<>();

		/*
		 * This commented code retrieves the values from the database; since we
		 * have constants / enums that represent the values in the DB, we can
		 * use those for efficiency. CPB
		 */

		// List<TeamRole> allRoles = roleDAO.findAll();
		// for (TeamRole role : allRoles)
		// {
		// Set<Permission> permissions = role.getPermissions();
		// for (Permission p : permissions)
		// {
		// allAuthorities.add(p.getName());
		// }
		// }

		for (RoleType type : RoleType.values())
			allAuthorities.add(type.getName());

		Map<String, Object> constantMap = new HashMap<>();
		WebUtil.addClassConstantsToModel(PermissionType.class, constantMap);
		for (Object val : constantMap.values())
			allAuthorities.add((String) val);
		return allAuthorities;
	}

	// ----------------------------- Utility Methods

	protected Precinct getPrecinct() throws Exception {
		return precinctDAO.findByCriteria(TEST_PRECINCT, null).first();
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

}
