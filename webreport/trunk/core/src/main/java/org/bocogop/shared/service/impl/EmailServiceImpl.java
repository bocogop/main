package org.bocogop.shared.service.impl;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.bocogop.shared.service.VelocityService;
import org.bocogop.shared.service.email.EmailService;
import org.bocogop.shared.service.email.EmailType;
import org.bocogop.shared.service.email.EmailType.InvalidParametersException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;

@Service
@Profile({ "attended" })
public class EmailServiceImpl implements EmailService {
	private static final Logger log = LoggerFactory.getLogger(EmailServiceImpl.class);

	public static final String PARAM_NAME_WEB_URL = "appWebURL";

	public static final String TEMPLATE_NAME_HTML_PREFIX = "email_html_prefix";
	public static final String TEMPLATE_NAME_HTML_SUFFIX = "email_html_suffix";
	public static final String CONTENT_ID_APP_LOGO = "header";
	public static final String CONTENT_ID_VA_LOGO = "valogo";

	@Autowired
	protected VelocityService velocityService;

	@Value("${email.sender}")
	private String emailSender;
	@Value("${email.recipientOverrideCSV}")
	private String recipientEmailOverrideCSV;
	@Value("${email.subjectPrefix}")
	private String subjectPrefix;

	@Autowired
	private EmailServiceImplAsyncHelper asyncHelper;

	@Override
	public ListenableFuture<Object> sendEmail(final EmailType type, final String[] toRecipients,
			final String[] bccRecipients, final Map<String, Object> params) throws InvalidParametersException {
		type.validateParams(params);

		Map<String, Object> finalParams = new HashMap<>();
		finalParams.putAll(params);

		String subject = velocityService.mergeTemplateIntoString(type.getSubjectTemplateName(), finalParams);
		String text = velocityService.mergeTemplateIntoString(type.getBodyTemplateName(), finalParams);
		return sendEmail(subject, text, toRecipients, bccRecipients);
	}

	public ListenableFuture<Object> sendEmail(final String subject, final String text, final String[] toRecipients,
			final String[] bccRecipients) {
		final boolean overrideRecipients = StringUtils.isNotEmpty(recipientEmailOverrideCSV);

		String prefix = velocityService.mergeTemplateIntoString(TEMPLATE_NAME_HTML_PREFIX);
		String suffix = velocityService.mergeTemplateIntoString(TEMPLATE_NAME_HTML_SUFFIX);

		final StringBuilder content = new StringBuilder();
		content.append(prefix);
		content.append(text);

		if (overrideRecipients) {
			content.append("<hr>Original intended recipients:<p><table align='center'>");
			if (toRecipients != null)
				content.append("<tr><td align='top'>To:</td><td><ul><li>")
						.append(StringUtils.join(toRecipients, "<li>")).append("</ul></td></tr>");
			if (bccRecipients != null)
				content.append("<tr><td align='top'>BCC:</td><td><ul><li>")
						.append(StringUtils.join(bccRecipients, "<li>")).append("</ul></td></tr>");
			content.append("</table>");
		}

		content.append(suffix);

		MimeMessagePreparator preparator = new MimeMessagePreparator() {
			@Override
			public void prepare(javax.mail.internet.MimeMessage mimeMessage) throws Exception {
				MimeMessageHelper message = new MimeMessageHelper(mimeMessage, true);

				if (overrideRecipients) {
					String[] recipients = recipientEmailOverrideCSV.split(",");
					message.setTo(recipients);
				} else {
					if (toRecipients != null)
						message.setTo(toRecipients);
					if (bccRecipients != null)
						message.setBcc(bccRecipients);
				}

				message.setFrom(emailSender);
				message.setSubject(subjectPrefix + " " + subject);

				if (log.isDebugEnabled())
					log.debug("Sending email with TO recipients ({}), BCC recipients ({}), FROM {} with body:\n{}",
							StringUtils.join(toRecipients), StringUtils.join(bccRecipients), emailSender, text);

				message.setText(content.toString(), true);
				// message.addInline(CONTENT_ID_APP_LOGO, new
				// ClassPathResource("images/APP.logo.jpg"));
				message.addInline(CONTENT_ID_VA_LOGO, new ClassPathResource("images/va.logo.jpg"));
			}
		};

		return asyncHelper.send(preparator);
	}

}
