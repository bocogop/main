package org.bocogop.wr.web;

import java.util.Locale;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.support.RequestContextUtils;

@Component
public class UserNotifier {

	public static final String ONE_TIME_USER_NOTIFICATION = "one-time-user-notification";

	@Resource(name = "messageSource")
	private MessageSource source;

	public static String getAndRemoveAttributeFromSession(HttpSession session) {
		String val = (String) session.getAttribute(ONE_TIME_USER_NOTIFICATION);
		if (val != null)
			session.removeAttribute(ONE_TIME_USER_NOTIFICATION);
		return val;
	}

	public void notifyUserOnceWithMessage(HttpServletRequest request, String resolvedMessage) {
		notifyUserOnce(getSession(request), resolvedMessage);
	}

	private HttpSession getSession(HttpServletRequest request) {
		return request.getSession(true);
	}

	public void notifyUserOnce(HttpServletRequest request, String messageCode, Object... messageArgs) {
		Locale locale = getLocale(request);
		String resolvedMessage = source.getMessage(messageCode, messageArgs, locale);
		notifyUserOnce(getSession(request), resolvedMessage);
	}

	public void notifyUserOnce(HttpServletRequest request, MessageSourceResolvable r) {
		Locale locale = getLocale(request);
		String resolvedMessage = source.getMessage(r, locale);
		notifyUserOnce(getSession(request), resolvedMessage);
	}

	public static void notifyUserOnce(HttpSession session, String message) {
		session.setAttribute(ONE_TIME_USER_NOTIFICATION, message);
	}

	private Locale getLocale(HttpServletRequest request) {
		Locale locale = request == null ? Locale.getDefault() : RequestContextUtils.getLocale(request);
		return locale;
	}
}
