package org.bocogop.wr.web.volunteer;

import java.time.LocalDate;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

import org.bocogop.wr.model.volunteer.Volunteer;
import org.bocogop.wr.util.DateUtil;
import org.bocogop.wr.util.ValidationUtil;
import org.bocogop.wr.util.context.SessionUtil;
import org.bocogop.wr.web.AbstractAppValidator;

@Component
public class VolunteerValidator extends AbstractAppValidator<VolunteerCommand> {

	@Override
	public void doExtraValidations(VolunteerCommand command, Errors errors) {
		Volunteer volunteer = command.getVolunteer();

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

		Integer mealsEligible = volunteer.getMealsEligible();
		if (mealsEligible != null && mealsEligible == 0 && StringUtils.isBlank(volunteer.getMealRemarks())) {
			errors.rejectValue("volunteer.mealRemarks", "volunteer.error.requiredMealRemarks");
		}

		if (command.isVolunteerTerminatedWithCause()) {
			if (command.getTerminationDate() == null)
				errors.rejectValue("terminationDate", "volunteer.error.requiredTerminationDate");
		}

		if (command.getTerminationDate() != null) {
			if (StringUtils.isBlank(command.getTerminationRemarks())) {
				errors.rejectValue("terminationRemarks", "volunteer.error.requiredTerminationRemarks");
			}
			if (command.getTerminationDate().isBefore(volunteer.getEntryDate())) {
				errors.rejectValue("terminationDate", "volunteer.error.terminationDateBeforeEntryDate");
			} else {
				LocalDate mostRecentWorkEntryDate = command.getTimeSummary().getMostRecentWorkEntryDate();
				if (mostRecentWorkEntryDate != null && !command.getTerminationDate().isAfter(mostRecentWorkEntryDate)) {
					errors.rejectValue("terminationDate",
							"volunteer.error.terminationDateBeforeMostRecentWorkEntryDate",
							new Object[] { mostRecentWorkEntryDate.format(DateUtil.DATE_ONLY_FORMAT) }, "");
				}
			}
		}

		LocalDate dateOfBirth = volunteer.getDateOfBirth();
		if (dateOfBirth != null && dateOfBirth.isAfter(LocalDate.now(SessionUtil.getFacilityContext().getTimeZone()))) {
			errors.rejectValue("volunteer.dateOfBirth", "volunteer.error.dateOfBirthNotInFuture");
		}

		LocalDate lastAwardDate = volunteer.getLastAwardDate();
		if (lastAwardDate != null && lastAwardDate.isBefore(volunteer.getEntryDate())) {
			errors.rejectValue("volunteer.lastAwardDate", "volunteer.error.lastAwardDateBeforeEntryDate");
		}
		
		String phoneNum = volunteer.getPhone();
		if(phoneNum != null && !validatePhoneNumber(phoneNum))
			errors.rejectValue("volunteer.phone", "volunteer.error.phone");
		
		String phoneAltNum = volunteer.getPhoneAlt();
		if(phoneAltNum != null && !validatePhoneNumber(phoneAltNum))
			errors.rejectValue("volunteer.phoneAlt", "volunteer.error.phone");
		
		String phoneAlt2Num = volunteer.getPhoneAlt2();
		if(phoneAlt2Num != null && !validatePhoneNumber(phoneAlt2Num))
			errors.rejectValue("volunteer.phoneAlt2", "volunteer.error.phone");
		
		String emgergencyPhone = volunteer.getEmergencyContactPhone();
		if(emgergencyPhone != null && !validatePhoneNumber(emgergencyPhone))
			errors.rejectValue("volunteer.emergencyContactPhone", "volunteer.error.phone");
		
		String emgergencyPhoneAlt = volunteer.getEmergencyContactPhoneAlt();
		if(emgergencyPhoneAlt != null && !validatePhoneNumber(emgergencyPhoneAlt))
			errors.rejectValue("volunteer.emergencyContactPhoneAlt", "volunteer.error.phone");
		
		String email = volunteer.getEmail();
		if(email != null && !isValidEmailAddress(email))
			errors.rejectValue("volunteer.email", "volunteer.error.email");
	}
	
	private static boolean validatePhoneNumber(String phoneNo) {
		
		if(phoneNo == null)
			return true;
		if(phoneNo.matches(ValidationUtil.PHONE_REGEX)) return true;
		else return false;
	}

	 public boolean isValidEmailAddress(String email) {
		 if(email == null)
			 return true;
        if(email.matches(ValidationUtil.EMAIL_REGEX)) return true;
        else return false;
  }
}
