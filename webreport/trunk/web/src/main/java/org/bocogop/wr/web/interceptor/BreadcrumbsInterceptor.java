package org.bocogop.wr.web.interceptor;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.bocogop.wr.util.context.SessionUtil;
import org.bocogop.wr.web.CommonController;
import org.bocogop.wr.web.breadcrumbs.Breadcrumbs;
import org.bocogop.wr.web.breadcrumbs.Breadcrumbs.Breadcrumb;
import org.bocogop.wr.web.breadcrumbs.Link;
import org.bocogop.wr.web.conversion.interceptor.AbstractInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

@Component
public class BreadcrumbsInterceptor extends AbstractInterceptor {
	private static final Logger log = LoggerFactory.getLogger(BreadcrumbsInterceptor.class);

	// ------------------------------------- Static Fields

	private static final String REQUEST_BREADCRUMB_KEY = "request_key_breadcrumb";

	private static final String SESSION_ATTRIBUTE_BREADCRUMBS = "session_key_breadcrumbs";

	public static final String MODEL_ATTRIBUTE_NAME = "app_breadcrumbs";

	public static final String CURRENT_BREADCRUMB_URL = "current_breadcrumb";
	public static final String MOST_RECENT_BREADCRUMB_URL = "most_recent_breadcrumb";

	// ------------------------------------- Static Methods

	public static void setRequestBreadcrumb(ServletRequest request, String breadcrumb) {
		request.setAttribute(REQUEST_BREADCRUMB_KEY, breadcrumb);
	}

	@Value("${protocolHostnamePortOverride}")
	private String protocolHostnamePortOverride;

	// ------------------------------------- Business Methods

	public void updateCurrentBreadcrumbParameters(HttpSession session, Map<String, Object> newParameters) {
		if (newParameters == null || newParameters.isEmpty())
			return;

		Breadcrumbs breadcrumbs = (Breadcrumbs) session.getAttribute(SESSION_ATTRIBUTE_BREADCRUMBS);
		if (breadcrumbs == null)
			return;

		List<Breadcrumb> breadcrumbList = breadcrumbs.getBreadcrumbs();
		if (breadcrumbList.isEmpty())
			return;

		Breadcrumb current = breadcrumbList.get(breadcrumbList.size() - 1);
		Link l = current.getLink();

		try {
			URIBuilder b = new URIBuilder(l.getHref());
			List<NameValuePair> nvps = new ArrayList<>();
			newParameters.forEach((name, vals) -> {
				if (vals instanceof Object[]) {
					for (Object val : (Object[]) vals)
						if (val != null)
							nvps.add(new BasicNameValuePair(name, String.valueOf(val)));
				} else if (vals instanceof Object) {
					nvps.add(new BasicNameValuePair(name, String.valueOf(vals)));
				} else
					throw new IllegalArgumentException(
							"The map value for key '" + name + "' was not a String or String[]");
			});
			b.setParameters(nvps);
			l.setHref(b.build().toString());
		} catch (URISyntaxException e) {
			log.error("Couldn't update the current breadcrumb parameters", e);
			return;
		}
	}

	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
			ModelAndView modelAndView) throws Exception {
		/*
		 * We don't want to add these attributes to specific ajax calls, but
		 * only to main page render calls - CPB
		 */
		if (isAjax(request))
			return;

		/*
		 * Whether GET or POST, ensure a Breadcrumbs is in the session and /home
		 * is the first entry
		 */
		HttpSession session = SessionUtil.getHttpSession();
		if (session == null) {
			log.warn("No session was found so no breadcrumbs were set.");
			return;
		}

		Breadcrumbs breadcrumbs = (Breadcrumbs) session.getAttribute(SESSION_ATTRIBUTE_BREADCRUMBS);
		if (breadcrumbs == null) {
			String servletContextPath = request.getServletContext().getContextPath();

			breadcrumbs = new Breadcrumbs();
			breadcrumbs.navigate(servletContextPath + CommonController.URL_HOME, handler,
					CommonController.BREADCRUMB_HOME);
			session.setAttribute(SESSION_ATTRIBUTE_BREADCRUMBS, breadcrumbs);
		}

		/* If we have a GET request, add the URL to the breadcrumbs */
		if ("GET".equalsIgnoreCase(request.getMethod())) {
			String breadcrumb = (String) request.getAttribute(BreadcrumbsInterceptor.REQUEST_BREADCRUMB_KEY);
			if (breadcrumb != null)
				breadcrumbs.navigate(request, handler, breadcrumb, protocolHostnamePortOverride);
		}

		if (modelAndView == null) {
			log.debug("No modelAndView returned, the controller must have dealt with the response manually.");
			return;
		}

		/*
		 * Regardless of GET or POST, if we have a model, populate the
		 * breadcrumbs in it. POST is only needed in case we have a Spring
		 * validation error and a POST method returns a view (and not a
		 * redirect). CPB
		 */
		if ("POST".equalsIgnoreCase(request.getMethod()) && modelAndView.hasView()
				&& (StringUtils.startsWithIgnoreCase(modelAndView.getViewName(), "redirect:")
						|| (modelAndView.getView() != null && modelAndView.getView() instanceof RedirectView))) {
			return;
		}

		List<Breadcrumbs.Breadcrumb> breadcrumbList = breadcrumbs.getBreadcrumbs();
		String currentLinkHref = null;
		String penultimateLinkHref = null;

		if (breadcrumbList != null) {
			if (breadcrumbList.size() > 0) {
				Breadcrumbs.Breadcrumb currentDestination = breadcrumbList.get(breadcrumbList.size() - 1);
				if (currentDestination != null)
					currentLinkHref = currentDestination.getLink().getHref();
			}
			if (breadcrumbList.size() > 1) {
				Breadcrumbs.Breadcrumb penultimateDestination = breadcrumbList.get(breadcrumbList.size() - 2);
				if (penultimateDestination != null)
					penultimateLinkHref = penultimateDestination.getLink().getHref();
			}
		}

		Map<String, Object> model = modelAndView.getModel();
		if (model != null) {
			model.put(MODEL_ATTRIBUTE_NAME, breadcrumbs);
			if (currentLinkHref != null)
				model.put(CURRENT_BREADCRUMB_URL, currentLinkHref);
			if (penultimateLinkHref != null)
				model.put(MOST_RECENT_BREADCRUMB_URL, penultimateLinkHref);
		}

	}

}
