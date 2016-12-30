package org.bocogop.shared.service;

import java.util.Map;
import java.util.SortedSet;

import org.bocogop.shared.model.AppUser;
import org.bocogop.wr.model.precinct.Precinct;

public interface UserAdminCustomizations {

	AppUser userRetrievedCallback(AppUser u, Map<String, Object> userAdminCustomizationsModel);

	AppUser userCreatedCallback(AppUser u, Map<String, Object> userAdminCustomizationsModel);

	void userDeletedCallback(long appUserId, Map<String, Object> userAdminCustomizationsModel);

	SortedSet<Precinct> getAssignablePrecincts();

}