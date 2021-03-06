package org.bocogop.shared.util;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import org.bocogop.shared.model.AppUser;

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

	public static final String DATE_ONLY = "M/d/yyyy";
	public static final DateTimeFormatter DATE_ONLY_FORMAT = DateTimeFormatter.ofPattern(DATE_ONLY);

	public static final String TWO_DIGIT_DATE_ONLY = "MM/dd/yyyy";
	public static final DateTimeFormatter TWO_DIGIT_DATE_ONLY_FORMAT = DateTimeFormatter.ofPattern(TWO_DIGIT_DATE_ONLY);

	public static final String MILITARY_DATE_TIME = "M/d/yyyy HH:mm:ss";
	public static final DateTimeFormatter MILITARY_DATE_TIME_FORMAT = DateTimeFormatter.ofPattern(MILITARY_DATE_TIME);

	public static final String MILITARY_DATE_TIME_WITH_ZONE = MILITARY_DATE_TIME + " z";
	public static final DateTimeFormatter MILITARY_DATE_TIME_WITH_ZONE_FORMAT = DateTimeFormatter
			.ofPattern(MILITARY_DATE_TIME_WITH_ZONE);

	public static final String TWO_DIGIT_MILITARY_DATE_TIME = "MM/dd/yyyy HH:mm:ss";
	public static final DateTimeFormatter TWO_DIGIT_MILITARY_DATE_TIME_FORMAT = DateTimeFormatter
			.ofPattern(TWO_DIGIT_MILITARY_DATE_TIME);

	public static final String TWO_DIGIT_MONTH_AND_DAY_ONLY = "MMdd";
	public static final DateTimeFormatter TWO_DIGIT_MONTH_AND_DAY_ONLY_FORMAT = DateTimeFormatter
			.ofPattern(TWO_DIGIT_MONTH_AND_DAY_ONLY);

	public static final String TWO_DIGIT_HOUR_AND_MINUTE_ONLY = "HHmm";
	public static final DateTimeFormatter TWO_DIGIT_HOUR_AND_MINUTE_ONLY_FORMAT = DateTimeFormatter
			.ofPattern(TWO_DIGIT_HOUR_AND_MINUTE_ONLY);

	/* Useful when sorting */
	public static final String MSD_TO_LSD = "yyyyMMddHHmm";
	public static final DateTimeFormatter MSD_TO_LSD_FORMAT = DateTimeFormatter.ofPattern(MSD_TO_LSD);

	public static final String TWO_DIGIT_DATE_MASK = "99/99/9999";
	public static final String TWO_DIGIT_MONTH_YEAR_MASK = "99/9999";
	public static final String TWO_DIGIT_DATE_TIME_MASK = "99/99/9999 99:99";

	public static final ZoneId UTC = ZoneId.of("Z");

	public static ZonedDateTime currentMinute() {
		return ZonedDateTime.now().withSecond(0).withNano(0);
	}

	public static String localFormat(AppUser user, ZonedDateTime date) {
		return localFormat(user.getTimeZone(), date);
	}

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

	/**
	 * Helper method to reduce boilerplate code - JDK runs in UTC so this will
	 * initially be set to UTC zone.
	 * 
	 * @param d
	 * @return
	 */
	public static ZonedDateTime getZonedDateTime(Date d) {
		if (d == null)
			return null;
		return ZonedDateTime.ofInstant(d.toInstant(), ZoneId.systemDefault());
	}

	public static Date asDate(LocalDate localDate) {
		if (localDate == null)
			return null;
		return Date.from(localDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
	}

	public static Date asDate(LocalDateTime localDateTime) {
		if (localDateTime == null)
			return null;
		return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
	}

	public static LocalDate asLocalDate(Date date) {
		if (date == null)
			return null;
		return Instant.ofEpochMilli(date.getTime()).atZone(ZoneId.systemDefault()).toLocalDate();
	}

	public static LocalDateTime asLocalDateTime(Date date) {
		if (date == null)
			return null;
		return Instant.ofEpochMilli(date.getTime()).atZone(ZoneId.systemDefault()).toLocalDateTime();
	}

}
