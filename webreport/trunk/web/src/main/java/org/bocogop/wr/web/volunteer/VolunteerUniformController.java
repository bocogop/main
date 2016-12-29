package org.bocogop.wr.web.volunteer;

import java.util.SortedSet;
import java.util.TreeSet;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttributes;

import com.fasterxml.jackson.annotation.JsonView;

import org.bocogop.wr.model.facility.Facility;
import org.bocogop.wr.model.volunteer.ShirtSize;
import org.bocogop.wr.model.volunteer.Uniform;
import org.bocogop.wr.model.volunteer.Uniform.UniformView;
import org.bocogop.wr.model.volunteer.Volunteer;
import org.bocogop.wr.web.AbstractAppController;

@Controller
@SessionAttributes(value = { AbstractAppController.DEFAULT_COMMAND_NAME })
public class VolunteerUniformController extends AbstractAppController {

	@RequestMapping(value = "/volunteer/deleteUniform", method = RequestMethod.POST)
	// @PreAuthorize("hasAuthority('" + Permission.VOLUNTEER_VIEW + "')")
	public @ResponseBody boolean uniformDelete(@RequestParam long uniformId) {
		uniformService.delete(uniformId);
		return true;
	}

	@RequestMapping("/volunteerUniforms")
	// @PreAuthorize("hasAuthority('" + Permission.VOLUNTEER_VIEW + "')")
	@JsonView(UniformView.Extended.class)
	public @ResponseBody SortedSet<Uniform> findUniformsForVolunteer(@RequestParam long volunteerId) {
		Volunteer v = volunteerDAO.findRequiredByPrimaryKey(volunteerId);
		SortedSet<Uniform> r = new TreeSet<>();
		r.addAll(v.getUniforms());
		return r;
	}

	@RequestMapping(value = "/volunteer/uniform/createOrUpdate", method = RequestMethod.POST)
	// @PreAuthorize("hasAuthority('" + Permission.VOLUNTEER_VIEW + "')")
	public @ResponseBody boolean uniformCreateOrUpdate(@RequestParam long facilityId,
			@RequestParam(required = false) Long uniformId, @RequestParam long volunteerId,
			@RequestParam(required = false) ShirtSize size, @RequestParam(required = false) Integer count) {
		Uniform u;
		if (uniformId != null) {
			u = uniformDAO.findRequiredByPrimaryKey(uniformId);
		} else {
			u = new Uniform();
			Volunteer v = volunteerDAO.findRequiredByPrimaryKey(volunteerId);
			u.setVolunteer(v);
		}

		u.setShirtSize(size);
		u.setNumberOfShirts(count);

		Facility i = facilityDAO.findRequiredByPrimaryKey(facilityId);
		u.setFacility(i);

		u = uniformService.saveOrUpdate(u);
		return true;
	}

}
