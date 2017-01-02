package org.bocogop.shared.service;

import org.bocogop.shared.model.precinct.Precinct;
import org.bocogop.shared.service.validation.ServiceValidationException;

public interface PrecinctService {

	Precinct saveOrUpdate(Precinct precinct) throws ServiceValidationException;

	void delete(long precinctId);

}
