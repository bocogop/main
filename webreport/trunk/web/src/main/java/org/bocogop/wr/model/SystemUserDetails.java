package org.bocogop.wr.model;

import java.time.ZoneId;
import java.util.Collection;

import org.bocogop.shared.model.CoreUserDetails;
import org.springframework.security.core.GrantedAuthority;

public class SystemUserDetails implements CoreUserDetails {
	private static final long serialVersionUID = 5238699990913115861L;

	private Collection<? extends GrantedAuthority> authorities;

	public SystemUserDetails(Collection<? extends GrantedAuthority> authorities) {
		this.authorities = authorities;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return authorities;
	}

	@Override
	public String getPassword() {
		return null;
	}

	@Override
	public String getUsername() {
		return "System";
	}

	@Override
	public boolean isNationalAdmin() {
		return false;
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public Long getId() {
		return -1L;
	}

	@Override
	public ZoneId getTimeZone() {
		return ZoneId.systemDefault();
	}

	@Override
	public boolean isSoundsEnabled() {
		return false;
	}

	@Override
	public String getDisplayName() {
		return getUsername();
	}

}
