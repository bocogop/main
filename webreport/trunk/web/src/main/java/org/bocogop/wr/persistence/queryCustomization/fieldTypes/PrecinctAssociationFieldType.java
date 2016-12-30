package org.bocogop.wr.persistence.queryCustomization.fieldTypes;

import org.bocogop.wr.model.precinct.Precinct;

public enum PrecinctAssociationFieldType implements ModelAssociationFieldType {
	PARENT("parent"), //
	;

	private String fieldName;

	private PrecinctAssociationFieldType(String fieldName) {
		this.fieldName = fieldName;
	}

	public String getFieldName() {
		return fieldName;
	}

	@Override
	public Class<?> getModelClass() {
		return Precinct.class;
	}

}