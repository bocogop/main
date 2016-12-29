package org.bocogop.wr.web.volunteer;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttributes;

import com.fasterxml.jackson.annotation.JsonView;

import org.bocogop.shared.service.validation.ServiceValidationException;
import org.bocogop.wr.model.volunteer.Volunteer;
import org.bocogop.wr.model.volunteer.VolunteerOrganization;
import org.bocogop.wr.model.volunteer.VolunteerOrganization.CompareByOrganization;
import org.bocogop.wr.model.volunteer.VolunteerOrganization.VolunteerOrganizationView;
import org.bocogop.wr.web.AbstractAppController;

@Controller
@SessionAttributes(value = { AbstractAppController.DEFAULT_COMMAND_NAME })
public class VolunteerOrganizationController extends AbstractAppController {

	@RequestMapping("/volunteerOrganizations")
	// @PreAuthorize("hasAuthority('" + Permission.VOLUNTEER_VIEW + "')")
	@JsonView(VolunteerOrganizationView.SearchForOrganizations.class)
	public @ResponseBody Map<String, Object> findOrgsForVolunteer(@RequestParam long volunteerId) {
		Map<String, Object> results = new HashMap<>();
		Volunteer v = volunteerDAO.findRequiredByPrimaryKey(volunteerId);

		SortedSet<VolunteerOrganization> r = new TreeSet<>(new CompareByOrganization());
		r.addAll(v.getVolunteerOrganizations());
		results.put("organizations", r);

		Map<Long, Double> hoursByOrganization = workEntryDAO.countByVolunteerAndBasicOrganizations(volunteerId,
				r.stream().map(p -> p.getOrganization().getId()).collect(Collectors.toList()));
		results.put("hoursByOrganization", hoursByOrganization);

		results.put("primaryOrganization", v.getPrimaryOrganization());
		return results;
	}

	@RequestMapping(value = "/volunteer/setPrimaryOrganization", method = RequestMethod.POST)
	// @PreAuthorize("hasAuthority('" + Permission.VOLUNTEER_VIEW + "')")
	public @ResponseBody boolean primaryOrganizationSet(@RequestParam long volunteerId,
			@RequestParam long organizationId) {
		volunteerService.setPrimaryOrganization(volunteerId, organizationId);
		return true;
	}

	@RequestMapping(value = "/volunteer/inactivateOrganization", method = RequestMethod.POST)
	// @PreAuthorize("hasAuthority('" + Permission.VOLUNTEER_VIEW + "')")
	public @ResponseBody Map<String, Object> organizationInactivate(@RequestParam long volunteerOrganizationId) {
		Map<String, Object> result = volunteerService.inactivateOrganization(volunteerOrganizationId);
		return result;
	}

	@RequestMapping(value = "/volunteer/deleteOrganization", method = RequestMethod.POST)
	// @PreAuthorize("hasAuthority('" + Permission.VOLUNTEER_VIEW + "')")
	public @ResponseBody Map<String, Object> organizationDelete(@RequestParam long volunteerOrganizationId,
			@ModelAttribute(DEFAULT_COMMAND_NAME) VolunteerCommand command) {
		Map<String, Object> result = volunteerService.deleteOrganization(volunteerOrganizationId);
		/*
		 * Without this, Hibernate gets confused since the Volunteer in the
		 * command has a reference to a VolunteerOrganization that was already
		 * deleted - CPB
		 */
		command.setVolunteer(volunteerDAO.findRequiredByPrimaryKey(command.getVolunteer().getId()));
		return result;
	}

	@RequestMapping(value = "/volunteer/addOrganization", method = RequestMethod.POST)
	// @PreAuthorize("hasAuthority('" + Permission.VOLUNTEER_VIEW + "')")
	public @ResponseBody Map<String, Object> organizationAdd(@RequestParam long volunteerId,
			@RequestParam long organizationId) throws ServiceValidationException {
		boolean statusChanged = volunteerService.addOrReactivateOrganization(volunteerId, organizationId);

		Map<String, Object> result = new HashMap<>();
		result.put("volunteerStatusChanged", statusChanged);
		return result;
	}

}
