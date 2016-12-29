package org.bocogop.wr.service;

import org.bocogop.shared.service.validation.ServiceValidationException;
import org.bocogop.wr.model.facility.Facility;

public interface FacilityService {

	Facility saveOrUpdate(Facility facility) throws ServiceValidationException;

	void delete(long facilityId);

	void unlinkSDSFacilityFromFacility(long facilityId);

	void linkSDSFacilityToFacility(long facilityId, long vaFacilityId);

}
