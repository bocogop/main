package org.bocogop.wr.persistence.queryCustomization.fieldTypes;

import org.bocogop.wr.model.time.WorkEntry;

public enum WorkEntryAssociationFieldType implements ModelAssociationFieldType {
	ORGANIZATION("organization"), //
	VOLUNTEER_ASSIGNMENT("volunteerAssignment"), //
	;

	private String fieldName;

	private WorkEntryAssociationFieldType(String fieldName) {
		this.fieldName = fieldName;
	}

	public String getFieldName() {
		return fieldName;
	}

	@Override
	public Class<?> getModelClass() {
		return WorkEntry.class;
	}

}