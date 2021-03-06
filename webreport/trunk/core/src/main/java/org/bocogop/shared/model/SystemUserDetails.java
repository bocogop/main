package org.bocogop.shared.model;

import java.time.ZoneId;
import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;

public class SystemUserDetails implements CoreUserDetails<SystemUserDetails> {
	private static final long serialVersionUID = 5238699990913115861L;

	private Collection<? extends GrantedAuthority> authorities;

	public SystemUserDetails(Collection<? extends GrantedAuthority> authorities) {
		this.authorities = authorities;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return authorities;
	}

	protected void setAuthorities(Collection<? extends GrantedAuthority> authorities) {
		this.authorities = authorities;
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
	public String getDisplayName() {
		return getUsername();
	}

	@Override
	public ZoneId getTimeZone() {
		return ZoneId.systemDefault();
	}

}
