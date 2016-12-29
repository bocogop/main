package org.bocogop.wr.service.email;

import java.util.Map;

import org.springframework.util.concurrent.ListenableFuture;

import org.bocogop.wr.service.email.EmailType.InvalidParametersException;

public interface EmailService {

	ListenableFuture<Object> sendEmail(EmailType type, String[] toRecipients, String[] bccRecipients,
			Map<String, Object> params) throws InvalidParametersException;

	ListenableFuture<Object> sendEmail(String subject, String emailBody, String[] toRecipients, String[] bccRecipients);

}
