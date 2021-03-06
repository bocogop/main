package org.bocogop.shared.persistence.dao.voter;

import java.util.Collection;
import java.util.List;
import java.util.SortedSet;

import org.bocogop.shared.model.precinct.Precinct;
import org.bocogop.shared.model.voter.Voter;
import org.bocogop.shared.persistence.dao.CustomizableSortedDAO;
import org.bocogop.shared.persistence.queryCustomization.QueryCustomization;

public interface VoterDAO extends CustomizableSortedDAO<Voter> {

	/**
	 * 
	 * @param middleName
	 *            TODO
	 * @param firstNameCanMatchNickname
	 *            TODO
	 * @param useExactNameMatching
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
	 * @param precinctIds
	 *            TODO
	 * @param identifyingCode
	 *            TODO
	 * @param dateOfBirth
	 *            TODO
	 * @param status
	 *            TODO
	 * @param name
	 *            All or part of the first name (case-insensitive)
	 * @param wildcardName
	 *            All or part of the last name (case-insensitive)
	 * @return The list of Voters matching the above criteria
	 */
	List<Voter> findByCriteria(String voterId, String firstName, String middleName, String lastName,
			boolean firstNameCanMatchNickname, boolean useExactNameMatching, Integer birthYear, String addressStreet,
			String city, String state, String zip, String phone, String email, Collection<Long> precinctIds,
			QueryCustomization... customization);

	List<String> findZipCodesAtPrecincts(List<Long> precinctIds, Boolean voterActive, Boolean voterAssignmentActive);

	SortedSet<Precinct> findPrecinctsForVoter(long voterId);

	SortedSet<VoterQuickSearchResult> quickSearch(String searchValue, Long voterId, long precinctId,
			boolean includeAssignments, boolean includeOrganizations, boolean onlyActiveAssignmentsAndOrgs);

}
