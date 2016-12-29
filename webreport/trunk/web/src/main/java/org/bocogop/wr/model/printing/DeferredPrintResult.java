package org.bocogop.wr.model.printing;

import java.time.ZonedDateTime;
import java.util.List;

import org.springframework.web.context.request.async.DeferredResult;

public class DeferredPrintResult extends DeferredResult<List<String>> {

	private ZonedDateTime creationDate;
	private List<Long> printRequestIds;

	public DeferredPrintResult() {
		super();
		this.creationDate = ZonedDateTime.now();
	}

	public DeferredPrintResult(Long timeout, Object timeoutResult) {
		super(timeout, timeoutResult);
		this.creationDate = ZonedDateTime.now();
	}

	public DeferredPrintResult(Long timeout) {
		super(timeout);
		this.creationDate = ZonedDateTime.now();
	}

	public ZonedDateTime getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(ZonedDateTime creationDate) {
		this.creationDate = creationDate;
	}

	public List<Long> getPrintRequestIds() {
		return printRequestIds;
	}

	public void setPrintRequestIds(List<Long> printRequestIds) {
		this.printRequestIds = printRequestIds;
	}

}
