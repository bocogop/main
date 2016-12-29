package org.bocogop.wr.web.donation;

import java.time.LocalDate;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

import org.bocogop.wr.model.donation.DonationSummary;
import org.bocogop.wr.util.context.SessionUtil;
import org.bocogop.wr.web.AbstractAppValidator;

@Component
public class DonationValidator extends AbstractAppValidator<DonationCommand> {

	@Override
	public void doExtraValidations(DonationCommand command, Errors errors) {
		DonationSummary donationSummary = command.getDonationSummary();

		// for example: CPB

		// No need to check null for donation date since it has been set to
		// default if it is null in database
		if (command.getCurrentFiscalYearStartDate().isAfter(command.getDonationSummary().getDonationDate())
				|| command.getCurrentFiscalYearEndDate().isBefore(command.getDonationSummary().getDonationDate())) {
			errors.rejectValue("donationSummary.donationDate", "donationSummary.error.donationDateNotWhthinFiscalYear");

		}

		if (donationSummary.getDonationDate().isAfter(LocalDate.now(SessionUtil.getFacilityContext().getTimeZone())))
			errors.rejectValue("donationSummary.donationDate", "donationSummary.error.donationDate");

		if (donationSummary.getAcknowledgementDate() != null
				&& donationSummary.getDonationDate().isAfter(donationSummary.getAcknowledgementDate()))
			errors.rejectValue("donationSummary.acknowledgementDate", "donationSummary.error.acknowledgementDate");

		if (donationSummary.getDonReference() != null && donationSummary.getDonReference().isInactive()
				&& (command.getPreviousDonRef() == null || !donationSummary.getDonReference().getDonationReference()
						.equalsIgnoreCase(command.getPreviousDonRef().getDonationReference())))
			errors.rejectValue("donationSummary.donReference", "donationSummary.error.donReference");
	}

}
