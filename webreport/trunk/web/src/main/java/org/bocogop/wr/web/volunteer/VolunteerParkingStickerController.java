package org.bocogop.wr.web.volunteer;

import java.util.SortedSet;
import java.util.TreeSet;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.annotation.JsonView;

import org.bocogop.shared.model.lookup.sds.State;
import org.bocogop.wr.model.facility.Facility;
import org.bocogop.wr.model.volunteer.ParkingSticker;
import org.bocogop.wr.model.volunteer.ParkingSticker.ParkingStickerView;
import org.bocogop.wr.model.volunteer.Volunteer;
import org.bocogop.wr.web.AbstractAppController;

@Controller
public class VolunteerParkingStickerController extends AbstractAppController {

	@RequestMapping(value = "/volunteer/deleteParkingSticker", method = RequestMethod.POST)
	public @ResponseBody boolean parkingStickerDelete(@RequestParam long parkingStickerId) {
		parkingStickerService.delete(parkingStickerId);
		return true;
	}

	@RequestMapping(value = "/volunteer/parkingSticker/createOrUpdate", method = RequestMethod.POST)
	public @ResponseBody boolean parkingStickerCreateOrUpdate(@RequestParam long facilityId,
			@RequestParam(required = false) Long parkingStickerId, @RequestParam long volunteerId,
			@RequestParam String number, @RequestParam(required = false) State state,
			@RequestParam(required = false) String licensePlate) {
		ParkingSticker ps;
		if (parkingStickerId != null) {
			ps = parkingStickerDAO.findRequiredByPrimaryKey(parkingStickerId);
		} else {
			ps = new ParkingSticker();
			Volunteer v = volunteerDAO.findRequiredByPrimaryKey(volunteerId);
			ps.setVolunteer(v);
		}

		ps.setStickerNumber(number);
		ps.setLicensePlate(licensePlate);
		ps.setState(state);

		Facility i = facilityDAO.findRequiredByPrimaryKey(facilityId);
		ps.setFacility(i);

		ps = parkingStickerService.saveOrUpdate(ps);
		return true;
	}

	@RequestMapping("/volunteerParkingStickers")
	@JsonView(ParkingStickerView.Extended.class)
	public @ResponseBody SortedSet<ParkingSticker> findParkingStickersForVolunteer(@RequestParam long volunteerId) {
		Volunteer v = volunteerDAO.findRequiredByPrimaryKey(volunteerId);
		SortedSet<ParkingSticker> r = new TreeSet<>();
		r.addAll(v.getParkingStickers());
		return r;
	}

}
