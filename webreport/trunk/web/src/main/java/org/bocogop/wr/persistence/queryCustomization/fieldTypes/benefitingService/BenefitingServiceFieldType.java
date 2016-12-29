package org.bocogop.wr.persistence.queryCustomization.fieldTypes.benefitingService;

import org.bocogop.wr.model.benefitingService.BenefitingService;
import org.bocogop.wr.persistence.queryCustomization.fieldTypes.ModelAssociationFieldType;

public enum BenefitingServiceFieldType implements ModelAssociationFieldType {
	FACILITY("facility"), //
	TEMPLATE("template"), //
	BENEFITING_SERVICE_ROLES("benefitingServiceRoles"), //
	VOLUNTEER_ASSIGNMENTS("volunteerAssignments"), //
	;

	private String fieldName;

	private BenefitingServiceFieldType(String fieldName) {
		this.fieldName = fieldName;
	}

	public String getFieldName() {
		return fieldName;
	}

	@Override
	public Class<?> getModelClass() {
		return BenefitingService.class;
	}

}