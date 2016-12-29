package org.bocogop.wr.web.organization;

import org.bocogop.wr.model.organization.AbstractBasicOrganization;

public class OrganizationCommand {

	private AbstractBasicOrganization organization;
	private String fromPage;

	public OrganizationCommand(AbstractBasicOrganization organization, String fromPage) {
		this.organization = organization;
		this.fromPage = fromPage;
	}

	public OrganizationCommand(AbstractBasicOrganization organization) {
		this.organization = organization;
	}

	public AbstractBasicOrganization getOrganization() {
		return organization;
	}

	public void setOrganization(AbstractBasicOrganization organization) {
		this.organization = organization;
	}

	public String getFromPage() {
		return fromPage;
	}

	public void setFromPage(String fromPage) {
		this.fromPage = fromPage;
	}
}
