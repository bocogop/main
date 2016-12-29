package org.bocogop.shared.service;

import org.bocogop.shared.model.lookup.sds.VAFacility;

public interface VAFacilityService {

	VAFacility saveOrUpdate(VAFacility facility);

	void delete(long facilityId);

}
