package org.bocogop.kiosk.web.interceptor;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
import org.bocogop.shared.model.lookup.TemplateType;
import org.bocogop.shared.model.voter.Voter;
import org.bocogop.shared.util.SecurityUtil;
import org.bocogop.shared.web.interceptor.AbstractReferenceDataInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.WebUtils;

@Component
public class CommonReferenceDataInterceptor extends AbstractReferenceDataInterceptor {
	private static final Logger log = LoggerFactory.getLogger(AbstractReferenceDataInterceptor.class);

	@Value("${session.kiosk.idleAfterSeconds}")
	private int sessionIdleAfterSeconds;

	@Override
	protected void addAdditionalReferenceData(HttpServletRequest request, HttpServletResponse response, Object handler,
			ModelAndView modelAndView) throws Exception {
		Cookie eventIdCookie = WebUtils.getCookie(request, "eventId");
		Long eventId = eventIdCookie == null ? null : new Long(eventIdCookie.getValue());

		Map<String, Object> model = modelAndView.getModel();

		Voter voterUser = SecurityUtil.getCurrentUserAs(Voter.class);

		if (voterUser != null) {
			model.put("currentUser", voterUser);
		}

		HttpSession session = request.getSession();

		session.setAttribute("sessionIdleAfterSeconds", new Integer(sessionIdleAfterSeconds));

		ZoneId timeZone = voterUser.getTimeZone();

		ZonedDateTime now = ZonedDateTime.now();
		if (timeZone != null)
			now = now.withZoneSameInstant(timeZone);
		model.put("currentTime", now);

		/* Velocity caches these vals per the engine setup - CPB */
		Locale locale = LocaleContextHolder.getLocale();
		model.put("locale", locale.getLanguage());
		model.put("systemNotification", StringUtils.trim(velocityService
				.mergeTemplateIntoString(TemplateType.SYSTEM_NOTIFICATION.getName() + "." + locale.getLanguage())));
	}

}
