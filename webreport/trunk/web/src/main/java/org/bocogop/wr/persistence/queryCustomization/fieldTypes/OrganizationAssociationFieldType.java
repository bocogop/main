package org.bocogop.wr.persistence.queryCustomization.fieldTypes;

import org.bocogop.wr.model.organization.Organization;

public enum OrganizationAssociationFieldType implements ModelAssociationFieldType {
	FACILITY("facility"), //
	BRANCHES("branches"), //
	;

	private String fieldName;

	private OrganizationAssociationFieldType(String fieldName) {
		this.fieldName = fieldName;
	}

	public String getFieldName() {
		return fieldName;
	}

	@Override
	public Class<?> getModelClass() {
		return Organization.class;
	}

}