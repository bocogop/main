package org.bocogop.wr.service.email;

import java.util.Map;

import org.bocogop.wr.service.email.EmailType.InvalidParametersException;
import org.springframework.util.concurrent.ListenableFuture;

public interface EmailService {

	ListenableFuture<Object> sendEmail(EmailType type, String[] toRecipients, String[] bccRecipients,
			Map<String, Object> params) throws InvalidParametersException;

	ListenableFuture<Object> sendEmail(String subject, String emailBody, String[] toRecipients, String[] bccRecipients);

}
