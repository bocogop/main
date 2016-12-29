package org.bocogop.wr.model.notification;

import com.fasterxml.jackson.annotation.JsonFormat;

import org.bocogop.shared.persistence.usertype.CodedEnum;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum NotificationLinkType implements CodedEnum {
	VOLUNTEER_PROFILE("V", "Volunteer Profile"), //
	VOLUNTEER_AUDIT_COMPARE("A", "Volunteer Data Changes"), //
	LEIE_REPORT("L", "LEIE Report"), //
	DONATION_LOG("D", "Donation Log"), //
	;

	private String code;
	private String name;

	private NotificationLinkType(String code, String name) {
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
