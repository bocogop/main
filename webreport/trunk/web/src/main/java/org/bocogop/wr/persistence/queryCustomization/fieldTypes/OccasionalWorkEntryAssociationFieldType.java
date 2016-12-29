package org.bocogop.wr.persistence.queryCustomization.fieldTypes;

import org.bocogop.wr.model.time.OccasionalWorkEntry;

public enum OccasionalWorkEntryAssociationFieldType implements ModelAssociationFieldType {
	ORGANIZATION("organization"), //
	BENEFITING_SERVICE("benefitingService"), //
	BENEFITING_SERVICE_ROLE("benefitingServiceRole"), //
	FACILITY("facility"), //
	;

	private String fieldName;

	private OccasionalWorkEntryAssociationFieldType(String fieldName) {
		this.fieldName = fieldName;
	}

	public String getFieldName() {
		return fieldName;
	}

	@Override
	public Class<?> getModelClass() {
		return OccasionalWorkEntry.class;
	}

}