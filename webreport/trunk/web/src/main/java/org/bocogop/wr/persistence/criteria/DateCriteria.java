package org.bocogop.wr.persistence.criteria;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

public class DateCriteria {

	static enum DateCriteriaType {
		ACTIVE_AS_OF_DATE, INACTIVE_AS_OF_DATE, ACTIVE_AFTER_DATE, ACTIVE_ON_OR_AFTER_DATE;
	}

	private DateCriteriaType type;
	private ZonedDateTime date;

	private DateCriteria(DateCriteriaType type, ZonedDateTime date) {
		this.type = type;
		this.date = date;
	}

	public void append(List<String> whereClauseItems, Map<String, Object> params, String fromDatePath,
			String toDatePath) {
		if (type == DateCriteriaType.ACTIVE_AS_OF_DATE) {
			whereClauseItems.add("coalesce(" + fromDatePath + ", '1900-01-01') <= :ensureActiveAsOfDate");
			whereClauseItems.add("coalesce(" + toDatePath + ", '2199-01-01') > :ensureActiveAsOfDate");
			params.put("ensureActiveAsOfDate", date);
		} else if (type == DateCriteriaType.INACTIVE_AS_OF_DATE) {
			whereClauseItems.add("(:ensureInactiveAsOfDate < coalesce(" + fromDatePath + ", '1900-01-01') or"
					+ " :ensureInactiveAsOfDate >= coalesce(" + toDatePath + ", '2199-01-01'))");
			params.put("ensureInactiveAsOfDate", date);
		} else if (type == DateCriteriaType.ACTIVE_AFTER_DATE) {
			whereClauseItems.add(":ensureActiveAfterDate < coalesce(" + fromDatePath + ", '1900-01-01')");
			params.put("ensureActiveAfterDate", date);
		} else if (type == DateCriteriaType.ACTIVE_ON_OR_AFTER_DATE) {
			whereClauseItems.add("(" + toDatePath + " is null or coalesce(" + toDatePath
					+ ", '2199-01-01') > :ensureActiveOnOrAfterDate)");
			params.put("ensureActiveOnOrAfterDate", date);
		}
	}

	public ZonedDateTime getEnsureActiveAsOfDate() {
		return type == DateCriteriaType.ACTIVE_AS_OF_DATE ? date : null;
	}

	public ZonedDateTime getEnsureInactiveAsOfDate() {
		return type == DateCriteriaType.INACTIVE_AS_OF_DATE ? date : null;
	}

	public ZonedDateTime getActiveAfterDate() {
		return type == DateCriteriaType.ACTIVE_AFTER_DATE ? date : null;
	}

	public ZonedDateTime getActiveOnOrAfterDate() {
		return type == DateCriteriaType.ACTIVE_ON_OR_AFTER_DATE ? date : null;
	}

	public static DateCriteria activeAsOfDate(ZonedDateTime d) {
		return new DateCriteria(DateCriteriaType.ACTIVE_AS_OF_DATE, d);
	}

	public static DateCriteria inactiveAsOfDate(ZonedDateTime d) {
		return new DateCriteria(DateCriteriaType.INACTIVE_AS_OF_DATE, d);
	}

	public static DateCriteria activeAfterDate(ZonedDateTime d) {
		return new DateCriteria(DateCriteriaType.ACTIVE_AFTER_DATE, d);
	}

	public static DateCriteria activeOnOrAfterDate(ZonedDateTime d) {
		return new DateCriteria(DateCriteriaType.ACTIVE_ON_OR_AFTER_DATE, d);
	}

}
