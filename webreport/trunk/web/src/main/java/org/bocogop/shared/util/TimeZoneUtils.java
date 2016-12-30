package org.bocogop.shared.util;

import java.text.NumberFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.SortedSet;
import java.util.TimeZone;
import java.util.TreeSet;

/**
 * Supports TimeZone functions/manipulation/conversion
 */
public class TimeZoneUtils {

	private static final List<String> PRIORITY_ZONES;

	/*
	 * List of time zones we present to the user; the four US/* time zones come
	 * first in the list - CPB
	 */
	public static final LinkedHashMap<ZoneId, String> TIME_ZONES = new LinkedHashMap<>();

	private static final NumberFormat HOURS_OFFSET_FORMATTER = NumberFormat.getNumberInstance();

	static {
		HOURS_OFFSET_FORMATTER.setMaximumFractionDigits(2);

		List<String> priorityZones = new ArrayList<String>();
		priorityZones.add("US/Eastern");
		priorityZones.add("US/Central");
		priorityZones.add("US/Mountain");
		priorityZones.add("US/Pacific");
		PRIORITY_ZONES = Collections.unmodifiableList(priorityZones);

		Instant now = Instant.now();

		/* Sort by GMT offset, then by name */
		Comparator<ZoneId> tzComparator = new Comparator<ZoneId>() {
			public int compare(ZoneId o1, ZoneId o2) {
				if (o1.equals(o2))
					return 0;
				ZoneOffset o1Offset = o1.getRules().getStandardOffset(now);
				ZoneOffset o2Offset = o2.getRules().getStandardOffset(now);

				int comparison = o1Offset.compareTo(o2Offset);
				if (comparison != 0)
					return comparison;

				String o1Name = o1.getId(); // o1.getDisplayName(TextStyle.SHORT_STANDALONE,
											// Locale.US);
				String o2Name = o2.getId(); // o2.getDisplayName(TextStyle.SHORT_STANDALONE,
											// Locale.US);
				return o1Name.compareTo(o2Name) > 0 ? 1 : -1;
			}
		};

		SortedSet<ZoneId> usZoneSet = new TreeSet<>(tzComparator);
		SortedSet<ZoneId> nonUSZoneSet = new TreeSet<>(tzComparator);
		for (String id : ZoneId.getAvailableZoneIds()) {
			ZoneId zone = ZoneId.of(id);
			if (TimeZoneUtils.PRIORITY_ZONES.contains(id)) {
				usZoneSet.add(zone);
			} else {
				nonUSZoneSet.add(zone);
			}
		}

		List<ZoneId> zoneListWithUSFirst = new ArrayList<>(usZoneSet);
		zoneListWithUSFirst.addAll(nonUSZoneSet);

		for (ZoneId zone : zoneListWithUSFirst)
			TIME_ZONES.put(zone, getUserDescription(zone));
	}

	public static String getUserDescription(ZoneId zone) {
		ZoneOffset offset = zone.getRules().getOffset(Instant.now());
		String displayName = offset.getDisplayName(TextStyle.FULL_STANDALONE, Locale.getDefault());
		String zoneName = zone.getId(); // zone.getDisplayName(TextStyle.SHORT_STANDALONE,
										// Locale.getDefault());
		return "UTC" + displayName + " - " + zoneName;
	}

	/**
	 * Does a best attempt to identify TimeZone. It can not be exact without the
	 * true TimeZone name (eg, America/New York) due to differing Daylight
	 * Saving time rules that can be shared within the same GMT offset. Gives
	 * higher weight to the four main US TimeZones.
	 * 
	 * @param rawOffsetMillis
	 * @param observesDaylightSaving
	 * @return
	 */
	public static TimeZone getTimeZone(int rawOffsetMillis, boolean observesDaylightSaving) {
		TimeZone targetTimeZone = null;
		Set<TimeZone> matchingTimeZones = doGetTimeZones(rawOffsetMillis, observesDaylightSaving);
		for (TimeZone timeZone : matchingTimeZones) {
			if (targetTimeZone == null) {
				targetTimeZone = timeZone;
			}

			if (PRIORITY_ZONES.contains(timeZone.getID())) {
				targetTimeZone = timeZone;
				break;
			}
		}
		return targetTimeZone;
	}

	private static Set<TimeZone> doGetTimeZones(int rawOffsetMillis, boolean observesDaylightSaving) {
		Set<TimeZone> targetTimeZones = new HashSet<TimeZone>();
		String[] matchingZones = TimeZone.getAvailableIDs(rawOffsetMillis);
		TimeZone timeZone = null;

		for (int i = 0; i < matchingZones.length; i++) {
			timeZone = TimeZone.getTimeZone(matchingZones[i]);
			if (timeZone.useDaylightTime() == observesDaylightSaving) {
				targetTimeZones.add(timeZone);
			}
		}

		return targetTimeZones;
	}

}
