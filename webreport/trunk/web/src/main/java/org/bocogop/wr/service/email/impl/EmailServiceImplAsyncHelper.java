package org.bocogop.wr.service.email.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;

@Service
public class EmailServiceImplAsyncHelper {
	private static final Logger log = LoggerFactory.getLogger(EmailServiceImplAsyncHelper.class);

	@Autowired
	private JavaMailSender mailSender;

	@Async("emailServiceTaskExecutor")
	public ListenableFuture<Object> send(MimeMessagePreparator preparator) {
		try {
			mailSender.send(preparator);
		} catch (MailException e) {
			log.error("There was an error sending an email message:", e);
		}
		return new AsyncResult<Object>(null);
	}

}