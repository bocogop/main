package org.bocogop.wr.model.views;

import com.fasterxml.jackson.annotation.JsonFormat;

import org.bocogop.shared.persistence.usertype.CodedEnum;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum DonationSummaryLetterType implements CodedEnum {
	TYPE_1("1", "Monetary Donation In Memory Of"), //
	TYPE_2("2", "Monetary Donation Family Contact"), //
	TYPE_3("3", "Monetary Donation Not In Memory Of"), //
	TYPE_4("4", "Activity Donation"), //
	TYPE_5("5", "Item Donation");

	private String code;
	private String name;

	private DonationSummaryLetterType(String code, String name) {
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
