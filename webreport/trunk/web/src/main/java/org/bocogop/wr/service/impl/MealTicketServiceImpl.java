package org.bocogop.wr.service.impl;

import static org.bocogop.wr.model.lookup.CommonTemplateType.MEAL_TICKET_TEXT;

import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.bocogop.shared.service.validation.ServiceValidationException;
import org.bocogop.wr.model.Holiday;
import org.bocogop.wr.model.facility.Facility;
import org.bocogop.wr.model.facility.Kiosk;
import org.bocogop.wr.model.facility.StationParameters;
import org.bocogop.wr.model.mealTicket.MealTicket;
import org.bocogop.wr.model.mealTicket.MealTicketRequestType;
import org.bocogop.wr.model.printing.PrintRequest;
import org.bocogop.wr.model.time.WorkEntry;
import org.bocogop.wr.model.volunteer.Volunteer;
import org.bocogop.wr.persistence.dao.MealTicketDAO;
import org.bocogop.wr.service.MealTicketService;
import org.bocogop.wr.service.VelocityService;
import org.bocogop.wr.util.DateUtil;

@Service
public class MealTicketServiceImpl extends AbstractServiceImpl implements MealTicketService {
	private static final Logger log = LoggerFactory.getLogger(MealTicketServiceImpl.class);

	@Autowired
	private MealTicketDAO mealTicketDAO;
	@Autowired
	private VelocityService velocityService;

	@Override
	public MealTicket saveOrUpdate(MealTicket mealTicket) throws ServiceValidationException {
		/* Business-level validations */
		mealTicket = mealTicketDAO.saveOrUpdate(mealTicket);
		return mealTicket;
	}

	@Override
	public MealTicket addOccasionalVolunteer(String lastName, String firstName) throws ServiceValidationException {
		MealTicket mt = new MealTicket(getTodayAtFacility(), getRequiredFacilityContext(), lastName, firstName,
				ZonedDateTime.now());
		mt = mealTicketDAO.saveOrUpdate(mt);
		return mealTicketDAO.saveOrUpdate(mt);
	}

	@Override
	public MealTicket addVolunteer(long volunteerId) throws ServiceValidationException {
		Volunteer volunteer = volunteerDAO.findRequiredByPrimaryKey(volunteerId);
		MealTicket mt = new MealTicket(getTodayAtFacility(), getRequiredFacilityContext(), volunteer,
				MealTicketRequestType.MANUAL, ZonedDateTime.now());
		return mealTicketDAO.saveOrUpdate(mt);
	}

	public void deleteMealTicket(long mealTicketId) {
		mealTicketDAO.delete(mealTicketId);
	}

	public Map<Long, String> printMealTicketsByStaff(long facilityId, List<Long> mealTicketIds) {
		Map<Long, String> results = new LinkedHashMap<>();

		Facility facility = facilityDAO.findRequiredByPrimaryKey(facilityId);
		StationParameters sp = facility.getStationParameters();

		ZonedDateTime now = ZonedDateTime.now(DateUtil.UTC);
		for (Long mealTicketId : mealTicketIds) {
			MealTicket mealTicket = mealTicketDAO.findRequiredByPrimaryKey(mealTicketId);
			if (mealTicket.getLastPrintedDate() != null) {
				mealTicket.setReprinted(true);
			} else {
				mealTicket.setLastPrintedDate(now);
			}
			mealTicket = mealTicketDAO.saveOrUpdate(mealTicket);

			String text = getMealTicketText(now, sp, mealTicket);
			results.put(mealTicketId, text);
		}

		return results;
	}

