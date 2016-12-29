package org.bocogop.wr.service;

import org.bocogop.shared.service.validation.ServiceValidationException;
import org.bocogop.wr.model.time.AdjustedHoursEntry;

public interface AdjustedHoursEntryService {

	AdjustedHoursEntry saveOrUpdate(AdjustedHoursEntry entry) throws ServiceValidationException;

	void delete(long adjustedHoursEntryId);

}
