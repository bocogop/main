package org.bocogop.wr.service.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.bocogop.wr.AbstractTransactionalWebTest;
import org.bocogop.wr.model.benefitingService.BenefitingService;
import org.bocogop.wr.model.benefitingService.BenefitingServiceRole;
import org.bocogop.wr.model.facility.Facility;
import org.bocogop.wr.model.facility.Kiosk;
import org.bocogop.wr.model.facility.StationParameters;
import org.bocogop.wr.model.printing.PrintRequest;
import org.bocogop.wr.model.time.WorkEntry;
import org.bocogop.wr.model.volunteer.Volunteer;
import org.bocogop.wr.model.volunteer.VolunteerAssignment;
import org.bocogop.wr.service.MealTicketService;
import org.bocogop.wr.util.DateUtil;

public class TestMealTicketServiceImpl extends AbstractTransactionalWebTest {

	@Autowired
	private MealTicketService mealTicketService;

	@Test
	public void test() {
		BenefitingService bs = benefitingServiceDAO.findSome(1).get(0);
		BenefitingServiceRole bsr = benefitingServiceRoleDAO.findSome(1).get(0);
		Volunteer v = volunteerDAO.findSome(1).get(0);
		v.setMealsEligible(1);
		v = volunteerDAO.saveOrUpdate(v);
		
		Facility f = bs.getFacility();
		Kiosk kiosk = f.getKiosks().get(0);

		VolunteerAssignment assn = new VolunteerAssignment();
		assn.setBenefitingService(bs);
		assn.setBenefitingServiceRole(bsr);
		assn.setFacility(f);
		assn.setRootFacility(f);
		assn.setVolunteer(v);
		v.getVolunteerAssignments().add(assn);

		assn = volunteerAssignmentDAO.saveOrUpdate(assn);

		StationParameters sp = f.getStationParameters();
		ZoneId zone = f.getTimeZone() == null ? ZoneId.systemDefault() : f.getTimeZone();
		int cutoff = Integer.parseInt(ZonedDateTime.now(zone).format(DateUtil.TWO_DIGIT_HOUR_AND_MINUTE_ONLY_FORMAT));
		sp.setMeal1CutoffTime(StringUtils.leftPad(String.valueOf(cutoff + 5), 4, "0"));
		sp.setMeal1Duration(BigDecimal.ONE);

		WorkEntry we = new WorkEntry();
		we.setDateWorked(LocalDate.now());
		we.setHoursWorked(sp.getMeal1Duration().add(BigDecimal.ONE).doubleValue());
		we.setVolunteerAssignment(assn);
		we.setOrganization(organizationDAO.findSome(1).get(0));
		we = workEntryDAO.saveOrUpdate(we);

		List<PrintRequest> beforeColl = printRequestDAO.findByCriteria(kiosk.getId(), null, null, null);
		int ticketsCreated = mealTicketService.processMealTicketsForTodayByKiosk(ZonedDateTime.now(), kiosk.getId(),
				v.getId(), true);
		Assert.assertTrue(ticketsCreated > 0);
		List<PrintRequest> afterColl = printRequestDAO.findByCriteria(kiosk.getId(), null, null, null);
		afterColl.removeAll(beforeColl);
		Assert.assertFalse(afterColl.isEmpty());
		System.out.println(afterColl.get(0).getPrintText());
	}

}
