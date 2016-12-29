package org.bocogop.wr.service.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import org.bocogop.shared.model.Permission.PermissionType;
import org.bocogop.shared.service.validation.ServiceValidationException;
import org.bocogop.shared.util.PersistenceUtil;
import org.bocogop.shared.util.SecurityUtil;
import org.bocogop.wr.model.organization.AbstractBasicOrganization;
import org.bocogop.wr.model.organization.Organization;
import org.bocogop.wr.model.organization.OrganizationBranch;
import org.bocogop.wr.model.organization.ScopeType;
import org.bocogop.wr.service.OrganizationService;

@Service
public class OrganizationServiceImpl extends AbstractServiceImpl implements OrganizationService {
	private static final Logger log = LoggerFactory.getLogger(OrganizationServiceImpl.class);

	@Override
	public AbstractBasicOrganization saveOrUpdate(AbstractBasicOrganization o, boolean previousStatus, boolean isEdit)
			throws ServiceValidationException {
		long vaFacilityId = getRequiredSiteContext().getId();

		boolean isOrgNotBranch = "Organization".equals(o.getScale());
		boolean isBranchNotOrg = "Branch".equals(o.getScale());

		boolean isInactivating = o.isInactive() && previousStatus;

		/* Business-level validations */
		if (o.getScope() == ScopeType.LOCAL
				&& !SecurityUtil.hasAllPermissionsAtFacility(vaFacilityId, PermissionType.ORG_CODE_LOCAL_CREATE))
			throw new ServiceValidationException("scope", "organization.saveOrUpdate.error", new Serializable[] {});
		if (o.getScope() == ScopeType.NATIONAL
				&& !SecurityUtil.hasAllPermissionsAtFacility(vaFacilityId, PermissionType.ORG_CODE_NATIONAL_CREATE))
			throw new ServiceValidationException("scope", "organization.saveOrUpdate.error", new Serializable[] {});

		o.setName(StringUtils.trim(o.getName()));

		o = organizationDAO.saveOrUpdate(o);

		if (isBranchNotOrg) {
			OrganizationBranch branch = (OrganizationBranch) o;

			// duplicate check for creating local Branch
			List<OrganizationBranch> branchMatches = organizationDAO.getLocalBranchesForOrgId(
					branch.getRootOrganization().getId(), branch.getFacility().getId(), branch.getName());
			branchMatches.remove(branch);

			if (!branchMatches.isEmpty()) {
				throw new ServiceValidationException("organizationBranch.create.error.duplicate");
			}
		}

		if (isInactivating) {
			List<AbstractBasicOrganization> orgsBeingInactivated = new ArrayList<>();
			orgsBeingInactivated.add(o);
			if (isOrgNotBranch) {
				Organization org = (Organization) o;

				for (OrganizationBranch b : org.getBranches()) {
					b.setInactive(true);
					b = (OrganizationBranch) organizationDAO.saveOrUpdate(b);
				}

				orgsBeingInactivated.addAll(org.getBranches());
			}

			List<Long> orgIds = PersistenceUtil.translateObjectsToIds(orgsBeingInactivated);
			volunteerOrganizationDAO.bulkUpdateByCriteria(orgIds, true, false);
			volunteerOrganizationDAO.bulkUpdatePrimaryOrganizationsByCriteria(orgIds, true, null);
		}

		return o;
	}

	@Override
	public void delete(long organizationId) {
		organizationDAO.delete(organizationId);
	}

}
