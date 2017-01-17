package org.bocogop.shared.persistence.dao.voter.demographics;

import java.io.Serializable;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class VoterDemographicsSearchParams implements Serializable {
	private static final long serialVersionUID = -333300450898010419L;

	public Map<VoterDemographicsColumn, String> filters = new HashMap<>();
	public String searchValue;
	public int sortColIndex;
	public boolean sortAscending;

	public int[] mostRecentCounts;
	public Map<String, String> restrictions = new HashMap<>();
	public EnumSet<VoterDemographicsColumn> displayCols;

	public VoterDemographicsSearchParams(Map<VoterDemographicsColumn, String> filters, String searchValue,
			int sortColIndex, boolean sortAscending, Map<String, String> restrictions,
			EnumSet<VoterDemographicsColumn> displayCols) {
		this.filters = filters;
		this.searchValue = searchValue;
		this.sortColIndex = sortColIndex;
		this.sortAscending = sortAscending;
		this.restrictions = restrictions;
		this.displayCols = displayCols;
	}

	public VoterDemographicsSearchParams() {
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(filters).append(searchValue).append(sortColIndex).append(sortAscending)
				.append(restrictions).append(displayCols).toHashCode();
	}

	public boolean matchesCountsCriteria(VoterDemographicsSearchParams other) {
		if (this == other)
			return true;
		if (other == null)
			return false;
		return new EqualsBuilder().append(filters, other.filters).append(searchValue, other.searchValue)
				.append(restrictions, other.restrictions).isEquals();
	}

	public boolean matchesPagingCriteria(VoterDemographicsSearchParams newSearchParams) {
		if (this == newSearchParams)
			return true;
		if (newSearchParams == null)
			return false;

		for (VoterDemographicsColumn newCol : newSearchParams.displayCols) {
			if (!displayCols.contains(newCol) && !newCol.isAlwaysSelected())
				return false;
		}

		return new EqualsBuilder().append(filters, newSearchParams.filters)
				.append(searchValue, newSearchParams.searchValue).append(restrictions, newSearchParams.restrictions)
				.append(sortColIndex, newSearchParams.sortColIndex).append(sortAscending, newSearchParams.sortAscending)
				.isEquals();
	}

}