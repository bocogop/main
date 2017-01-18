package org.bocogop.wr.web.event;

import org.bocogop.shared.model.Event;
import org.bocogop.shared.web.AbstractAppValidator;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

@Component
public class EventValidator extends AbstractAppValidator<EventCommand> {

	@Override
	public void doExtraValidations(EventCommand command, Errors errors) {
		Event event = command.getEvent();

		// for example: CPB

		// if (voter.getCurrentInactivationDate() != null
		// && DateTimeComparator.getInstance().compare(
		// voter.getCurrentActivationDate(),
		// voter.getCurrentInactivationDate()) > 0) {
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
