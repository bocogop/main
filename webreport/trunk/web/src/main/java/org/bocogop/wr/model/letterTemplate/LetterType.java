package org.bocogop.wr.model.letterTemplate;

import org.bocogop.shared.persistence.usertype.CodedEnum;

public enum LetterType implements CodedEnum {
	TYPE_1("TYPE_1", "Cash/Check/Credit Card/E-Donate In Memory Of"), //
	TYPE_A2("TYPE_A2", "Family Contact (To Donor)"), //
	TYPE_B2("TYPE_B2", "Family Contact (To Family)"), //
	TYPE_3("TYPE_3", "Cash/Check/Credit Card/E-Donate Not in Memory Of"), //
	TYPE_4("TYPE_4", "Activity Donation"), //
	TYPE_5("TYPE_5", "Item Donation");

	private String code;
	private String name;

	private LetterType(String code, String name) {
		this.code = code;
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public String getCode() {
		return code;
	}

}
