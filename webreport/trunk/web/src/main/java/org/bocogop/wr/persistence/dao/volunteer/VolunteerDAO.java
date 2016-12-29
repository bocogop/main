package org.bocogop.wr.persistence.dao.volunteer;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collection;
import java.util.List;
import java.util.SortedSet;

import org.bocogop.wr.model.facility.Facility;
import org.bocogop.wr.model.volunteer.Volunteer;
import org.bocogop.wr.model.volunteer.VolunteerStatus.VolunteerStatusType;
import org.bocogop.wr.persistence.dao.CustomizableSortedDAO;
import org.bocogop.wr.persistence.dao.TimeSummary;
import org.bocogop.wr.persistence.queryCustomization.QueryCustomization;

public interface VolunteerDAO extends CustomizableSortedDAO<Volunteer> {

	/**
	 * 
	 * @param middleName
	 *            TODO
	 * @param firstNameOrLastNameMatches
	 *            TODO
	 * @param useExactNameMatching
	 *            TODO
	 * @param identifyingCode
	 *            TODO
	 * @param dateOfBirth
	 *            TODO
	 * @param addressStreet
	 *            TODO
	 * @param city
	 *            TODO
	 * @param state
	 *            TODO
	 * @param zip
	 *            TODO
	 * @param phone
	 *            TODO
	 * @param email
	 *            TODO
	 * @param status
	 *            TODO
	 * @param facilityIds
	 *            TODO
	 * @param name
	 *            All or part of the first name (case-insensitive)
	 * @param wildcardName
	 *            All or part of the last name (case-insensitive)
	 * @return The list of Volunteers matching the above criteria
	 */
	List<Volunteer> findByCriteria(String firstName, String middleName, String lastName,
			boolean firstNameOrLastNameMatches, boolean useExactNameMatching, String identifyingCode,
			LocalDate dateOfBirth, String addressStreet, String city, String state, String zip, String phone,
			String email, VolunteerStatusType status, Collection<Long> facilityIds,
			QueryCustomization... customization);

	List<String> findZipCodesAtFacilities(List<Long> facilityIds, Boolean volunteerActive,
			Boolean volunteerAssignmentActive);

	SortedSet<Facility> findFacilitiesForVolunteer(long volunteerId);

	void updateFieldsWithoutVersionIncrement(long volunteerId, boolean setPrimaryOrganization,
			Long primaryOrganizationId, boolean setPrimaryFacility, Long primaryFacilityId, VolunteerStatusType status,
			LocalDate statusDate, Long preferredLanguageId);

	TimeSummary getTimeSummary(long volunteerId, ZoneId facilityTimeZone);

	SortedSet<VolunteerQuickSearchResult> quickSearch(String searchValue, Long volunteerId, long facilityId,
			boolean includeAssignments, boolean includeOrganizations, boolean onlyActiveAssignmentsAndOrgs);

	int inactivateStaleVolunteers(LocalDate entryDateBefore, ZoneId zoneForStatusDate);

	List<Volunteer> findByAwardCode(long awardCodeId);

}
