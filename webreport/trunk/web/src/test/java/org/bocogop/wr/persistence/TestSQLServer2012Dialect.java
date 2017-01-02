package org.bocogop.wr.persistence;

import java.sql.Timestamp;
import java.util.List;

import javax.persistence.Query;

import org.bocogop.shared.model.Role;
import org.bocogop.shared.persistence.SQLServer2012Dialect;
import org.bocogop.wr.AbstractTransactionalCoreTest;
import org.junit.Test;

public class TestSQLServer2012Dialect extends AbstractTransactionalCoreTest {

	@Test
	public void testCustomFunctions() throws Exception {
		for (String datepart : SQLServer2012Dialect.SUPPORTED_DATE_PARTS) {
			Query q = em
					.createQuery("select add_" + datepart + "(1,'2012-01-10') from " + Role.class.getName());
			@SuppressWarnings("unchecked")
			List<Timestamp> resultList = q.getResultList();
			System.out.println("2012-01-10 + 1 " + datepart + " = " + resultList.get(0));
		}
	}
}
