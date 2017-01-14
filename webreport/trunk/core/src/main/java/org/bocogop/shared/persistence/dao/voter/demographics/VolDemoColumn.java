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
	VOTER_ID("Voter ID", true, false, true), //
	PRECINCT("Precinct", true, false, true), //
	PARTY("Party", true, true, true), //
	AFFILIATED_DATE("Affiliated Date", false, false, true), //

	REGISTRATION_DATE("Registration Date", false, false, true), //
	EFFECTIVE_DATE("Effective Date", false, false, true), //
	STATUS("Status", true, false, true), //
	STATUS_REASON("Status Reason", false, false, true), //

	FULL_ADDRESS("Full Address", true, false, true), //
	STREET("Street", false, false, true), //
	CITY("City", false, false, true), //
	STATE("State", false, true, true), //
	ZIP("Zip", false, false, true), //
	
	GENDER("Gender", true, true, true), //
	YOB("Birth Year", false, false, true), //
	AGE("Age (approx)", true, false, true), //
	MAILING_ADDRESS("Mailing Address", false, false, true), //
	BALLOT_ADDRESS("Ballot Address", false, false, true), //

	FULL_CONTACT("Full Contact", true, false, true), //
	PHONE("Phone", false, false, true), //
	FAX("Fax", false, false, true), //
	EMAIL("Email", false, false, true), //
	;

	static List<VolDemoColumn> DIVIDERS_AFTER = Arrays.asList(AFFILIATED_DATE, STATUS_REASON, BALLOT_ADDRESS, ZIP);

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