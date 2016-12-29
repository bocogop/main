package org.bocogop.wr.model.notification;

import com.fasterxml.jackson.annotation.JsonFormat;

import org.bocogop.shared.persistence.usertype.CodedEnum;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum NotificationType implements CodedEnum {
	DONATION("D", "Donation", 2), //
	OPPORTUNITY("O", "Opportunity", 5), //
	SYSTEM("S", "System", 3), //
	TMS("T", "TMS", 4), //
	LEIE("L", "LEIE", 1), //
	;

	private String code;
	private String name;
	private int sortOrder;

	private NotificationType(String code, String name, int sortOrder) {
		this.code = code;
		this.name = name;
		this.sortOrder = sortOrder;
	}

	public String getName() {
		return name;
	}

	public String getCode() {
		return code;
	}

	public int getSortOrder() {
		return sortOrder;
	}
	
	

}
