package org.bocogop.wr.persistence.queryCustomization.fieldTypes.benefitingService;

import org.bocogop.wr.model.benefitingService.BenefitingServiceRole;
import org.bocogop.wr.persistence.queryCustomization.fieldTypes.ModelAssociationFieldType;

public enum BenefitingServiceRoleFieldType implements ModelAssociationFieldType {
	BENEFITING_SERVICE("benefitingService"), //
	FACILITY("facility"), //
	TEMPLATE("template");

	private String fieldName;

	private BenefitingServiceRoleFieldType(String fieldName) {
		this.fieldName = fieldName;
	}

	public String getFieldName() {
		return fieldName;
	}

	@Override
	public Class<?> getModelClass() {
		return BenefitingServiceRole.class;
	}

}