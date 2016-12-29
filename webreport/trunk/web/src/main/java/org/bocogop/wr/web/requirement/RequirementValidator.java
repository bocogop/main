package org.bocogop.wr.web.requirement;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;

import org.bocogop.wr.web.AbstractAppValidator;

@Component
public class RequirementValidator extends AbstractAppValidator<RequirementCommand> {

	@Override
	public void doExtraValidations(RequirementCommand command, Errors errors) {
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "name", "requirement.error.nameEmpty");
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "description", "requirement.error.descriptionEmpty");
	}

}
