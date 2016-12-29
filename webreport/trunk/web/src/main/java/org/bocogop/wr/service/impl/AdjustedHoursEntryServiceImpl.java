package org.bocogop.wr.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import org.bocogop.shared.service.validation.ServiceValidationException;
import org.bocogop.wr.model.time.AdjustedHoursEntry;
import org.bocogop.wr.service.AdjustedHoursEntryService;

@Service
public class AdjustedHoursEntryServiceImpl extends AbstractServiceImpl implements AdjustedHoursEntryService {
	private static final Logger log = LoggerFactory.getLogger(AdjustedHoursEntryServiceImpl.class);

	@Override
	public AdjustedHoursEntry saveOrUpdate(AdjustedHoursEntry adjustedHoursEntry) throws ServiceValidationException {
		if (adjustedHoursEntry.getDate()
				.isBefore(dateUtil.getEarliestAcceptableDateEntryAsOfNow(getFacilityTimeZone())))
			throw new ServiceValidationException("adjustedHours.error.dateTooEarly");
		if (adjustedHoursEntry.getDate().isAfter(getTodayAtFacility()))
			throw new ServiceValidationException("adjustedHours.error.futureDateDisallowed");
		
		return adjustedHoursEntryDAO.saveOrUpdate(adjustedHoursEntry);
	}

	@Override
	public void delete(long adjustedHoursEntryId) {
		adjustedHoursEntryDAO.delete(adjustedHoursEntryId);
	}

}
