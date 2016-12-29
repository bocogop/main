package org.bocogop.wr.web.mealTicket;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttributes;

import org.bocogop.shared.model.AppUser;
import org.bocogop.shared.model.Permission;
import org.bocogop.shared.model.Permission.PermissionType;
import org.bocogop.shared.service.validation.ServiceValidationException;
import org.bocogop.shared.util.SecurityUtil;
import org.bocogop.wr.model.mealTicket.MealTicket;
import org.bocogop.wr.web.AbstractAppController;
import org.bocogop.wr.web.breadcrumbs.Breadcrumb;

@Controller
@SessionAttributes(value = { AbstractAppController.DEFAULT_COMMAND_NAME })
public class MealTicketController extends AbstractAppController {

	@RequestMapping(path = "/mealTicketList.htm", method = RequestMethod.GET)
	@Breadcrumb("Daily Meal Tickets")
	@PreAuthorize("hasAnyAuthority('" + Permission.MEALTICKET_READ + ", " + Permission.MEALTICKET_CREATE + "')")
	public String listMealTickets(@ModelAttribute("mealticketListCommand") MealTicketListCommand command,
			ModelMap model) {
		long facilityId = getFacilityContextId();

		LocalDate mealDate = null;
		ZoneId timeZone = SecurityUtil.getCurrentUserAs(AppUser.class).getTimeZone();
		mealDate = LocalDate.now(timeZone);

		List<MealTicket> results = mealTicketDAO.findByCriteria(facilityId, null, mealDate);

		command = new MealTicketListCommand(results);

		model.addAttribute(DEFAULT_COMMAND_NAME, command);
		model.addAttribute("mealDate", mealDate);
		setFormAsReadOnlyUnlessUserHasPermissions(model, PermissionType.MEALTICKET_CREATE);
		return "mealTicketList";
	}

	@RequestMapping("/mealTicketOccasionalSubmit")
	@PreAuthorize("hasAuthority('" + Permission.MEALTICKET_CREATE + "')")
	public @ResponseBody MealTicket addOccasionalVolunteer(@RequestParam String lastName,
			@RequestParam(required = false) String firstName) throws ServiceValidationException {
		return mealTicketService.addOccasionalVolunteer(lastName, firstName);
	}

	@RequestMapping("/mealTicketAddVolunteer")
	@PreAuthorize("hasAuthority('" + Permission.MEALTICKET_CREATE + "')")
	public @ResponseBody MealTicket addVolunteer(@RequestParam long volunteerId) throws ServiceValidationException {

		return mealTicketService.addVolunteer(volunteerId);
	}

	@RequestMapping("/mealTicketDelete.htm")
	@PreAuthorize("hasAuthority('" + Permission.MEALTICKET_CREATE + "')")
	public String deleteMealTicket(@RequestParam long mealTicketId) throws ServiceValidationException {
		mealTicketService.deleteMealTicket(mealTicketId);
		return "redirect:/mealTicketList.htm";
	}

	@RequestMapping("/mealTicketPrint.htm")
	@PreAuthorize("hasAuthority('" + Permission.MEALTICKET_READ + "')")
	public String printMealTickets(@RequestParam(name = "mealTicketIds") List<Long> mealTicketIds, ModelMap model)
			throws ServiceValidationException {
		Map<Long, String> results = mealTicketService.printMealTicketsByStaff(getFacilityContextId(), mealTicketIds);
		model.put("mealTickets", results);
		return "mealTicketPrint";
	}

}
