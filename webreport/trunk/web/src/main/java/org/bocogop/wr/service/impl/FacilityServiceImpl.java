package org.bocogop.wr.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import org.bocogop.shared.model.Permission.PermissionType;
import org.bocogop.shared.model.lookup.sds.VAFacility;
import org.bocogop.shared.service.validation.ServiceValidationException;
import org.bocogop.shared.util.SecurityUtil;
import org.bocogop.shared.util.StationsUtil;
import org.bocogop.wr.model.facility.AdministrativeUnit;
import org.bocogop.wr.model.facility.Facility;
import org.bocogop.wr.model.facility.FacilityType.FacilityTypeValue;
import org.bocogop.wr.service.FacilityService;

@Service
public class FacilityServiceImpl extends AbstractServiceImpl implements FacilityService {
	private static final Logger log = LoggerFactory.getLogger(FacilityServiceImpl.class);

	@Override
	public Facility saveOrUpdate(Facility facility) throws ServiceValidationException {
		/* Business-level validations */
		if (facility.getType() == null)
			facility.setType(facilityTypeDAO.findByLookup(FacilityTypeValue.TIMEKEEPING));
		if (SecurityUtil.hasAllPermissionsAtCurrentFacility(PermissionType.FACILITY_EDIT_ALL)) {
			facility = facilityDAO.saveOrUpdate(facility);
		} else if (SecurityUtil.hasAllPermissionsAtCurrentFacility(PermissionType.FACILITY_EDIT_CURRENT)) {
			stationParametersDAO.saveOrUpdate(facility.getStationParameters());
		}

		return facility;
	}

	@Override
	public void delete(long facilityId) {
		facilityDAO.delete(facilityId);
	}

	@Override
	public void unlinkSDSFacilityFromFacility(long facilityId) {
		facilityDAO.updateFieldsWithoutVersionIncrement(facilityId, true, null, false, null, false, null);
	}

	@Override
	public void linkSDSFacilityToFacility(long facilityId, long vaFacilityId) {
		VAFacility f = vaFacilityDAO.findRequiredByPrimaryKey(vaFacilityId);
		VAFacility sdsVisn = StationsUtil.getVisnForFacilityOrAnyAncestor(f);

		Long visnId = null;
		if (sdsVisn != null) {
			AdministrativeUnit visn = administrativeUnitDAO.findBySDSInstitution(sdsVisn.getId());
			if (visn != null)
				visnId = visn.getId();
		}

		facilityDAO.updateFieldsWithoutVersionIncrement(facilityId, true, vaFacilityId, visnId != null, visnId, true,
				f.getStationNumber());
	}

}
