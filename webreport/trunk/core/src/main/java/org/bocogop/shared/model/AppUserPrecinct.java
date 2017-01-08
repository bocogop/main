package org.bocogop.shared.model;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.PUBLIC_ONLY;

import java.util.Comparator;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.bocogop.shared.model.precinct.Precinct;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Type;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "AppUserPrecinct")
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = PUBLIC_ONLY, isGetterVisibility = PUBLIC_ONLY)
public class AppUserPrecinct extends AbstractAuditedPersistent<AppUserPrecinct> {
	private static final long serialVersionUID = 4928083791827168547L;

	/*
	 * Exposing this functionality on a per-request basis since the precinct
	 * field may be lazy-loaded - CPB
	 */
	public static class CompareByPrecinct implements Comparator<AppUserPrecinct> {
		@Override
		public int compare(AppUserPrecinct o1, AppUserPrecinct o2) {
			if (o1.equals(o2))
				return 0;
			return new CompareToBuilder()
					.append(o1 == null ? null : o1.getPrecinct(), o2 == null ? null : o2.getPrecinct())
					.toComparison() > 0 ? 1 : -1;
		}
	}

	// ------------------------------------- Fields

	private AppUser appUser;
	private Precinct precinct;
	private boolean primaryPrecinct;

	// ------------------------------------- Constructors

	public AppUserPrecinct() {
	}

	public AppUserPrecinct(AppUser user, Precinct w) {
		this.appUser = user;
		this.precinct = w;
	}

	// ------------------------------------- Business Methods

	public void initializeAll() {
		/*
		 * This call relies on L2 cache to be performant - Hibernate doesn't
		 * Batch-load all children here for some reason - CPB
		 */
		initialize(getPrecinct());
	}

	// ------------------------------------- Common Methods

	@Override
	protected boolean requiredEquals(AppUserPrecinct oo) {
		return new EqualsBuilder().append(nullSafeGetId(getAppUser()), nullSafeGetId(oo.getAppUser()))
				.append(nullSafeGetId(getPrecinct()), nullSafeGetId(oo.getPrecinct())).isEquals();
	}

	@Override
	protected int requiredHashCode() {
		return new HashCodeBuilder().append(nullSafeGetId(getAppUser())).append(nullSafeGetId(getPrecinct()))
				.toHashCode();
	}

	// ------------------------------------- Accessor Methods

	@Column(name = "PrimaryPrecinctInd")
	@Type(type = "yes_no")
	public boolean isPrimaryPrecinct() {
		return primaryPrecinct;
	}

	public void setPrimaryPrecinct(boolean primaryPrecinct) {
		this.primaryPrecinct = primaryPrecinct;
	}

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "AppUserFK", nullable = false, updatable = false)
	@JsonIgnore
	public AppUser getAppUser() {
		return appUser;
	}

	public void setAppUser(AppUser appUser) {
		this.appUser = appUser;
	}

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "PrecinctFK", nullable = false, updatable = false)
	@BatchSize(size = 500)
	public Precinct getPrecinct() {
		return precinct;
	}

	public void setPrecinct(Precinct precinct) {
		this.precinct = precinct;
	}

}
