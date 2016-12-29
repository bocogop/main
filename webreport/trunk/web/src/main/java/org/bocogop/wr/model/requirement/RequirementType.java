package org.bocogop.wr.model.requirement;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.PUBLIC_ONLY;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonFormat;

import org.bocogop.shared.persistence.usertype.CodedEnum;

/**
 * Defines stereotypes for requirements for custom logic in the app.
 */
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = PUBLIC_ONLY, isGetterVisibility = PUBLIC_ONLY)
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum RequirementType implements CodedEnum {
	STANDARD("S", "Standard"), //
	// ... OHRS, TMS, etc
	;

	private String code;
	private String name;

	private RequirementType(String code, String name) {
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
