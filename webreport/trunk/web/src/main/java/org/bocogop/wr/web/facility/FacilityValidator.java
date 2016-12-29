package org.bocogop.wr.web.facility;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;

import org.bocogop.wr.model.facility.AbstractUpdateableLocation;
import org.bocogop.wr.model.facility.AdministrativeUnit;
import org.bocogop.wr.model.facility.Facility;
import org.bocogop.wr.web.AbstractAppValidator;

@Component
public class FacilityValidator extends AbstractAppValidator<FacilityCommand> {

	@Override
	public void doExtraValidations(FacilityCommand command, Errors errors) {
		Facility f = command.getFacility();
		ValidationUtils.rejectIfEmpty(errors, "facility.timeZone", "facility.error.requiredTimeZone");

		if (!f.isLinkedToVAFacility()) {
			ValidationUtils.rejectIfEmptyOrWhitespace(errors, "facility.name", "facility.error.requiredName");
			ValidationUtils.rejectIfEmptyOrWhitespace(errors, "facility.addressLine1",
					"facility.error.requiredAddress");
			ValidationUtils.rejectIfEmptyOrWhitespace(errors, "facility.city", "facility.error.requiredCity");
			ValidationUtils.rejectIfEmpty(errors, "facility.state", "facility.error.requiredState");
			ValidationUtils.rejectIfEmptyOrWhitespace(errors, "facility.zip", "facility.error.requiredZip");

			AbstractUpdateableLocation<?> parent = f.getParent();
			if (parent != null) {
				Facility parentFacility = parent.getFacility();
				AdministrativeUnit parentVisn = parentFacility.getAdministrativeUnit();
				if (parentVisn != null && !parentVisn.equals(f.getAdministrativeUnit()))
					errors.rejectValue("facility.administrativeUnit", "facility.error.parentVISNMismatch",
							new Object[] { parentVisn.getDisplayName() }, "Missing error message");
			}
		}

	}

}
