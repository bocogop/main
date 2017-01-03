package org.bocogop.shared.model.voter;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.PUBLIC_ONLY;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.bocogop.shared.model.CoreUserDetails;
import org.bocogop.shared.model.Participation;
import org.hibernate.annotations.BatchSize;
import org.springframework.security.core.GrantedAuthority;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * @author Connor
 *
 */
@Entity
@Table(name = "Voter")
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = PUBLIC_ONLY, isGetterVisibility = PUBLIC_ONLY)
public class Voter extends AbstractVoter<Voter> implements CoreUserDetails {
	private static final long serialVersionUID = 6904844123870655771L;

	public static class VoterView {
		public interface Basic {
		}

		public interface Search extends Basic {
		}

		public interface Extended extends Search {
		}

		public interface Demographics extends Basic {
		}
	}

	// -------------------------------------- Fields

	private List<Participation> participations;

	/* Transient security fields */
	private Collection<? extends GrantedAuthority> authorities = null;

	// -------------------------------------- Constructors

	public Voter() {
	}

	public Voter(String lastName, String firstName, String middleName, String nameSuffix) {
		super(lastName, firstName, middleName, nameSuffix);
	}

	// -------------------------------------- Business Methods

	@Override
	@Transient
	@JsonIgnore
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return authorities;
	}

	public void setAuthorities(Collection<? extends GrantedAuthority> authorities) {
		if (this.authorities != null)
			throw new IllegalStateException(
					"Cannot assign new authorities to Voter once they have been assigned once.");
		this.authorities = authorities;
	}

	@Transient
	@Override
	@JsonIgnore
	public String getPassword() {
		Integer birthYear = getBirthYear();
		if (birthYear == null)
			return "_INVALID_password*#@$&(#@*)$&!!--1NKWE";
		return String.valueOf(birthYear);
	}

	@Override
	@Transient
	public String getUsername() {
		return getVoterId();
	}

	@Override
	@Transient
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	@Transient
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	@Transient
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	@Transient
	public boolean isEnabled() {
		return true;
	}

	@Transient
	@Override
	public boolean isNationalAdmin() {
		return false;
	}

	@Override
	@Transient
	public ZoneId getTimeZone() {
		// TODO BOCOGOP hardcoded for now - CPB
		return ZoneId.of("America/Denver");
	}

	@OneToMany(mappedBy = "voter", fetch = FetchType.LAZY)
	@BatchSize(size = 500)
	@JsonIgnore
	public List<Participation> getParticipations() {
		if (participations == null)
			participations = new ArrayList<>();
		return participations;
	}

	public void setParticipations(List<Participation> participations) {
		this.participations = participations;
	}

}
