package org.bocogop.wr.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.bocogop.shared.model.AppUser;
import org.bocogop.shared.service.AppUserService;
import org.bocogop.wr.model.facility.Facility;
import org.bocogop.wr.model.voluntaryService.VoluntaryServiceStaff;
import org.bocogop.wr.service.VoluntaryServiceStaffService;

@Service
public class VoluntaryServiceStaffServiceImpl extends AbstractServiceImpl implements VoluntaryServiceStaffService {
	private static final Logger log = LoggerFactory.getLogger(VoluntaryServiceStaffServiceImpl.class);

	@Autowired
	private AppUserService appUserService;

	@Override
	public VoluntaryServiceStaff saveOrUpdate(VoluntaryServiceStaff serviceStaff) {
		return voluntaryServiceStaffDAO.saveOrUpdate(serviceStaff);
	}

	@Override
	public void delete(long serviceStaffId) {
		voluntaryServiceStaffDAO.delete(serviceStaffId);
	}

	@Override
	public VoluntaryServiceStaff createOrRetrieveServiceStaff(String staffAppUserName, long facilityId) {
		List<VoluntaryServiceStaff> results = voluntaryServiceStaffDAO.findByCriteria(facilityId, staffAppUserName);
		if (!results.isEmpty())
			return results.get(0);

		VoluntaryServiceStaff serviceStaff = new VoluntaryServiceStaff();
		Map<String, Object> userAdminCustomizationsModel = new HashMap<>();

		AppUser appUser = appUserService.createOrRetrieveUser(staffAppUserName, userAdminCustomizationsModel);

		serviceStaff.setAppUser(appUser);

		Facility facility = facilityDAO.findRequiredByPrimaryKey(facilityId);
		serviceStaff.setFacility(facility);

		serviceStaff = saveOrUpdate(serviceStaff);
		return serviceStaff;
	}

}
