package org.bocogop.wr.service.impl;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import org.bocogop.shared.service.validation.ServiceValidationException;
import org.bocogop.wr.model.benefitingService.BenefitingServiceRole;
import org.bocogop.wr.model.facility.Facility;
import org.bocogop.wr.model.facility.Location;
import org.bocogop.wr.service.LocationService;

@Service
public class LocationServiceImpl extends AbstractServiceImpl implements LocationService {
	private static final Logger log = LoggerFactory.getLogger(LocationServiceImpl.class);

	@Override
	public Location saveOrUpdate(Location location) throws ServiceValidationException {
		locationDAO.detach(location);

		boolean inactivating = false;
		boolean activating = false;

		if (location.isPersistent()) {
			Location existingLocation = locationDAO.findRequiredByPrimaryKey(location.getId());
			inactivating = !existingLocation.isInactive() && location.isInactive();
			activating = existingLocation.isInactive() && !location.isInactive();
		}

		location = locationDAO.saveOrUpdate(location);

		if (inactivating) {
			cascadeInactivation(location.getId());
		} else if (activating) {
			ensureParentFacilityActive(location);
		}

		locationDAO.flushAndRefresh(location);

		return location;
	}

	public boolean canBeDeleted(long locationId) {
		List<BenefitingServiceRole> serviceRolesReferencingLocation = benefitingServiceRoleDAO.findByCriteria(null,
				Arrays.asList(locationId), false, null);
		int[] volAssignmentCounts = volunteerAssignmentDAO.countByCriteria(locationId);
		return serviceRolesReferencingLocation.isEmpty() && volAssignmentCounts[1] == 0;
	}

	public void deleteOrInactivate(long locationId) throws ServiceValidationException {
		if (canBeDeleted(locationId)) {
			deleteLocationInternal(locationId, false);
		} else {
			inactivate(locationId);
		}
	}

	public void delete(long locationId) {
		deleteLocationInternal(locationId, true);
	}

	private void deleteLocationInternal(long locationId, boolean check) {
		if (!check || canBeDeleted(locationId)) {
			locationDAO.delete(locationId);
		}
	}

	public void inactivate(long locationId) {
		Location l = locationDAO.findRequiredByPrimaryKey(locationId);
		l.setActive(false);
		locationDAO.saveOrUpdate(l);

		cascadeInactivation(locationId);
	}

	public void cascadeInactivation(long locationId) {
		volunteerAssignmentDAO.bulkInactivateByCriteria(null, null, null, null, locationId);
		benefitingServiceDAO.bulkUpdateByCriteria(null, locationId, false, null, false, null, false, null, false, null);
		benefitingServiceRoleDAO.bulkUpdateByCriteria(null, null, null, locationId, null, null, false, null);
	}

	@Override
	public void reactivate(long locationId) throws ServiceValidationException {
		Location location = locationDAO.findRequiredByPrimaryKey(locationId);
		boolean wasInactive = location.isInactive();

		ensureParentFacilityActive(location);

		if (wasInactive) {
			location.setInactive(false);
			location = locationDAO.saveOrUpdate(location);
		}
	}

	public void ensureParentFacilityActive(Location location) throws ServiceValidationException {
		Facility facility = location.getFacility();
		if (facility.isInactive())
			throw new ServiceValidationException("location.error.parentInactive",
					new Serializable[] { facility.getDisplayName() });
	}

}