	@Override
	public int processMealTicketsForTodayByKiosk(ZonedDateTime requestTime, long kioskId, long volunteerId,
			boolean printTickets) {
		Volunteer volunteer = volunteerDAO.findRequiredByPrimaryKey(volunteerId);
		Integer mealsEligible = volunteer.getMealsEligible();
		if (mealsEligible != null && mealsEligible == 0)
			return 0;

		Kiosk kiosk = kioskDAO.findRequiredByPrimaryKey(kioskId);
		Facility facility = kiosk.getFacility();
		ZoneId zone = facility.getTimeZone() == null ? ZoneId.systemDefault() : facility.getTimeZone();
		StationParameters sp = facility.getStationParameters();
		requestTime = requestTime.withZoneSameInstant(zone);

		LocalDate today = requestTime.toLocalDate();
		if (today.getDayOfWeek() == DayOfWeek.SUNDAY && sp.getSundayMeal() != null && !sp.getSundayMeal())
			return 0;
		if (today.getDayOfWeek() == DayOfWeek.SATURDAY && sp.getSaturdayMeal() != null && !sp.getSaturdayMeal())
			return 0;

		if (sp.getHolidayMeal() != null && !sp.getHolidayMeal()) {
			List<Holiday> h = holidayDAO.findByCriteria(today);
			if (!h.isEmpty())
				return 0;
		}

		int curHourAndMin = Integer
				.parseInt(requestTime.toLocalTime().format(DateUtil.TWO_DIGIT_HOUR_AND_MINUTE_ONLY_FORMAT));

		List<WorkEntry> entriesForToday = workEntryDAO.findByCriteria(volunteerId, null, facility.getId(), null, today,
				null);
		double totalHours = entriesForToday.stream().mapToDouble(p -> p.getHoursWorked()).sum();

		int numMeals = 0;

		Integer facilityNumMealsAllowed = sp.getNumberOfMeals();
		if (facilityNumMealsAllowed == null)
			facilityNumMealsAllowed = 0;

		if (facilityNumMealsAllowed >= 1 && totalHours >= sp.getMeal1Duration().doubleValue()
				&& (sp.getMeal1CutoffTime() == null || Integer.parseInt(sp.getMeal1CutoffTime()) > curHourAndMin))
			numMeals++;
		if (facilityNumMealsAllowed >= 2 && sp.getMeal2Duration() != null
				&& totalHours >= sp.getMeal2Duration().doubleValue()
				&& (sp.getMeal2CutoffTime() == null || Integer.parseInt(sp.getMeal2CutoffTime()) > curHourAndMin))
			numMeals++;
		if (facilityNumMealsAllowed >= 3 && sp.getMeal3Duration() != null
				&& totalHours >= sp.getMeal3Duration().doubleValue()
				&& (sp.getMeal3CutoffTime() == null || Integer.parseInt(sp.getMeal3CutoffTime()) > curHourAndMin))
			numMeals++;

		List<MealTicket> existingMeals = mealTicketDAO.findByCriteria(facility.getId(), volunteerId,
				requestTime.toLocalDate());

		if (mealsEligible != null && numMeals > mealsEligible)
			numMeals = mealsEligible;

		int mealsDue = numMeals - existingMeals.size();

		if (printTickets) {
			ZonedDateTime printTime = ZonedDateTime.now();

			for (int i = 0; i < mealsDue; i++) {
				PrintRequest pr = new PrintRequest();
				pr.setKiosk(kiosk);
				pr.setRequestTime(printTime);

				MealTicket mt = new MealTicket(requestTime.toLocalDate(), facility, volunteer,
						MealTicketRequestType.AUTOMATIC, requestTime.plusSeconds(i));
				mt.setLastPrintedDate(printTime);
				mt = mealTicketDAO.saveOrUpdate(mt);

				String templateResult = getMealTicketText(printTime, sp, mt);
				pr.setPrintText(templateResult);

				pr = printRequestDAO.saveOrUpdate(pr);
			}
		}

		return Math.max(mealsDue, 0);
	}

	public String getMealTicketText(ZonedDateTime printTime, StationParameters sp, MealTicket mt) {
		Map<String, Object> ticketModel = new HashMap<>();
		ticketModel.put("ticket", mt);
		ticketModel.put("printTime", printTime);
		ticketModel.put("ticketPrice", sp.getMealPrice().setScale(2, RoundingMode.HALF_EVEN));

		String templateResult = velocityService.mergeTemplateIntoString(MEAL_TICKET_TEXT.getName(), ticketModel);
		return templateResult;
	}

}
