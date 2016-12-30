package org.bocogop.wr.web.interceptor;

import java.net.URLEncoder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
import org.bocogop.wr.util.context.SessionUtil;
import org.bocogop.wr.web.conversion.interceptor.AbstractInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

/**
 * Interceptor which logs the most recent GET request URL in the session. This
 * is helpful when we want to take the user back after an
 * OptimisticLockException, for example. We don't use the most recent breadcrumb
 * because we don't want to introduce a dependency on the calling page to
 * populate the breadcrumb (e.g. if we're on step 2 of a 3 step wizard" flow or
 * something). CPB
 */
@Component
public class StoreLastGetRequestInterceptor extends AbstractInterceptor {
	private static final Logger log = LoggerFactory.getLogger(StoreLastGetRequestInterceptor.class);

	// ------------------------------------- Static Fields

	public static final String SESSION_ATTRIBUTE_LAST_GET_REQUEST = "last_get_request_url";

	// ------------------------------------- Business Methods

	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
			ModelAndView modelAndView) throws Exception {
		/*
		 * We don't want to add these attributes to specific ajax calls, but
		 * only to main page GET calls - CPB
		 */
		if (isAjax(request))
			return;

		if (!RequestMethod.GET.name().equals(request.getMethod()))
			return;

		HttpSession session = SessionUtil.getHttpSession();
		if (session == null) {
			log.warn("No session was found so the last GET request was not stored.");
			return;
		}
		
		StringBuffer requestURL = request.getRequestURL();
		String queryString = request.getQueryString();
		if (StringUtils.isNotBlank(queryString))
			requestURL.append("?").append(queryString);
		
		String relativeLink = requestURL.toString();
		relativeLink = URLEncoder.encode(relativeLink, "UTF-8");
		log.debug("Storing last GET URL in session: {}", relativeLink);

		session.setAttribute(SESSION_ATTRIBUTE_LAST_GET_REQUEST, relativeLink);
	}

}
