package org.bocogop.wr.service;

import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.transaction.BeforeTransaction;

import org.bocogop.wr.AbstractTransactionalWebTest;

@Ignore
public class TestExcludedEntityService extends AbstractTransactionalWebTest {

	@Autowired
	private ExcludedEntityService service;

	/*
	 * Don't want to run this every unit test run, but nice to have for
	 * debugging - CPB
	 */

	@BeforeTransaction
	public void testJob() throws Exception {
		service.refreshDataAndUpdateVolunteers();
	}

	@Test
	public void fakeJob() {
	}
}
