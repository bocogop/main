package org.bocogop.shared.service.impl;

import org.springframework.stereotype.Service;

import org.bocogop.shared.model.lookup.sds.VAFacility;
import org.bocogop.shared.service.VAFacilityService;

@Service
public class VAFacilityServiceImpl extends AbstractAppServiceImpl implements VAFacilityService {

	public VAFacility saveOrUpdate(VAFacility vaFacility) {
		return vaFacilityDAO.saveOrUpdate(vaFacility);
	}

	public void delete(long institutionId) {
		vaFacilityDAO.delete(institutionId);
	}

}
