package org.bocogop.wr.persistence.queryCustomization.fieldTypes;

import org.bocogop.wr.model.requirement.VolunteerRequirement;

public enum VolunteerRequirementAssociationFieldType implements ModelAssociationFieldType {
	VOLUNTEER("volunteer"), //
	REQUIREMENT("requirement"), //
	;

	private String fieldName;

	private VolunteerRequirementAssociationFieldType(String fieldName) {
		this.fieldName = fieldName;
	}

	public String getFieldName() {
		return fieldName;
	}

	@Override
	public Class<?> getModelClass() {
		return VolunteerRequirement.class;
	}

}