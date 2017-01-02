package org.bocogop.shared.persistence.queryCustomization.fieldTypes;

import org.bocogop.shared.model.voter.Voter;

public enum VoterAssociationFieldType implements ModelAssociationFieldType {
	STATION_PROFILES("stationProfiles"), //
	;

	private String fieldName;

	private VoterAssociationFieldType(String fieldName) {
		this.fieldName = fieldName;
	}

	public String getFieldName() {
		return fieldName;
	}

	@Override
	public Class<?> getModelClass() {
		return Voter.class;
	}

}