package org.bocogop.shared.util;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Contains utility methods for manipulating dates as well as helpful formatting
 * constants. All "AM" / "PM" designations are in uppercase by convention. In
 * addition to static methods, this class is also a stateful component which can
 * be injected for processing things like Fiscal year calculations, which depend
 * on properties file entries.
 * 
 * @author Connor Barry
 * 
 */
public final class DateUtil {
	
	public static final String MILITARY_DATE_TIME = "M/d/yyyy HH:mm:ss";
	public static final String MILITARY_DATE_TIME_WITH_ZONE = MILITARY_DATE_TIME + " z";

	public static String localFormat(ZoneId userZone, ZonedDateTime date) {
		if (date == null)
			return "(unspecified)";
		return (userZone == null ? date : date.withZoneSameInstant(userZone))
				.format(DateTimeFormatter.ofPattern(MILITARY_DATE_TIME_WITH_ZONE));
	}

	public static String getDateRangeDescription(ZoneId userZone, ZonedDateTime startDate, ZonedDateTime endDate) {
		if (startDate != null && endDate != null) {
			return "between " + localFormat(userZone, startDate) + " and " + localFormat(userZone, endDate);
		} else if (startDate != null) {
			return "from " + localFormat(userZone, startDate) + " onward";
		} else {
			return "before " + localFormat(userZone, endDate);
		}
	}

}
