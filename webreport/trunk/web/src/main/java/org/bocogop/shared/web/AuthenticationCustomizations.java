package org.bocogop.shared.web;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.security.core.Authentication;

public interface AuthenticationCustomizations {

	void successfulAuthenticationCallback(HttpServletRequest request, Authentication authResult, Map<String, Object> authCustomizationsModel);

}