package org.bocogop.wr.service;

import org.bocogop.shared.service.validation.ServiceValidationException;
import org.bocogop.wr.model.precinct.Precinct;

public interface PrecinctService {

	Precinct saveOrUpdate(Precinct precinct) throws ServiceValidationException;

	void delete(long precinctId);

}
