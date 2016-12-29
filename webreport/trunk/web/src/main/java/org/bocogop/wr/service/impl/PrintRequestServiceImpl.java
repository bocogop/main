package org.bocogop.wr.service.impl;

import java.time.ZonedDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import org.bocogop.wr.service.PrintRequestService;

@Service
public class PrintRequestServiceImpl extends AbstractServiceImpl implements PrintRequestService {
	private static final Logger log = LoggerFactory.getLogger(PrintRequestServiceImpl.class);

	@Override
	public int bulkUpdate(List<Long> printRequestIds, ZonedDateTime setCompletedDate) {
		return printRequestDAO.bulkUpdate(printRequestIds, setCompletedDate);
	}

	@Override
	public int bulkDeleteByCriteria(ZonedDateTime requestedTimeBefore, ZonedDateTime completedTimeBefore) {
		return printRequestDAO.bulkDeleteByCriteria(requestedTimeBefore, completedTimeBefore);
	}

}
