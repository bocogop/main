package org.bocogop.wr.model;

import java.time.ZoneId;

import org.springframework.security.core.userdetails.UserDetails;

public interface CoreUserDetails extends UserDetails {

	Long getId();

	/*
	 * May return null, in which case the system will use the default timezone
	 * for parsing and formatting dates - CPB
	 */
	ZoneId getTimeZone();

	String getDisplayName();

	boolean isNationalAdmin();

}
