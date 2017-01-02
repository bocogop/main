package org.bocogop.wr.service.email.impl;

import org.bocogop.shared.service.impl.EmailServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;

@Service
@Profile({ "default" })
public class TestEmailServiceImpl extends EmailServiceImpl {
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(TestEmailServiceImpl.class);

	@Autowired
	private TestEmailServiceAsyncHelper asyncHelper;

	/*
	 * Change this value at any time in any unit test and it will be preserved
	 * during the execution of all subsequent tests - CPB
	 */
	public static boolean sendEmail = false;

	/* Change this value to simulate async delay in each email thread */
	public static boolean emulateDelay = false;

	@Override
	public ListenableFuture<Object> sendEmail(final String subject, final String text, final String[] toRecipients,
			final String[] bccRecipients) {
		if (sendEmail) {
			return super.sendEmail(subject, text, toRecipients, bccRecipients);
		}

		return asyncHelper.sendEmail(subject, text, toRecipients, bccRecipients);
	}

}
