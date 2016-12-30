package org.bocogop.wr.web.voter;

import java.time.LocalDate;

import org.bocogop.wr.model.voter.Voter;
import org.bocogop.wr.util.ValidationUtil;
import org.bocogop.wr.web.AbstractAppValidator;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

@Component
public class VoterValidator extends AbstractAppValidator<VoterCommand> {

	@Override
	public void doExtraValidations(VoterCommand command, Errors errors) {
		Voter voter = command.getVoter();

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

		Integer birthYear = voter.getBirthYear();
		if (birthYear != null && birthYear > LocalDate.now().getYear()) {
			errors.rejectValue("voter.birthYear", "voter.error.birthYearNotInFuture");
		}

		String phoneNum = voter.getPhone();
		if (phoneNum != null && !validatePhoneNumber(phoneNum))
			errors.rejectValue("voter.phone", "voter.error.phone");

		String email = voter.getEmail();
		if (email != null && !isValidEmailAddress(email))
			errors.rejectValue("voter.email", "voter.error.email");
	}

	private static boolean validatePhoneNumber(String phoneNo) {

		if (phoneNo == null)
			return true;
		if (phoneNo.matches(ValidationUtil.PHONE_REGEX))
			return true;
		else
			return false;
	}

	public boolean isValidEmailAddress(String email) {
		if (email == null)
			return true;
		if (email.matches(ValidationUtil.EMAIL_REGEX))
			return true;
		else
			return false;
	}
}