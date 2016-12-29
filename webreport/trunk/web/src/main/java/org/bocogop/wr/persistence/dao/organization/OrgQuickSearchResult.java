package org.bocogop.wr.persistence.dao.organization;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.builder.CompareToBuilder;

import org.bocogop.wr.model.organization.ScopeType;

public class OrgQuickSearchResult implements Comparable<OrgQuickSearchResult> {

	private long id;
	private ScopeType scope;
	private String name;
	private String abbreviation;
	private String facility;
	private boolean active;
	private Boolean volunteerOrgActive;

	public OrgQuickSearchResult(long id, ScopeType scope, String name, String abbreviation, String facility,
			boolean active, Boolean volunteerOrgActive) {
		this.id = id;
		this.scope = scope;
		this.name = name;
		this.abbreviation = abbreviation;
		this.facility = facility;
		this.active = active;
		this.volunteerOrgActive = volunteerOrgActive;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (id ^ (id >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		OrgQuickSearchResult other = (OrgQuickSearchResult) obj;
		if (id != other.id)
			return false;
		return true;
	}

	@Override
	public int compareTo(OrgQuickSearchResult o) {
		if (equals(o))
			return 0;

		return new CompareToBuilder().append(getScope(), o.getScope())
				.append(StringUtils.lowerCase(getName()), StringUtils.lowerCase(o.getName())).append(id, o.id)
				.toComparison() > 0 ? 1 : -1;
	}

	public String getDisplayName() {
		return getName() + " (" + (getFacility() == null ? "National" : getFacility()) + ")";
	}

	public long getId() {
		return id;
	}

	public ScopeType getScope() {
		return scope;
	}

	public String getName() {
		return name;
	}

	public String getAbbreviation() {
		return abbreviation;
	}

	public String getFacility() {
		return facility;
	}

	public boolean isActive() {
		return active;
	}

	public Boolean getVolunteerOrgActive() {
		return volunteerOrgActive;
	}

}