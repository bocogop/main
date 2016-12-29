package org.bocogop.wr.model.notification;

import com.fasterxml.jackson.annotation.JsonFormat;

import org.bocogop.shared.persistence.usertype.CodedEnum;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum NotificationSeverityType implements CodedEnum {
	HIGH("H", "High"), //
	MEDIUM("M", "Medium"), //
	LOW("L", "Low"), //
	;

	private String code;
	private String name;

	private NotificationSeverityType(String code, String name) {
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
