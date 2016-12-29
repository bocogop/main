package org.bocogop.wr.model.organization;

import org.bocogop.shared.persistence.usertype.CodedEnum;

public enum ScopeType implements CodedEnum {
	NATIONAL("N"), //
	LOCAL("L"), //
	GAMES("G");

	private String code;

	private ScopeType(String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}

}
