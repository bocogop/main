package org.bocogop.wr.util.context;

import org.bocogop.wr.model.facility.Facility;

public class BasicContextManager implements ContextManager {

	Facility facilityContext;
	String facilityContextName;
	Integer facilityContextNumMeals;
	Boolean facilityContextCentralOffice;

	public BasicContextManager(Facility initialFacilityContext, String initialFacilityContextName,
			Integer initialFacilityContextNumMeals, Boolean initialFacilityContextCentralOffice) {
		this.facilityContext = initialFacilityContext;
		this.facilityContextName = initialFacilityContextName;
		this.facilityContextNumMeals = initialFacilityContextNumMeals;
		this.facilityContextCentralOffice = initialFacilityContextCentralOffice;
	}

	public Facility getFacilityContext() {
		return facilityContext;
	}

	public void setFacilityContext(Facility facilityContext) {
		this.facilityContext = facilityContext;
	}

	public String getFacilityContextName() {
		return facilityContextName;
	}

	public void setFacilityContextName(String facilityContextName) {
		this.facilityContextName = facilityContextName;
	}

	public Integer getFacilityContextNumMeals() {
		return facilityContextNumMeals;
	}

	public void setFacilityContextNumMeals(Integer facilityContextNumMeals) {
		this.facilityContextNumMeals = facilityContextNumMeals;
	}

	public Boolean getFacilityContextCentralOffice() {
		return facilityContextCentralOffice;
	}

	public void setFacilityContextCentralOffice(Boolean facilityContextCentralOffice) {
		this.facilityContextCentralOffice = facilityContextCentralOffice;
	}

}
