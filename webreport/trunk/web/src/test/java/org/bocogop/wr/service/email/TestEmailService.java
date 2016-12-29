package org.bocogop.wr.service.email;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;

import org.bocogop.wr.AbstractWebAppTest;
import org.bocogop.wr.service.email.impl.TestEmailServiceImpl;
import org.bocogop.wr.util.DateUtil;

@Component
@Ignore
public class TestEmailService extends AbstractWebAppTest {

	@Autowired
	private EmailService emailService;

	@Test
	public void testSendEmail() throws Exception {
		boolean previousSendEmailVal = TestEmailServiceImpl.sendEmail;
		TestEmailServiceImpl.sendEmail = true;

		try {
			Map<String, Object> params = new HashMap<>();
			params.put("successfulStations", Arrays.asList("442"));
			params.put("unsuccessfulStations", Arrays.asList("552"));
			params.put("startTime", ZonedDateTime.of(2012, 1, 1, 0, 0, 0, 0, DateUtil.UTC));
			params.put("completionTime", ZonedDateTime.of(2012, 1, 1, 3, 45, 0, 0, DateUtil.UTC));

			ListenableFuture<Object> result = emailService.sendEmail(
					EmailType.BATCH_PROCESS_COMPLETION_APP_USERS_UPDATED, new String[] { "connor.barry@va.gov" }, null,
					params);
			result.get();
		} finally {
			TestEmailServiceImpl.sendEmail = previousSendEmailVal;
		}
	}

}
