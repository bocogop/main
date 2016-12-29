package org.bocogop.wr.web.donation;

import org.bocogop.wr.model.donation.Donor;
import org.bocogop.wr.model.organization.AbstractBasicOrganization;
import org.bocogop.wr.model.organization.ScopeType;

public class DonorCommand {

	private Donor donor;
	private String desiredIndividualType;
	private String orgFacilityDisplay;

	public DonorCommand() {
	}

	public DonorCommand(Donor donor) {
		this.donor = donor;
		this.desiredIndividualType = donor.getOrganization() != null ? "org"
				: donor.getVolunteer() != null ? "volunteer" : "individual";
	}
	
	public Donor getDonor() {
		return donor;
	}

	public void setOrganization(Donor donor) {
		this.donor = donor;
	}

	public String getDesiredIndividualType() {
		return desiredIndividualType;
	}

	public void setDesiredIndividualType(String desiredIndividualType) {
		this.desiredIndividualType = desiredIndividualType;
	}

	public void setDonor(Donor donor) {
		this.donor = donor;
	}

	public String getOrgFacilityDisplay() {
		if (this.orgFacilityDisplay == null) {
			AbstractBasicOrganization org = donor.getOrganization();
			
			// org is branch
			if (org != null && "Branch".equalsIgnoreCase(org.getScale()))  {
				this.orgFacilityDisplay = org.getRootOrganization().getScope() != ScopeType.NATIONAL? 
						org.getRootOrganization().getFacility().getDisplayName() : "NATIONAL";
			}
			else if(org != null) {// org is organization
				this.orgFacilityDisplay = org.getScope() != ScopeType.NATIONAL? org.getFacility().getDisplayName() : "NATIONAL";
			}
		}
		return this.orgFacilityDisplay;
	}
	
	public void setOrgFacilityDisplay(String orgFacilityDisplay) {
		this.orgFacilityDisplay = orgFacilityDisplay;
	}

	public String getMutillineAddressWithoutLineFeed() {
		String mutillineAddr = this.donor.getAddressMultilineDisplay();
		return mutillineAddr.replaceAll("\n", "--Newline--");
	}

}
