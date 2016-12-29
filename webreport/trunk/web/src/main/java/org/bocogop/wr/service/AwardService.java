package org.bocogop.wr.service;

import java.time.LocalDate;
import java.util.Map;

import org.bocogop.wr.model.award.Award;

public interface AwardService {

	/**
	 * @param awardCode
	 *            The Award to save or update
	 * @return The updated awardCode after it's been merged

	 */
	Award saveOrUpdate(Award award);
	
	/**
	 * @param volIdToAwardMap
	 *            The Volunteer Id and Award Id map
	 * @return The update volunteers to assign deserved awards

	 */
	
	public void saveMultipleVolunteers(Map<Long, Long> volIdToAwardMap, LocalDate awardDate);

	

}
