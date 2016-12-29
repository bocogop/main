package org.bocogop.wr.persistence.queryCustomization.fieldTypes.lookup.sds;

import org.bocogop.shared.model.lookup.sds.VAFacilityType;
import org.bocogop.wr.persistence.queryCustomization.fieldTypes.ModelAssociationFieldType;

public enum FacilityTypeAssociationFieldType implements ModelAssociationFieldType {
	INSTITUTION_LIST("institutionList");

	private String fieldName;

	private FacilityTypeAssociationFieldType(String fieldName) {
		this.fieldName = fieldName;
	}

	public String getFieldName() {
		return fieldName;
	}

	@Override
	public Class<?> getModelClass() {
		return VAFacilityType.class;
	}

}