package org.bocogop.wr.persistence.queryCustomization.fieldTypes;

import org.bocogop.wr.model.volunteer.Volunteer;

public enum VolunteerAssociationFieldType implements ModelAssociationFieldType {
	STATION_PROFILES("stationProfiles"), //
	;

	private String fieldName;

	private VolunteerAssociationFieldType(String fieldName) {
		this.fieldName = fieldName;
	}

	public String getFieldName() {
		return fieldName;
	}

	@Override
	public Class<?> getModelClass() {
		return Volunteer.class;
	}

}