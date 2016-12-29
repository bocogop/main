package org.bocogop.shared.persistence.lookup.sds;

import java.util.List;
import java.util.SortedSet;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.CompareToBuilder;

import org.bocogop.shared.model.lookup.sds.VAFacility;
import org.bocogop.shared.persistence.AppSortedDAO;

public interface VAFacilityDAO extends AppSortedDAO<VAFacility> {

	VAFacility findByStationNumber(String stationNumber);

	SortedSet<VAFacility> findAllChildrenSorted(long parentId);

	SortedSet<VAFacility> findAllThreeDigitStationsSorted();

	SortedSet<VAFacility> findAllVISNsSorted();

	SortedSet<VAFacility> findAllStationsInVisn(long visnId);

	SortedSet<VAFacility> findAllActiveTreatingFacilities();

	List<QuickSearchResult> findByCriteria(String searchValue, int length);

	public static class QuickSearchResult implements Comparable<QuickSearchResult> {

		private long id;
		private String displayName;

		public QuickSearchResult(long userId, String displayName) {
			this.id = userId;
			this.displayName = displayName;
		}

		public long getId() {
			return id;
		}

		public String getDisplayName() {
			return displayName;
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
			QuickSearchResult other = (QuickSearchResult) obj;
			if (id != other.id)
				return false;
			return true;
		}

		@Override
		public int compareTo(QuickSearchResult o) {
			if (equals(o))
				return 0;

			return new CompareToBuilder()
					.append(StringUtils.lowerCase(getDisplayName()), StringUtils.lowerCase(o.getDisplayName()))
					.toComparison() > 0 ? 1 : -1;
		}
	}
}
