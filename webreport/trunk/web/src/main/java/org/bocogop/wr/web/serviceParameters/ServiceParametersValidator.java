package org.bocogop.wr.web.serviceParameters;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

import org.bocogop.wr.web.AbstractAppValidator;

@Component
public class ServiceParametersValidator extends AbstractAppValidator<ServiceParametersCommand> {

	@Override
	public void doExtraValidations(ServiceParametersCommand command, Errors errors) {
		// VoluntaryServiceParameters serviceParameters =
		// command.getServiceParameters();

		// for example: CPB

		// if (volunteer.getCurrentInactivationDate() != null
		// && DateTimeComparator.getInstance().compare(
		// volunteer.getCurrentActivationDate(),
		// volunteer.getCurrentInactivationDate()) > 0) {
		// errors.rejectValue("currentInactivationDate",
		// "currentInactivationDate.errors.invalidInactivationDate",
		// "The inactivation date must be later than the activation date");
		// }

		// or:

		// ValidationUtils.rejectIfEmpty(errors, field, errorCode, errorArgs,
		// defaultMessage);

		// (note that @NotBlank is Hibernate-specific API but achieves the same
		// as an annotation on the field)
	}

}
