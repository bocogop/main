package org.bocogop.wr.web.precinct;

import org.bocogop.shared.model.precinct.Precinct;
import org.bocogop.shared.web.AbstractAppValidator;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;

@Component
public class PrecinctValidator extends AbstractAppValidator<PrecinctCommand> {

	@Override
	public void doExtraValidations(PrecinctCommand command, Errors errors) {
		Precinct f = command.getPrecinct();
		ValidationUtils.rejectIfEmpty(errors, "precinct.timeZone", "precinct.error.requiredTimeZone");

		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "precinct.name", "precinct.error.requiredName");
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "precinct.addressLine1", "precinct.error.requiredAddress");
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "precinct.city", "precinct.error.requiredCity");
		ValidationUtils.rejectIfEmpty(errors, "precinct.state", "precinct.error.requiredState");
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "precinct.zip", "precinct.error.requiredZip");

	}

}
