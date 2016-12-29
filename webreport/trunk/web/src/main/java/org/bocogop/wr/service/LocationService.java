package org.bocogop.wr.service;

import org.bocogop.shared.service.validation.ServiceValidationException;
import org.bocogop.wr.model.facility.Location;

public interface LocationService {

	Location saveOrUpdate(Location location) throws ServiceValidationException;

	void deleteOrInactivate(long facilityId) throws ServiceValidationException;

	void delete(long facilityId) throws ServiceValidationException;

	void inactivate(long facilityId);

	void reactivate(long facilityId) throws ServiceValidationException;

}
