package org.bocogop.shared.model;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.PUBLIC_ONLY;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

public interface BasicUserFields {

	String getUsername();

	String getFullDisplayName();

	String getPhone();

	String getEmailAddress();

	String getLastName();

	String getMiddleName();

	String getFirstName();

	@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = PUBLIC_ONLY, isGetterVisibility = PUBLIC_ONLY)
	public static class AppUserAdapter implements BasicUserFields {
		private AppUser appUser;

		public AppUserAdapter(AppUser appUser) {
			this.appUser = appUser;
		}

		@Override
		public String getFullDisplayName() {
			return appUser.getDisplayName();
		}

		@Override
		public String getEmailAddress() {
			return appUser.getEmail();
		}

		@Override
		public String getLastName() {
			return appUser.getLastName();
		}

		@Override
		public String getMiddleName() {
			return appUser.getMiddleName();
		}

		@Override
		public String getFirstName() {
			return appUser.getFirstName();
		}

		@Override
		public String getUsername() {
			return appUser.getUsername();
		}

		@Override
		public String getPhone() {
			return appUser.getPhone();
		}

	}

}
