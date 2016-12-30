package org.bocogop.wr.config;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.bocogop.wr.web.ajax.AjaxRequestHandler;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;

public class AjaxAwareAuthenticationEntryPoint extends LoginUrlAuthenticationEntryPoint {
	public AjaxAwareAuthenticationEntryPoint(String loginUrl) {
		super(loginUrl);
	}

	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException authException) throws IOException, ServletException {
		if (AjaxRequestHandler.isAjax(request)) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN, "Request Denied (Session Expired)");
		} else {
			super.commence(request, response, authException);
		}
	}
}