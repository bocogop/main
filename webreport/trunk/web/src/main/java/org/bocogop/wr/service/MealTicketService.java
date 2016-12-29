package org.bocogop.wr.service;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

import org.bocogop.shared.service.validation.ServiceValidationException;
import org.bocogop.wr.model.facility.StationParameters;
import org.bocogop.wr.model.mealTicket.MealTicket;

public interface MealTicketService {

	/**
	 * 
	 * @param o
	 *            The MealTicket to save or update
	 * @return The updated MealTicket after it's been persisted / updated
	 * @throws ServiceValidationException
	 *             If a business-level validation exception occurred during the
	 *             save/update process
	 */
	MealTicket saveOrUpdate(MealTicket o) throws ServiceValidationException;

	/**
	 * Adds meal ticket for occasional volunteer with a name
	 * 
	 * @param lastName
	 * @param firstName
	 * @return
	 * @throws ServiceValidationException
	 */
	MealTicket addOccasionalVolunteer(String lastName, String firstName) throws ServiceValidationException;

	MealTicket addVolunteer(long volunteerId) throws ServiceValidationException;

	/**
	 * Deletes the MealTicket with the specified mealTicketId
	 * 
	 * @param mealTicketId
	 *            The ID of the meal ticket to delete
	 */
	void deleteMealTicket(long mealTicketId);

	/**
	 * Print or reprint the MealTicket with the specified mealTicketIds
	 * @param facilityId TODO
	 * @param mealTicketIds
	 *            The IDs of the meal ticket to print or reprint
	 * 
	 * @return 
	 */
	Map<Long, String> printMealTicketsByStaff(long facilityId, List<Long> mealTicketIds);

	int processMealTicketsForTodayByKiosk(ZonedDateTime requestTime, long kioskId, long volunteerId,
			boolean printTickets);

	String getMealTicketText(ZonedDateTime printTime, StationParameters sp, MealTicket mt);
}
