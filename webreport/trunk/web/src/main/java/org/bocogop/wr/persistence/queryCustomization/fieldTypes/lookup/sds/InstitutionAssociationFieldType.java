package org.bocogop.wr.persistence.queryCustomization.fieldTypes.lookup.sds;

import org.bocogop.shared.model.lookup.sds.VAFacility;
import org.bocogop.wr.persistence.queryCustomization.fieldTypes.ModelAssociationFieldType;

public enum InstitutionAssociationFieldType implements ModelAssociationFieldType {
	PARENT("parent"), VISN("visn"), FACILITY_TYPE("facilityType");

	private String fieldName;

	private InstitutionAssociationFieldType(String fieldName) {
		this.fieldName = fieldName;
	}

	public String getFieldName() {
		return fieldName;
	}

	@Override
	public Class<?> getModelClass() {
		return VAFacility.class;
	}

}