package org.bocogop.shared.util;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public class ServletUtil {

	public static HttpServletRequest getThreadBoundServletRequest() {
		RequestAttributes requestAttr = RequestContextHolder.getRequestAttributes();
		if (requestAttr == null)
			return null;

		if (!(requestAttr instanceof ServletRequestAttributes)) {
			throw new IllegalStateException("Current request is not a servlet request");
		}
		ServletRequestAttributes servletRequestAttrs = (ServletRequestAttributes) requestAttr;
		HttpServletRequest servletRequest = servletRequestAttrs.getRequest();
		return servletRequest;
	}

}
