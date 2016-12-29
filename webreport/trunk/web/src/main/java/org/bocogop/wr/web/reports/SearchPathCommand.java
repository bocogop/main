package org.bocogop.wr.web.reports;

public enum SearchPathCommand {

	ALL("ALL", "All Volunteers"), AGE("AGE", "Age Range"), BIRTH_MONTH("BIRTH_MONTH",
			"Birth Month"), SPECIFIC_VOLUNTEERS("SPECIFIC_VOLUNTEERS", "Specific Volunteers (Not Implemeted)"), ORGANIZATION(
					"ORGANIZATION", "Organization (Not Implemeted)"), SERVICE("SERVICE", "Service (Not Implemeted)"), ZIP("ZIP", "Zip Code (Not Implemeted)");

	private String id;
	private String name;

	private SearchPathCommand(String id, String name) {
		this.id = id;
		this.name = name;
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

}
