package org.bocogop.wr.model.mealTicket;

import org.bocogop.shared.persistence.usertype.CodedEnum;

public enum MealTicketRequestType implements CodedEnum {
	AUTOMATIC("A"), //
	MANUAL("M");

	private String code;

	private MealTicketRequestType(String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}

}
