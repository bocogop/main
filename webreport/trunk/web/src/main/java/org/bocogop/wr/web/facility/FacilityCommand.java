package org.bocogop.wr.web.facility;

import org.bocogop.wr.model.facility.Facility;
import org.bocogop.wr.model.facility.StationParameters;

public class FacilityCommand {

	private Facility facility;
	private Long parentId;

	public FacilityCommand() {
	}

	public FacilityCommand(Facility facility) {
		this.facility = facility;
		this.parentId = facility.getParent() != null ? facility.getParent().getId() : null;
		StationParameters stationParam = facility.getStationParameters();
		if (stationParam == null) {
			stationParam = new StationParameters();
			facility.setStationParameters(stationParam);
		}
		if (stationParam.getNumberOfMeals() == null)
			stationParam.setNumberOfMeals(1);
		if (stationParam.getMealAuthorization() == null)
			stationParam.setMealAuthorization("T");
	}

	public void refreshFacility(Facility f) {
		setFacility(f);
		/* Avoid lazy loading on binding - CPB */
		f.getStationParameters().getNumberOfMeals();
	}
	
	public Facility getFacility() {
		return facility;
	}

	public void setFacility(Facility facility) {
		this.facility = facility;
	}

	public Long getParentId() {
		return parentId;
	}

	public void setParentId(Long parentId) {
		this.parentId = parentId;
	}

}
