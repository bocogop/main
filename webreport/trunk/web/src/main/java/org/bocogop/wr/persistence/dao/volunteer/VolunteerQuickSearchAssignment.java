package org.bocogop.wr.persistence.dao.volunteer;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.PUBLIC_ONLY;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.builder.CompareToBuilder;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = PUBLIC_ONLY, isGetterVisibility = PUBLIC_ONLY)
public class VolunteerQuickSearchAssignment implements Comparable<VolunteerQuickSearchAssignment> {

	public static class VolunteerQuickSearchAssignmentView {
		public interface Basic {
		}
	}

	// ------------------------------------- Fields

	private long id;
	private String name;
	private boolean active;

	// ------------------------------------- Constructors

	public VolunteerQuickSearchAssignment(long id, String name, boolean active) {
		this.id = id;
		this.name = name;
		this.active = active;
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
		VolunteerQuickSearchAssignment other = (VolunteerQuickSearchAssignment) obj;
		if (id != other.id)
			return false;
		return true;
	}

	@Override
	public int compareTo(VolunteerQuickSearchAssignment o) {
		if (equals(o))
			return 0;

		return new CompareToBuilder().append(StringUtils.lowerCase(getName()), StringUtils.lowerCase(o.getName()))
				.append(id, o.id).toComparison() > 0 ? 1 : -1;
	}

	// ------------------------------------- Accessor Methods

	public long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public boolean isActive() {
		return active;
	}

}