package org.bocogop.wr.persistence.dao.voter;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.PUBLIC_ONLY;

import java.time.LocalDate;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.builder.CompareToBuilder;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonView;

@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = PUBLIC_ONLY, isGetterVisibility = PUBLIC_ONLY)
public class VoterQuickSearchResult implements Comparable<VoterQuickSearchResult> {

	public static class VoterQuickSearchResultView {
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

	// ------------------------------------- Constructors

	public VoterQuickSearchResult(long id, String code, String name, LocalDate dob) {
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
		VoterQuickSearchResult other = (VoterQuickSearchResult) obj;
		if (id != other.id)
			return false;
		return true;
	}

	@Override
	public int compareTo(VoterQuickSearchResult o) {
		if (equals(o))
			return 0;

		return new CompareToBuilder().append(StringUtils.lowerCase(getName()), StringUtils.lowerCase(o.getName()))
				.append(id, o.id).toComparison() > 0 ? 1 : -1;
	}

	// ------------------------------------- Accessor Methods

	@JsonView(VoterQuickSearchResultView.Basic.class)
	public LocalDate getDob() {
		return dob;
	}

	public long getId() {
		return id;
	}

	@JsonView(VoterQuickSearchResultView.Extended.class)
	public String getCode() {
		return code;
	}

	@JsonView(VoterQuickSearchResultView.Basic.class)
	public String getName() {
		return name;
	}

}