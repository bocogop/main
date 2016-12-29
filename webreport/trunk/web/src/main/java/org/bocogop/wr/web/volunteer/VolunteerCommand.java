package org.bocogop.wr.web.volunteer;

import static org.bocogop.wr.model.volunteer.VolunteerStatus.VolunteerStatusType.TERMINATED_WITH_CAUSE;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import org.bocogop.wr.model.volunteer.Volunteer;
import org.bocogop.wr.model.volunteer.VolunteerStatus.VolunteerStatusType;
import org.bocogop.wr.persistence.dao.TimeSummary;
import org.bocogop.wr.util.DateUtil;

public class VolunteerCommand {

	// -------------------------------- Fields

	private Volunteer volunteer;
	private TimeSummary timeSummary;
	private String fromPage;

	@DateTimeFormat(pattern = DateUtil.TWO_DIGIT_DATE_ONLY)
	private LocalDate terminationDate;
	private String terminationRemarks;
	private boolean volunteerTerminatedWithCause;

	// -------------------------------- Constructors

	public VolunteerCommand() {
	}

	public VolunteerCommand(Volunteer volunteer, TimeSummary timeSummary, String fromPage) {
		this.volunteer = volunteer;
		this.timeSummary = timeSummary;
		this.fromPage = fromPage;

		VolunteerStatusType lookupType = volunteer.getStatus().getLookupType();
		this.terminationDate = lookupType.isTerminated() ? volunteer.getStatusDate() : null;
		this.volunteerTerminatedWithCause = (lookupType == TERMINATED_WITH_CAUSE);
		this.terminationRemarks = volunteer.getTerminationRemarks();
	}

	// -------------------------------- Business Methods

	public String getTitleStatus() {
		if (volunteerTerminatedWithCause)
			return "(TERMINATED WITH CAUSE)";
		if (terminationDate != null)
			return "(Terminated)";
		if (volunteer.getStatus().getLookupType() == VolunteerStatusType.INACTIVE)
			return "(Inactive)";
		return null;
	}

	// -------------------------------- Accessor Methods

	public Volunteer getVolunteer() {
		return volunteer;
	}

	public void setVolunteer(Volunteer volunteer) {
		this.volunteer = volunteer;
	}

	public TimeSummary getTimeSummary() {
		return timeSummary;
	}

	public void setTimeSummary(TimeSummary timeSummary) {
		this.timeSummary = timeSummary;
	}

	public boolean isVolunteerTerminatedWithCause() {
		return volunteerTerminatedWithCause;
	}

	public String getFromPage() {
		return fromPage;
	}

	public void setFromPage(String fromPage) {
		this.fromPage = fromPage;
	}

	public void setVolunteerTerminatedWithCause(boolean volunteerTerminatedWithCause) {
		this.volunteerTerminatedWithCause = volunteerTerminatedWithCause;
	}

	public LocalDate getTerminationDate() {
		return terminationDate;
	}

	public void setTerminationDate(LocalDate terminationDate) {
		this.terminationDate = terminationDate;
	}

	public String getTerminationRemarks() {
		return terminationRemarks;
	}

	public void setTerminationRemarks(String terminationRemarks) {
		this.terminationRemarks = terminationRemarks;
	}

}
