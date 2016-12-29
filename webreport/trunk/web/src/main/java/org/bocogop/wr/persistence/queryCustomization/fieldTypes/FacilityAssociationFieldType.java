package org.bocogop.wr.persistence.queryCustomization.fieldTypes;

import org.bocogop.wr.model.facility.Facility;

public enum FacilityAssociationFieldType implements ModelAssociationFieldType {
	PARENT("parent"), //
	;

	private String fieldName;

	private FacilityAssociationFieldType(String fieldName) {
		this.fieldName = fieldName;
	}

	public String getFieldName() {
		return fieldName;
	}

	@Override
	public Class<?> getModelClass() {
		return Facility.class;
	}

}