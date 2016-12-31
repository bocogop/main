package org.bocogop.wr.web.interceptor;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
import org.bocogop.wr.model.AppUser;
import org.bocogop.wr.model.lookup.TemplateType;
import org.bocogop.wr.util.SecurityUtil;
import org.bocogop.wr.web.AbstractAppController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;

@Component
public class CommonReferenceDataInterceptor extends AbstractReferenceDataInterceptor {
	private static final Logger log = LoggerFactory.getLogger(AbstractReferenceDataInterceptor.class);

	@Value("${session.idleAfterSeconds}")
	private int sessionIdleAfterSeconds;
	@Value("${session.expirationSeconds}")
	private int sessionExpirationSeconds;

	@Override
	protected void addAdditionalReferenceData(HttpServletRequest request, HttpServletResponse response, Object handler,
			ModelAndView modelAndView) throws Exception {
		HttpSession session = request.getSession();

		session.setMaxInactiveInterval(sessionExpirationSeconds);
		session.setAttribute("sessionIdleAfterSeconds", new Integer(sessionIdleAfterSeconds));

		Map<String, Object> model = modelAndView.getModel();

		AppUser appUser = SecurityUtil.getCurrentUserAs(AppUser.class);

		ZoneId timeZone = null;
		if (appUser != null) {
			model.put("currentUser", appUser);
			model.put("multipleStationsAssigned", appUser.getPrecincts().size() > 1);
			timeZone = appUser.getTimeZone();

			if (timeZone != null) {
				model.put("userTimeZone", timeZone);
				model.put("userTimeZoneName", timeZone.getDisplayName(TextStyle.FULL, Locale.US));
			}
		}

		ZonedDateTime now = ZonedDateTime.now();
		if (timeZone != null)
			now = now.withZoneSameInstant(timeZone);
		model.put("currentTime", now);

		/* Sensible default for consistency in JSP layer - CPB */
		if (model.get(AbstractAppController.FORM_READ_ONLY) == null)
			model.put(AbstractAppController.FORM_READ_ONLY, false);

		/* Velocity caches these vals per the engine setup - CPB */
		model.put("referenceDataLinks",
				velocityService.mergeTemplateIntoString(TemplateType.REFERENCE_DATA_LINKS.getName()));
		model.put("footerContent", velocityService.mergeTemplateIntoString(TemplateType.FOOTER_CONTENT.getName()));
		model.put("systemNotification",
				StringUtils.trim(velocityService.mergeTemplateIntoString(TemplateType.SYSTEM_NOTIFICATION.getName())));
	}

}
