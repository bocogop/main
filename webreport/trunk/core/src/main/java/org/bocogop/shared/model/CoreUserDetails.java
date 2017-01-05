package org.bocogop.shared.model;

import java.time.ZoneId;

import org.springframework.security.core.userdetails.UserDetails;

public interface CoreUserDetails<T extends CoreUserDetails<T>> extends UserDetails {

	Long getId();

	String getDisplayName();

	boolean isNationalAdmin();

	ZoneId getTimeZone();

}
