package org.bocogop.shared.persistence;

import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.List;
import java.util.SortedSet;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.CompareToBuilder;
import org.bocogop.shared.model.AppUser;

public interface AppUserDAO extends AppSortedDAO<AppUser> {

	int updateFieldsWithoutVersionCheck(long appUserID, boolean incrementVersion, Long lastVisitedPrecinctId,
			ZonedDateTime accountLockDate, Integer failedLoginCount, String hashedPassword);

	Collection<AppUser> findByCriteria(Collection<String> usernames, String userDn, boolean preinitialize,
			String lastName, boolean wildcardLastName, String firstName, boolean wildcardFirstName);

	AppUser findByUsername(String activeDirectoryName, boolean preinitialize);

	AppUser findRequiredByUsername(String username, boolean preinitialize);

	List<QuickSearchResult> findByNameOrUsername(String text, int maxResults);

	public static class QuickSearchResult implements Comparable<QuickSearchResult> {
		private long id;
		private String displayName;
		private String username;

		public QuickSearchResult(long userId, String displayName, String username) {
			this.id = userId;
			this.displayName = displayName;
			this.username = username;
		}

		public long getId() {
			return id;
		}

		public String getDisplayName() {
			return displayName;
		}

		public String getUsername() {
			return username;
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

	SortedSet<AppUser> listAllWithRoles();
}
