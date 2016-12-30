package org.bocogop.wr.persistence.lookup;

import java.util.SortedSet;

import org.bocogop.wr.model.lookup.State;
import org.bocogop.wr.persistence.AppSortedDAO;

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
