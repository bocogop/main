package org.bocogop.wr.web.volunteer;

import static org.bocogop.wr.persistence.queryCustomization.fieldTypes.VolunteerAssignmentAssociationFieldType.BENEFITING_SERVICE;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import org.bocogop.wr.model.benefitingService.BenefitingServiceRole;
import org.bocogop.wr.model.volunteer.Volunteer;
import org.bocogop.wr.model.volunteer.VolunteerAssignment;
import org.bocogop.wr.model.volunteer.VolunteerAssignment.VolunteerAssignmentView;
import org.bocogop.wr.persistence.queryCustomization.QueryCustomization;
import org.bocogop.wr.persistence.queryCustomization.fieldTypes.benefitingService.BenefitingServiceRoleFieldType;
import org.bocogop.wr.web.AbstractAppController;

@Controller
@SessionAttributes(value = { AbstractAppController.DEFAULT_COMMAND_NAME })
public class VolunteerAssignmentController extends AbstractAppController {

	@RequestMapping(value = "/volunteer/inactivateVolunteerAssignment", method = RequestMethod.POST)
	// @PreAuthorize("hasAuthority('" + Permission.VOLUNTEER_VIEW + "')")
	public @ResponseBody Map<String, Object> volunteerAssignmentInactivate(@RequestParam long volunteerAssignmentId) {
		boolean statusChanged = volunteerService.inactivateAssignment(volunteerAssignmentId);
		Map<String, Object> result = new HashMap<>();
		result.put("volunteerStatusChanged", statusChanged);
		return result;
	}

	@RequestMapping(value = "/volunteer/deleteVolunteerAssignment", method = RequestMethod.POST)
	// @PreAuthorize("hasAuthority('" + Permission.VOLUNTEER_VIEW + "')")
	public @ResponseBody Map<String, Object> volunteerAssignmentDelete(@RequestParam long volunteerAssignmentId,
			@ModelAttribute(DEFAULT_COMMAND_NAME) VolunteerCommand command) {
		boolean statusChanged = volunteerService.deleteAssignment(volunteerAssignmentId);
		/*
		 * Without this, Hibernate gets confused since the Volunteer in the
		 * command has a reference to a VolunteerAssignment that was already
		 * deleted - CPB
		 */
		command.setVolunteer(volunteerDAO.findRequiredByPrimaryKey(command.getVolunteer().getId()));

		Map<String, Object> result = new HashMap<>();
		result.put("volunteerStatusChanged", statusChanged);
		return result;
	}

	@RequestMapping(value = "/volunteer/addOrReactivateVolunteerAssignment", method = RequestMethod.POST)
	// @PreAuthorize("hasAuthority('" + Permission.VOLUNTEER_VIEW + "')")
	public @ResponseBody Map<String, Object> volunteerAssignmentAddOrReactivate(
			// either
			@RequestParam(required = false) Long volunteerAssignmentId,
			// or
			@RequestParam(required = false) Long volunteerId,
			@RequestParam(required = false) Long benefitingServiceRoleId) throws ServiceValidationException {
		long facilityId = getFacilityContextId();
		boolean statusChanged = volunteerService.addOrReactivateAssignment(volunteerAssignmentId, volunteerId,
				facilityId, benefitingServiceRoleId);
		Map<String, Object> result = new HashMap<>();
		result.put("volunteerStatusChanged", statusChanged);
		return result;
	}

	@RequestMapping("/findAvailableAssignments")
	// @PreAuthorize("hasAuthority('" + Permission.VOLUNTEER_VIEW + "')")
	@JsonView(VolunteerAssignmentView.Search.class)
	public @ResponseBody List<AvailableAssignment> findAvailableAssignmentsForFacility(@RequestParam long facilityId) {
		List<BenefitingServiceRole> list = benefitingServiceRoleDAO.findByCriteria(null, Arrays.asList(facilityId),
				true, true, new QueryCustomization(BenefitingServiceRoleFieldType.BENEFITING_SERVICE));
		List<AvailableAssignment> results = new ArrayList<>();
		for (int i = 0; i < list.size(); i++) {
			BenefitingServiceRole r = list.get(i);
			AvailableAssignment a = new AvailableAssignment(i, r.getBenefitingService(), r);
			results.add(a);
		}
		return results;
	}

	@RequestMapping("/volunteerAssignments")
	// @PreAuthorize("hasAuthority('" + Permission.VOLUNTEER_VIEW + "')")
	@JsonView(VolunteerAssignmentView.Search.class)
	public @ResponseBody Map<String, Object> findAssignmentsForVolunteer(@RequestParam long volunteerId) {
		Map<String, Object> results = new HashMap<>();
		Volunteer v = volunteerDAO.findRequiredByPrimaryKey(volunteerId);

		List<VolunteerAssignment> resultList = volunteerAssignmentDAO.findByCriteria(volunteerId, null, null, null,
				null, new QueryCustomization(BENEFITING_SERVICE));
		results.put("assignments", resultList);

		Map<Long, Integer> hoursByAssignment = workEntryDAO
				.countByVolunteerAssignmentIds(resultList.stream().map(p -> p.getId()).collect(Collectors.toList()));
		results.put("hoursByAssignment", hoursByAssignment);

		results.put("primaryFacility", v.getPrimaryFacility());
		return results;
	}

}
