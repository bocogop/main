package org.bocogop.shared.persistence.dao.voter.demographics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// If these are reordered, change the VolDemoDAOImpl.getOrderByCols method too
public enum VolDemoColumn {
	NAME("Name", true, false, true), //
	DOB("Date of Birth", true, true, true), //
	AGE("Age", true, false, true), //
	AGE_GROUP("Age Group", false, true, true), //
	GENDER("Gender", true, true, true), //
	IDENTIFYING_CODE("Identifying Code", true, false, true), //
	ENTRY_DATE("Entry Date", true, true, true), //

	FULL_ADDRESS("Full Address", true, false, true), //
	STREET("Street", false, false, true), //
	CITY("City", false, false, true), //
	STATE("State", false, true, true), //
	ZIP("Zip", false, false, true), //
	PARKING_STICKERS("Parking Stickers", false, false, false), //
	UNIFORMS("Uniforms", false, false, false), //

	FULL_CONTACT("Full Contact", true, false, true), //
	PHONE("Phone", false, false, true), //
	ALT_PHONE("Alt Phone", false, false, true), //
	ALT_PHONE2("Alt Phone 2", false, false, true), //
	EMAIL("Email", false, false, true), //
	EMERGENCY_CONTACT("Emergency Contact", false, false, true), //

	STATUS("Status", false, false, true), //
	STATUS_DATE("Status Date", false, true, true), //
	PRIMARY_PRECINCT("Primary Precinct", false, true, true), //
	ACTIVE_ASSIGNMENTS("Active Assignments", true, false, false), //
	SUPERVISORS("Supervisors", false, false, false), //
	LAST_VOTERED_DATE("Last Votered Date", "Last Vol Date", true, true, true), //
	PRIMARY_ORGANIZATION("Primary Organization", "Primary Org", false, false, true), //

	CURRENT_YEAR_HOURS("Current Year Hours", "Current Yr Hrs", false, false, true), //
	PRIOR_HOURS("Prior Hours", false, false, true), //
	ADJUSTED_HOURS("Adjusted Hours", "Adj Hours", false, false, true), //
	TOTAL_HOURS("Total Hours", false, false, true), //
	TOTAL_DONATIONS("Total Donations", false, false, false), //
	HOURS_LAST_AWARD("Hours Last Award", false, false, true), //
	DATE_LAST_AWARD("Date Last Award", false, true, true);
	;

	static List<VolDemoColumn> DIVIDERS_AFTER = Arrays.asList(ENTRY_DATE, UNIFORMS, EMERGENCY_CONTACT, PRIMARY_ORGANIZATION);

	private String fullName;
	private String shortName;
	private boolean initiallyChecked;
	private boolean filtered;
	private boolean alwaysSelected;
	
	private VolDemoColumn(String fullName, boolean initiallyChecked, boolean filtered, boolean alwaysSelected) {
		this(fullName, fullName, initiallyChecked, filtered, alwaysSelected);
	}

	private VolDemoColumn(String fullName, String shortName, boolean initiallyChecked, boolean filtered,
			boolean alwaysSelected) {
		this.fullName = fullName;
		this.shortName = shortName;
		this.initiallyChecked = initiallyChecked;
		this.filtered = filtered;
		this.alwaysSelected = alwaysSelected;
	}

	public String getFullName() {
		return fullName;
	}

	public String getShortName() {
		return shortName;
	}

	public boolean isInitiallyChecked() {
		return initiallyChecked;
	}

	public boolean isFiltered() {
		return filtered;
	}

	public boolean isAlwaysSelected() {
		return alwaysSelected;
	}

	public static Map<Integer, List<VolDemoColumn>> getColumnsByDivider() {
		Map<Integer, List<VolDemoColumn>> results = new LinkedHashMap<>();
		int colIndex = 0;
		List<VolDemoColumn> l = new ArrayList<>();
		results.put(colIndex, l);

		for (VolDemoColumn c : values()) {
			l.add(c);
			if (DIVIDERS_AFTER.contains(c)) {
				colIndex++;
				l = new ArrayList<>();
				results.put(colIndex, l);
			}
		}

		return results;
	}

	public static EnumSet<VolDemoColumn> getWithIndexes(int[] displayColumnIndexes) {
		EnumSet<VolDemoColumn> results = EnumSet.noneOf(VolDemoColumn.class);
		VolDemoColumn[] vals = VolDemoColumn.values();
		for (int i : displayColumnIndexes)
			results.add(vals[i]);
		return results;
	}

}