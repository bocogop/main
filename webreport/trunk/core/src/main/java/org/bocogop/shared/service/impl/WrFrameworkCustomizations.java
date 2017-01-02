package org.bocogop.shared.service.impl;

import java.time.ZoneId;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.bocogop.shared.model.AppUser;
import org.bocogop.shared.service.UserAdminCustomizations;
import org.bocogop.shared.web.AuthenticationCustomizations;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class WrFrameworkCustomizations extends AbstractServiceImpl
		implements UserAdminCustomizations, AuthenticationCustomizations {

	@Value("${userAdmin.newUserDefaultTimezone}")
	private ZoneId newUserDefaultTimezone;

	@Override
	public AppUser userRetrievedCallback(AppUser u, Map<String, Object> userAdminCustomizationsModel) {
		return u;
	}

	@Override
	public AppUser userCreatedCallback(AppUser u, Map<String, Object> userAdminCustomizationsModel) {
		u.setTimeZone(newUserDefaultTimezone);
		u = appUserDAO.saveOrUpdate(u);
		return u;
	}

	@Override
	public void userDeletedCallback(long appUserId, Map<String, Object> userAdminCustomizationsModel) {
	}

	@Override
	public void successfulAuthenticationCallback(HttpServletRequest request, Authentication authResult,
			Map<String, Object> authCustomizationsModel) {
	}

}
