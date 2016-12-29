package org.bocogop.shared.model;

import java.time.ZoneId;
import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public interface CoreUserDetails extends UserDetails {

	Collection<? extends GrantedAuthority> getAuthoritiesAtFacility(long facilityId);

	Long getId();

	/*
	 * May return null, in which case the system will use the default timezone
	 * for parsing and formatting dates - CPB
	 */
	ZoneId getTimeZone();
	
	boolean isSoundsEnabled();
	
	String getDisplayName();

	boolean isNationalAdmin();
	
}
