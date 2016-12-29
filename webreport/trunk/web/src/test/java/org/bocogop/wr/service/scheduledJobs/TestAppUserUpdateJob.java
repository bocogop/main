package org.bocogop.wr.service.scheduledJobs;

import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.bocogop.wr.AbstractTransactionalWebTest;

@Ignore
public class TestAppUserUpdateJob extends AbstractTransactionalWebTest {

	@Autowired
	private AppUserUpdateJob job;

	/*
	 * Don't want to run this every unit test run, but nice to have for
	 * debugging - CPB
	 */

	@Test
	public void testUpdateAllAppUsers() {
		job.updateAllAppUsers();
	}
}
