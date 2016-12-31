package org.bocogop.wr.model;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.PUBLIC_ONLY;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.bocogop.wr.model.core.AbstractAuditedVersionedPersistent;
import org.hibernate.annotations.Type;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "AppUserPreferences", schema = "Core")
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = PUBLIC_ONLY, isGetterVisibility = PUBLIC_ONLY)
public class AppUserPreferences extends AbstractAuditedVersionedPersistent<AppUserPreferences> {
	private static final long serialVersionUID = 1L;

	// ------------------------------------- Fields

	private AppUser appUser;

	// ------------------------------------- Business Methods

	// ------------------------------------- Common Methods

	@Override
	protected boolean requiredEquals(AppUserPreferences oo) {
		return new EqualsBuilder().append(nullSafeGetId(getAppUser()), nullSafeGetId(oo.getAppUser())).isEquals();
	}

	@Override
	protected int requiredHashCode() {
		return new HashCodeBuilder().append(nullSafeGetId(getAppUser())).toHashCode();
	}

	// ------------------------------------- Accessor Methods

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "AppUserFK", nullable = false, unique = true)
	@JsonIgnore
	public AppUser getAppUser() {
		return appUser;
	}

	public void setAppUser(AppUser appUser) {
		this.appUser = appUser;
	}

}
