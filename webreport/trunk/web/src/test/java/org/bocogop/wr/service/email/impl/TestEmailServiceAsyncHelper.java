package org.bocogop.wr.service.email.impl;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;

@Service
@Profile({ "default" })
public class TestEmailServiceAsyncHelper {
	private static final Logger log = LoggerFactory.getLogger(TestEmailServiceAsyncHelper.class);

	@Async("emailServiceTaskExecutor")
	public ListenableFuture<Object> sendEmail(String subject, String text, String[] toRecipients,
			String[] bccRecipients) {
		if (TestEmailServiceImpl.emulateDelay)
			/* Simulate delay of sending to real SMTP server */
			try {
				Thread.sleep(1400);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		log.info("Mail sent to " + StringUtils.join(toRecipients, ",") + ": Subject=" + subject + "\n" + text);
		return new AsyncResult<Object>(null);
	}

}
