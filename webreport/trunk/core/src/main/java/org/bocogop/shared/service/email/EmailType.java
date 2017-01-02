package org.bocogop.shared.service.email;

import java.time.ZonedDateTime;
import java.util.Map;

import org.hibernate.Hibernate;

public enum EmailType {
	BATCH_PROCESS_COMPLETION_APP_USERS_UPDATED("email_batch_done_app_user_update_body",
			"email_batch_done_app_user_update_subject",
			new Object[] { //
					ZonedDateTime.class, "startTime", //
					ZonedDateTime.class, "completionTime" });

	private String bodyTemplateName;
	private String subjectTemplateName;
	/*
	 * An array simulating a method signature - contains pairs of <class,
	 * string> that must exist in the params map when sending this email type -
	 * CPB
	 */
	private Object[] requiredParametersAndTypes;

	private EmailType(String bodyTemplateName, String subjectTemplateName, Object[] requiredParametersAndTypes) {
		this.bodyTemplateName = bodyTemplateName;
		this.subjectTemplateName = subjectTemplateName;
		this.requiredParametersAndTypes = requiredParametersAndTypes;
	}

	public String getBodyTemplateName() {
		return bodyTemplateName;
	}

	public String getSubjectTemplateName() {
		return subjectTemplateName;
	}

	public void validateParams(Map<String, Object> params) throws InvalidParametersException {
		for (int i = 0; i < requiredParametersAndTypes.length - 1; i += 2) {
			Class<?> type = (Class<?>) requiredParametersAndTypes[i];
			String name = (String) requiredParametersAndTypes[i + 1];
			Object val = params.get(name);
			if (val == null || !type.isAssignableFrom(Hibernate.getClass(val)))
				throw new InvalidParametersException(
						"The specified params doesn't contain the required mapping " + name + " -> " + type.getName());
		}
	}

	public static class InvalidParametersException extends RuntimeException {
		private static final long serialVersionUID = -6374807639399542835L;

		public InvalidParametersException(String message) {
			super(message);
		}
	}
}
