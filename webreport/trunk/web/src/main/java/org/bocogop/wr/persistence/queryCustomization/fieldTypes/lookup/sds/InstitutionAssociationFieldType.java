package org.bocogop.wr.persistence.queryCustomization.fieldTypes.lookup.sds;

import org.bocogop.wr.model.precinct.Precinct;
import org.bocogop.wr.persistence.queryCustomization.fieldTypes.ModelAssociationFieldType;

public enum InstitutionAssociationFieldType implements ModelAssociationFieldType {
	PARENT("parent"), VISN("visn"), PRECINCT_TYPE("precinctType");

	private String fieldName;

	private InstitutionAssociationFieldType(String fieldName) {
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