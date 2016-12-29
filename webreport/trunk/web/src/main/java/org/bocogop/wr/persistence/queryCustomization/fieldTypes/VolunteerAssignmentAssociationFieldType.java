package org.bocogop.wr.persistence.queryCustomization.fieldTypes;

import org.bocogop.wr.model.volunteer.VolunteerAssignment;

public enum VolunteerAssignmentAssociationFieldType implements ModelAssociationFieldType {
	VOLUNTEER("volunteer"), //
	FACILITY("facility"), //
	BENEFITING_SERVICE("benefitingService"), //
	BENEFITING_SERVICE_ROLE("benefitingServiceRole"), //
	;

	private String fieldName;

	private VolunteerAssignmentAssociationFieldType(String fieldName) {
		this.fieldName = fieldName;
	}

	public String getFieldName() {
		return fieldName;
	}

	@Override
	public Class<?> getModelClass() {
		return VolunteerAssignment.class;
	}

}