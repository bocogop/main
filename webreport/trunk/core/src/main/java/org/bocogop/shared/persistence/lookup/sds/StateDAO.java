package org.bocogop.shared.persistence.lookup.sds;

import java.util.SortedSet;

import org.bocogop.shared.model.lookup.sds.State;
import org.bocogop.shared.persistence.AppSortedDAO;

public interface StateDAO extends AppSortedDAO<State> {

	/**
	 * Sorted List of states in a given country
	 * 
	 * @param countryId
	 * @return
	 */
	public SortedSet<State> findSortedStateByCountry(String countryId);

	/**
	 * Sorted List of states in a given country filtered by fipcode
	 * 
	 * @param countryId
	 * @param fipsCode
	 * @return
	 */
	public SortedSet<State> findSortedStateByCountry(String countryId, String fipsCode);

	/**
	 * 
	 * @param postalCode
	 * @return
	 */
	public State findStateByPostalCode(String postalCode);

	/**
	 * 
	 * @return
	 */
	public SortedSet<State> findListOfStatesInUSA();

}
