package org.bocogop.wr.util.context;

import org.bocogop.wr.model.facility.Facility;

/**
 * Represents the contract for any class responsible for providing session-bound
 * values.
 * 
 */
public interface ContextManager {
	Facility getFacilityContext();

	void setFacilityContext(Facility f);

	String getFacilityContextName();

	void setFacilityContextName(String facilityContextName);

	Integer getFacilityContextNumMeals();

	void setFacilityContextNumMeals(Integer facilityContextNumMeals);

	Boolean getFacilityContextCentralOffice();

	void setFacilityContextCentralOffice(Boolean facilityContextCentralOffice);
}