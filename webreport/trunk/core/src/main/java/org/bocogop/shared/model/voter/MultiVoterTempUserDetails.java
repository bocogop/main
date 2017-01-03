package org.bocogop.shared.model.voter;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.bocogop.shared.model.CoreUserDetails;
import org.bocogop.shared.model.Permission;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

public class MultiVoterTempUserDetails implements CoreUserDetails {
	private static final long serialVersionUID = -7663004971330754422L;

	private List<Voter> matches;

	public MultiVoterTempUserDetails(List<Voter> matches) {
		this.matches = matches;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		Collection<GrantedAuthority> results = new ArrayList<>();
		results.add(new SimpleGrantedAuthority(Permission.LOGIN_KIOSK));
		results.add(new SimpleGrantedAuthority(Permission.SPECIFY_VOTER_MATCH));
		return results;
	}

	@Override
	public String getPassword() {
		return "_unimportant";
	}

	@Override
	public String getUsername() {
		return "_unimportant";
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
		return "Multiple Matching Voters";
	}

	@Override
	public boolean isNationalAdmin() {
		return false;
	}

	@Override
	public ZoneId getTimeZone() {
		return ZoneId.systemDefault();
	}

	public List<Voter> getMatches() {
		if (matches == null)
			matches = new ArrayList<>();
		return matches;
	}

}
