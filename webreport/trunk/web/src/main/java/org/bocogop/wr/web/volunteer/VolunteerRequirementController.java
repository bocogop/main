package org.bocogop.wr.web.volunteer;

import static org.bocogop.wr.persistence.queryCustomization.fieldTypes.VolunteerAssignmentAssociationFieldType.BENEFITING_SERVICE;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttributes;

import com.fasterxml.jackson.annotation.JsonView;

import org.bocogop.wr.model.requirement.RequirementStatus;
import org.bocogop.wr.model.requirement.VolunteerRequirement;
import org.bocogop.wr.model.requirement.VolunteerRequirement.VolunteerRequirementView;
import org.bocogop.wr.model.views.VolunteerRequirementActive;
import org.bocogop.wr.model.volunteer.VolunteerAssignment;
import org.bocogop.wr.persistence.queryCustomization.QueryCustomization;
import org.bocogop.wr.service.impl.RequirementServiceImpl;
import org.bocogop.wr.util.DateUtil;
import org.bocogop.wr.web.AbstractAppController;

@Controller
@SessionAttributes(value = { AbstractAppController.DEFAULT_COMMAND_NAME })
public class VolunteerRequirementController extends AbstractAppController {

	@RequestMapping(value = "/volunteer/volunteerRequirement/update", method = RequestMethod.POST)
	// @PreAuthorize("hasAuthority('" + Permission.VOLUNTEER_VIEW + "')")
	public @ResponseBody boolean volunteerRequirementUpdate(@RequestParam long volunteerRequirementId,
			@DateTimeFormat(pattern = DateUtil.TWO_DIGIT_DATE_ONLY) @RequestParam(required = false) LocalDate requirementDate,
			@RequestParam(required = false) RequirementStatus status, @RequestParam(required = false) String comments) {

		VolunteerRequirement vr = volunteerRequirementDAO.findRequiredByPrimaryKey(volunteerRequirementId);
		vr.setRequirementDate(requirementDate);
		vr.setStatus(status);
		vr.setComments(comments);

		vr = volunteerRequirementService.saveOrUpdate(vr);
		return true;
	}

	@RequestMapping("/volunteerRequirements")
	// @PreAuthorize("hasAuthority('" + Permission.VOLUNTEER_VIEW + "')")
	@JsonView(VolunteerRequirementView.Search.class)
	public @ResponseBody Map<String, Object> findRequirementsForVolunteer(
			@RequestParam(required = false) Long volunteerId,
			@RequestParam(required = false) Long volunteerRequirementId) throws Exception {
		if (volunteerId == null && volunteerRequirementId == null)
			throw new Exception("Must specify either volunteerId or volunteerRequirementId");
		if (volunteerId != null && volunteerRequirementId != null)
			throw new Exception("Must specify only volunteerId or volunteerRequirementId but not both");

		Long finalVolunteerId = volunteerId;
		if (volunteerRequirementId != null) {
			finalVolunteerId = volunteerRequirementDAO.findRequiredByPrimaryKey(volunteerRequirementId).getVolunteer()
					.getId();
		}

		Map<String, Object> results = new HashMap<>();

		List<VolunteerRequirementActive> requirements = volunteerRequirementDAO.findByCriteria(
				VolunteerRequirementActive.class, finalVolunteerId,
				/*
				 * Change this to null to enable site admins to view
				 * requirements for any facility in which they have
				 * edit-volunteer permissions (this logic is implemented in the
				 * javascript) - CPB
				 */
				getFacilityContextId());
		results.put("allRequirements", requirements);

		List<VolunteerAssignment> assignments = volunteerAssignmentDAO.findByCriteria(finalVolunteerId, true, null,
				null, null, new QueryCustomization(BENEFITING_SERVICE));
		results.put("allVolunteerAssignments", assignments);

		results.put("requirementsByScope", RequirementServiceImpl.getRequirementsByScope(requirements, assignments));

		return results;
	}

}
