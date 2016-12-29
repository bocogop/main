package org.bocogop.wr.persistence.dao;

import java.time.ZonedDateTime;
import java.util.List;

import org.bocogop.wr.model.printing.PrintRequest;

public interface PrintRequestDAO extends CustomizableSortedDAO<PrintRequest> {

	List<PrintRequest> findByCriteria(Long kioskId, ZonedDateTime requestedBefore, Boolean completedStatus,
			ZonedDateTime completedBefore);

	int bulkUpdate(List<Long> printRequestIds, ZonedDateTime setCompletedDate);

	int bulkDeleteByCriteria(ZonedDateTime requestedTimeBefore, ZonedDateTime completedTimeBefore);

}
