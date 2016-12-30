package org.bocogop.wr.service;

import org.bocogop.wr.model.precinct.Precinct;
import org.bocogop.wr.service.validation.ServiceValidationException;

public interface PrecinctService {

	Precinct saveOrUpdate(Precinct precinct) throws ServiceValidationException;

	void delete(long precinctId);

}
