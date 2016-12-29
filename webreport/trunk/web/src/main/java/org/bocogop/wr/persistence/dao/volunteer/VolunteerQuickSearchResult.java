package org.bocogop.wr.persistence.dao.volunteer;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.PUBLIC_ONLY;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.builder.CompareToBuilder;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonView;

@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = PUBLIC_ONLY, isGetterVisibility = PUBLIC_ONLY)
public class VolunteerQuickSearchResult implements Comparable<VolunteerQuickSearchResult> {

	public static class VolunteerQuickSearchResultView {
		public interface Basic {
		}

		public interface Extended extends Basic {
		}

		public interface TimeEntrySearch extends Basic {
		}
	}

	// ------------------------------------- Fields

	private long id;
	private String code;
	private String name;
	private LocalDate dob;

	private List<VolunteerQuickSearchAssignment> assignments;

	private List<VolunteerQuickSearchOrganization> organizations;
	
	// ------------------------------------- Constructors

	public VolunteerQuickSearchResult(long id, String code, String name, LocalDate dob) {
		this.id = id;
		this.code = code;
		this.name = name;
		this.dob = dob;
	}

	// ------------------------------------- Common Methods

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
		VolunteerQuickSearchResult other = (VolunteerQuickSearchResult) obj;
		if (id != other.id)
			return false;
		return true;
	}

	@Override
	public int compareTo(VolunteerQuickSearchResult o) {
		if (equals(o))
			return 0;

		return new CompareToBuilder().append(StringUtils.lowerCase(getName()), StringUtils.lowerCase(o.getName()))
				.append(id, o.id).toComparison() > 0 ? 1 : -1;
	}

	// ------------------------------------- Accessor Methods

	@JsonView(VolunteerQuickSearchResultView.Basic.class)
	public LocalDate getDob() {
		return dob;
	}

	public long getId() {
		return id;
	}

	@JsonView(VolunteerQuickSearchResultView.Extended.class)
	public String getCode() {
		return code;
	}

	@JsonView(VolunteerQuickSearchResultView.Basic.class)
	public String getName() {
		return name;
	}

	@JsonView(VolunteerQuickSearchResultView.TimeEntrySearch.class)
	public List<VolunteerQuickSearchAssignment> getAssignments() {
		if (assignments == null)
			assignments = new ArrayList<>();
		return assignments;
	}

	@JsonView(VolunteerQuickSearchResultView.TimeEntrySearch.class)
	public List<VolunteerQuickSearchOrganization> getOrganizations() {
		if (organizations == null)
			organizations = new ArrayList<>();
		return organizations;
	}

	
	
}