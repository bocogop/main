package org.bocogop.wr.service.impl;

import java.time.ZoneId;
import java.util.Map;
import java.util.SortedSet;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import org.bocogop.shared.model.AppUser;
import org.bocogop.shared.model.lookup.sds.VAFacility;
import org.bocogop.shared.service.UserAdminCustomizations;
import org.bocogop.shared.web.AuthenticationCustomizations;
import org.bocogop.wr.model.facility.Facility;

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
	public SortedSet<VAFacility> getAssignableFacilities() {
		return facilityDAO.findVAFacilitiesWithLinkToFacility();
	}

	@Override
	public void userDeletedCallback(long appUserId, Map<String, Object> userAdminCustomizationsModel) {
	}

	@Override
	public void successfulAuthenticationCallback(HttpServletRequest request, Authentication authResult,
			Map<String, Object> authCustomizationsModel) {
		VAFacility vaFacility = org.bocogop.shared.util.context.SessionUtil.getSiteContext(request.getSession());
		if (vaFacility == null)
			return;

		Facility f = facilityDAO.findByVAFacility(vaFacility.getId());
		sessionUtil.setFacilityContext(vaFacility, f, request.getSession());
	}

}
