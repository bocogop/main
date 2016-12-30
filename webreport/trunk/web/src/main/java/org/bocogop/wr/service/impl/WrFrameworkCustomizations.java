package org.bocogop.wr.service.impl;

import java.time.ZoneId;
import java.util.Map;
import java.util.SortedSet;

import javax.servlet.http.HttpServletRequest;

import org.bocogop.wr.model.AppUser;
import org.bocogop.wr.model.precinct.Precinct;
import org.bocogop.wr.service.UserAdminCustomizations;
import org.bocogop.wr.web.AuthenticationCustomizations;
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
	public SortedSet<Precinct> getAssignablePrecincts() {
		return precinctDAO.findAllSorted();
	}

	@Override
	public void userDeletedCallback(long appUserId, Map<String, Object> userAdminCustomizationsModel) {
	}

	@Override
	public void successfulAuthenticationCallback(HttpServletRequest request, Authentication authResult,
			Map<String, Object> authCustomizationsModel) {
	}

}
