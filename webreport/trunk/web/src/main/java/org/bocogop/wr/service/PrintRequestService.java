package org.bocogop.wr.service;

import java.time.ZonedDateTime;
import java.util.List;

public interface PrintRequestService {

	int bulkUpdate(List<Long> printRequestIds, ZonedDateTime setCompletedDate);

	int bulkDeleteByCriteria(ZonedDateTime requestedTimeBefore, ZonedDateTime completedTimeBefore);

}
