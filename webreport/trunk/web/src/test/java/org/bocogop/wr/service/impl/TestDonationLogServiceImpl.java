package org.bocogop.wr.service.impl;

import java.util.List;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.bocogop.wr.AbstractTransactionalWebTest;
import org.bocogop.wr.model.donation.DonationLog;
import org.bocogop.wr.service.DonationLogService;

public class TestDonationLogServiceImpl extends AbstractTransactionalWebTest {

	@Autowired
	private DonationLogService service;

	@Test
	public void test() throws Exception {
		List<DonationLog> list = service
				.importExternalDonations(ClassLoader.class.getResourceAsStream("/test_donation_log_file.xml"));
		for (DonationLog l : list) {
			System.out.println(l);
		}
	}

}
