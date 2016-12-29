package org.bocogop.wr.model.award;

import org.bocogop.shared.persistence.usertype.CodedEnum;

public enum AwardType implements CodedEnum {
	ADULT("A", "Adult", "ADULT"), //
	YOUTH("Y", "Youth", "YOUTH"), //
	OTHER("O", "Other", "OTHER");

	private String code;
	private String name;
	private String fullCode;

	private AwardType(String code, String name, String fullCode) {
		this.code = code;
		this.name = name;
		this.fullCode = fullCode;
	}

	public String getName() {
		return name;
	}

	public String getCode() {
		return code;
	}

	public String getFullCode() {
		return fullCode;
	}

}
