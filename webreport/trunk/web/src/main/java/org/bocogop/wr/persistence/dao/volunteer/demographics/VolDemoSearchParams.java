package org.bocogop.wr.persistence.dao.volunteer.demographics;

import java.io.Serializable;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class VolDemoSearchParams implements Serializable {
	private static final long serialVersionUID = -333300450898010419L;

	// never null
	public long workingFacilityId;
	// null if searching by national scope
	public Long facilityId;
	public Map<VolDemoColumn, String> filters = new HashMap<>();
	public String searchValue;
	public int sortColIndex;
	public boolean sortAscending;

	public int[] mostRecentCounts;
	public Map<String, String> restrictions = new HashMap<>();
	public EnumSet<VolDemoColumn> displayCols;

	public VolDemoSearchParams(long workingFacilityId, Long facilityId, Map<VolDemoColumn, String> filters,
			String searchValue, int sortColIndex, boolean sortAscending, Map<String, String> restrictions,
			EnumSet<VolDemoColumn> displayCols) {
		this.workingFacilityId = workingFacilityId;
		this.facilityId = facilityId;
		this.filters = filters;
		this.searchValue = searchValue;
		this.sortColIndex = sortColIndex;
		this.sortAscending = sortAscending;
		this.restrictions = restrictions;
		this.displayCols = displayCols;
	}

	public VolDemoSearchParams() {
		// ensure this never matches any other SearchParams
		this.facilityId = -1L;
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(facilityId).append(filters).append(searchValue).append(sortColIndex)
				.append(sortAscending).append(restrictions).append(displayCols).toHashCode();
	}

	public boolean matchesCountsCriteria(VolDemoSearchParams other) {
		if (this == other)
			return true;
		if (other == null)
			return false;
		return new EqualsBuilder().append(facilityId, other.facilityId).append(filters, other.filters)
				.append(searchValue, other.searchValue).append(restrictions, other.restrictions).isEquals();
	}

	public boolean matchesPagingCriteria(VolDemoSearchParams newSearchParams) {
		if (this == newSearchParams)
			return true;
		if (newSearchParams == null)
			return false;

		for (VolDemoColumn newCol : newSearchParams.displayCols) {
			if (!displayCols.contains(newCol) && !newCol.isAlwaysSelected())
				return false;
		}

		return new EqualsBuilder().append(facilityId, newSearchParams.facilityId)
				.append(filters, newSearchParams.filters).append(searchValue, newSearchParams.searchValue)
				.append(restrictions, newSearchParams.restrictions).append(sortColIndex, newSearchParams.sortColIndex)
				.append(sortAscending, newSearchParams.sortAscending).isEquals();
	}

	public boolean isLocal() {
		return facilityId != null;
	}

	public boolean isIncludeActive() {
		return "true".equals(restrictions.get("includeActive"));
	}

	public boolean isIncludeInactive() {
		return "true".equals(restrictions.get("includeInactive"));
	}

	public boolean isIncludeTerminated() {
		return "true".equals(restrictions.get("includeTerminated"));
	}

	public boolean isIncludeTerminatedByCause() {
		return "true".equals(restrictions.get("includeTerminatedByCause"));
	}

	public boolean onlyActive() {
		return isIncludeActive() && !isIncludeInactive() && !isIncludeTerminated() && !isIncludeTerminatedByCause();
	}
}