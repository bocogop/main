package org.bocogop.wr.web.serviceParameters;

import java.util.List;

import org.bocogop.wr.model.voluntaryService.VoluntaryServiceParameters;
import org.bocogop.wr.model.voluntaryService.VoluntaryServiceStaff;

public class ServiceParametersCommand {

	// -------------------------------- Fields

	private VoluntaryServiceParameters serviceParameters;
	List<VoluntaryServiceStaff> voluntaryServiceStaffs;
	private boolean changeLastReviewedDate = false;
	private VoluntaryServiceStaff voluntaryServiceStaff;

	// -------------------------------- Constructors

	public ServiceParametersCommand() {
	}

	public ServiceParametersCommand(VoluntaryServiceParameters serviceParameters) {
		this.serviceParameters = serviceParameters;
	}

	// -------------------------------- Accessor Methods

	public VoluntaryServiceParameters getServiceParameters() {
		return serviceParameters;
	}

	public void setServiceParameters(VoluntaryServiceParameters serviceParameters) {
		this.serviceParameters = serviceParameters;
	}

	public boolean isChangeLastReviewedDate() {
		return changeLastReviewedDate;
	}

	public void setChangeLastReviewedDate(boolean changeLastReviewedDate) {
		this.changeLastReviewedDate = changeLastReviewedDate;
	}

	public List<VoluntaryServiceStaff> getVoluntaryServiceStaffs() {
		return voluntaryServiceStaffs;
	}

	public void setVoluntaryServiceStaffs(List<VoluntaryServiceStaff> voluntaryServiceStaffs) {
		this.voluntaryServiceStaffs = voluntaryServiceStaffs;
	}

	public VoluntaryServiceStaff getVoluntaryServiceStaff() {
		return voluntaryServiceStaff;
	}

	public void setVoluntaryServiceStaff(VoluntaryServiceStaff voluntaryServiceStaff) {
		this.voluntaryServiceStaff = voluntaryServiceStaff;
	}
	
	

}
