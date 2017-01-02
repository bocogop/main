package org.bocogop.shared.model;

import java.time.ZoneId;

import org.springframework.security.core.userdetails.UserDetails;

public interface CoreUserDetails extends UserDetails {

	Long getId();

	String getDisplayName();

	boolean isNationalAdmin();

	ZoneId getTimeZone();

}
