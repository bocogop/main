package org.bocogop.wr.web.organization;

import java.util.List;

import org.bocogop.wr.model.organization.AbstractBasicOrganization;

public class OrganizationListCommand {

	List<AbstractBasicOrganization> organizations;

	public OrganizationListCommand(List<AbstractBasicOrganization> organizations) {
		this.organizations = organizations;
	}

	public List<AbstractBasicOrganization> getOrganizations() {
		return organizations;
	}

	public void setOrganizations(List<AbstractBasicOrganization> organizations) {
		this.organizations = organizations;
	}
}
