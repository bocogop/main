package org.bocogop.wr.service.impl;

import java.io.Serializable;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang.WordUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import org.bocogop.shared.model.Permission;
import org.bocogop.shared.service.validation.ServiceValidationException;
import org.bocogop.wr.model.benefitingService.BenefitingService;
import org.bocogop.wr.model.benefitingService.BenefitingServiceRole;
import org.bocogop.wr.model.benefitingService.BenefitingServiceRoleTemplate;
import org.bocogop.wr.model.benefitingService.BenefitingServiceRoleType;
import org.bocogop.wr.model.benefitingService.ScopeType;
import org.bocogop.wr.model.facility.AbstractUpdateableLocation;
import org.bocogop.wr.service.BenefitingServiceRoleService;
import org.bocogop.wr.service.BenefitingServiceService;

@Service
public class BenefitingServiceRoleServiceImpl extends AbstractServiceImpl implements BenefitingServiceRoleService {
	private static final Logger log = LoggerFactory.getLogger(BenefitingServiceRoleServiceImpl.class);

	@Autowired
	private BenefitingServiceService benefitingServiceService;

	@PreAuthorize("hasAuthority('" + Permission.BENEFITING_SERVICE_CREATE + "')")
	public void saveOrUpdateAtLocations(List<Long> locationIds, Long benefitingServiceRoleId, Long benefitingServiceId,
			String name, String description, String contactName, String contactEmail, String contactPhone,
			BenefitingServiceRoleType roleType, boolean isEdit) throws ServiceValidationException {
		BenefitingService benefitingService = null;

		if (isEdit) {
			BenefitingServiceRole role = benefitingServiceRoleDAO.findRequiredByPrimaryKey(benefitingServiceRoleId);
			benefitingService = role.getBenefitingService();

			long locationId = locationIds.get(0);
			AbstractUpdateableLocation<?> location = (locationId == -1) ? benefitingService.getFacility()
					: locationDAO.findRequiredByPrimaryKey(locationId);
			role.setFacility(location);
			setInternalRoleFields(role, name, description, contactName, contactEmail, contactPhone, roleType,
					isEdit ? null : benefitingService.isInactive(), isEdit ? null : ScopeType.LOCAL);

			role = saveOrUpdate(role);
		} else {
			for (Long locationId : locationIds) {
				BenefitingServiceRole role = new BenefitingServiceRole();
				benefitingService = benefitingServiceDAO.findRequiredByPrimaryKey(benefitingServiceId);
				role.setBenefitingService(benefitingService);

				AbstractUpdateableLocation<?> location = (locationId == -1) ? benefitingService.getFacility()
						: locationDAO.findRequiredByPrimaryKey(locationId);
				role.setFacility(location);
				setInternalRoleFields(role, name, description, contactName, contactEmail, contactPhone, roleType,
						isEdit ? null : benefitingService.isInactive(), isEdit ? null : ScopeType.LOCAL);

				role = saveOrUpdate(role);
			}
		}
	}

	public static void setInternalRoleFields(BenefitingServiceRole role, String name, String description,
			String contactName, String contactEmail, String contactPhone, BenefitingServiceRoleType roleType,
			Boolean inactive, ScopeType scopeType) {
		role.setName(name);
		role.setDescription(description);
		role.setContactName(contactName);
		role.setContactEmail(contactEmail);
		role.setContactPhone(contactPhone);
		role.setRoleType(roleType);
		if (inactive != null)
			role.setInactive(inactive);
		if (scopeType != null)
			role.setScope(scopeType);
	}

	@PreAuthorize("hasAuthority('" + Permission.BENEFITING_SERVICE_CREATE + "')")
	public BenefitingServiceRole saveOrUpdate(BenefitingServiceRole benefitingServiceRole)
			throws ServiceValidationException {
		benefitingServiceRoleDAO.detach(benefitingServiceRole);

		BenefitingService existingBenefitingService = benefitingServiceDAO
				.findRequiredByPrimaryKey(benefitingServiceRole.getBenefitingService().getId());
		SortedSet<BenefitingServiceRole> existingSiblingRoles = existingBenefitingService.getBenefitingServiceRoles();
		for (BenefitingServiceRole sibling : existingSiblingRoles) {
			if (benefitingServiceRole.isPersistent() && sibling.getId().equals(benefitingServiceRole.getId()))
				continue;
			if (!benefitingServiceRole.getFacility().equals(sibling.getFacility()))
				continue;

			if (StringUtils.trimToEmpty(benefitingServiceRole.getName())
					.equalsIgnoreCase(StringUtils.trimToEmpty(sibling.getName())))
				throw new ServiceValidationException("benefitingServiceRole.error.duplicateNameDetected");
		}

		benefitingServiceRole.setContactName(WordUtils.capitalizeFully(benefitingServiceRole.getContactName()));

		boolean inactivating = false;
		boolean activating = false;

		if (benefitingServiceRole.isPersistent()) {
			BenefitingServiceRole existingServiceRole = benefitingServiceRoleDAO
					.findRequiredByPrimaryKey(benefitingServiceRole.getId());
			inactivating = !existingServiceRole.isInactive() && benefitingServiceRole.isInactive();
			activating = existingServiceRole.isInactive() && !benefitingServiceRole.isInactive();
		}

		benefitingServiceRole.setName(StringUtils.trim(benefitingServiceRole.getName()));

		benefitingServiceRole = benefitingServiceRoleDAO.saveOrUpdate(benefitingServiceRole);

		if (inactivating) {
			cascadeInactivation(benefitingServiceRole.getId());
		} else if (activating) {
			ensureParentServiceAndTemplateAndLocationActive(benefitingServiceRole);
			// nothing to cascade here
		}

		benefitingServiceRoleDAO.flushAndRefresh(benefitingServiceRole);

		return benefitingServiceRole;
	}

	public void ensureParentServiceAndTemplateAndLocationActive(BenefitingServiceRole benefitingServiceRole)
			throws ServiceValidationException {
		BenefitingService benefitingService = benefitingServiceRole.getBenefitingService();
		if (benefitingService.isInactive())
			throw new ServiceValidationException("benefitingServiceRole.error.parentInactive",
					new Serializable[] { benefitingService.getDisplayName() });

		BenefitingServiceRoleTemplate template = benefitingServiceRole.getTemplate();
		if (template != null && template.isInactive())
			throw new ServiceValidationException("benefitingServiceRole.error.templateInactive");

		AbstractUpdateableLocation<?> l = benefitingServiceRole.getFacility();
		if (l.isInactive())
			throw new ServiceValidationException("benefitingServiceRole.error.facilityOrLocationInactive",
					new Serializable[] { l.getDisplayName() });
	}

	@PreAuthorize("hasAuthority('" + Permission.BENEFITING_SERVICE_CREATE + "')")
	public void deleteOrInactivateBenefitingServiceRole(long benefitingServiceRoleId)
			throws ServiceValidationException {
		if (canBeDeleted(benefitingServiceRoleId)) {
			deleteBenefitingServiceRoleInternal(benefitingServiceRoleId, false);
		} else {
			inactivateBenefitingServiceRole(benefitingServiceRoleId);
		}
	}

	@PreAuthorize("hasAuthority('" + Permission.BENEFITING_SERVICE_CREATE + "')")
	public void deleteBenefitingServiceRole(long benefitingServiceRoleId) {
		deleteBenefitingServiceRoleInternal(benefitingServiceRoleId, true);
	}

	private void deleteBenefitingServiceRoleInternal(long benefitingServiceRoleId, boolean check) {
		BenefitingServiceRole bsr = benefitingServiceRoleDAO.findRequiredByPrimaryKey(benefitingServiceRoleId);
		if (bsr.isRequiredAndReadOnly()) {
			benefitingServiceService.deleteBenefitingService(bsr.getBenefitingService().getId());
			return;
		}

		if (!check || canBeDeleted(benefitingServiceRoleId)) {
			volunteerAssignmentDAO.bulkDeleteByCriteria(null, null, null, benefitingServiceRoleId);
			benefitingServiceRoleRequirementAssociationDAO.bulkDeleteByCriteria(null, benefitingServiceRoleId, null,
					null, null);
			benefitingServiceRoleDAO.delete(benefitingServiceRoleId);
		}
	}

	@PreAuthorize("hasAuthority('" + Permission.BENEFITING_SERVICE_CREATE + "')")
	public void inactivateBenefitingServiceRole(long benefitingServiceRoleId) throws ServiceValidationException {
		BenefitingServiceRole benefitingServiceRole = benefitingServiceRoleDAO
				.findRequiredByPrimaryKey(benefitingServiceRoleId);
		if (benefitingServiceRole.isRequiredAndReadOnly())
			throw new ServiceValidationException("benefitingServiceRole.error.inactivateRequiredRole",
					benefitingServiceRole.getBenefitingService().getDisplayName());

		benefitingServiceRole.setInactive(true);
		benefitingServiceRole = benefitingServiceRoleDAO.saveOrUpdate(benefitingServiceRole);

		cascadeInactivation(benefitingServiceRoleId);
	}

	public boolean canBeDeleted(long benefitingServiceRoleId) {
		return !workEntryDAO.existsForCriteria(null, null, null, benefitingServiceRoleId)
				&& !occasionalWorkEntryDAO.existsForCriteria(null, null, null, benefitingServiceRoleId);
	}

	@PreAuthorize("hasAuthority('" + Permission.BENEFITING_SERVICE_CREATE + "')")
	public void cascadeInactivation(long benefitingServiceRoleId) {
		volunteerAssignmentDAO.bulkInactivateByCriteria(null, null, null, benefitingServiceRoleId, null);
	}

	@Override
	@PreAuthorize("hasAuthority('" + Permission.BENEFITING_SERVICE_CREATE + "')")
	public void merge(long fromBenefitingServiceRoleId, long toBenefitingServiceRoleId,
			boolean throwExceptionOnMergeFailure, boolean moveLocalSiblingsIfNecessary)
			throws ServiceValidationException {
		BenefitingServiceRole fromBsr = benefitingServiceRoleDAO.findRequiredByPrimaryKey(fromBenefitingServiceRoleId);
		BenefitingService fromBs = fromBsr.getBenefitingService();

		BenefitingServiceRole toBsr = benefitingServiceRoleDAO.findRequiredByPrimaryKey(toBenefitingServiceRoleId);
		BenefitingService toBs = toBsr.getBenefitingService();

		/*
		 * We update volunteer assignments here which use the
		 * fromBenefitingServiceRole and set them to use the
		 * toBenefitingServiceRole, unless there's already a volunteer
		 * assignment using the toBenefitingServiceRole. If so, we don't update
		 * the current one, but just move hours attached to it over to the new
		 * one (via workEntryDAO.bulkChangeFor...) and then we delete the
		 * current one (via volunteerAssignmentDAO.bulkDeleteDuplicates...) -
		 * CPB
		 */
		int volAssnsUpdated = volunteerAssignmentDAO
				.bulkChangeForBenefitingServiceRoleMerge(fromBenefitingServiceRoleId, toBenefitingServiceRoleId);
		workEntryDAO.bulkChangeForBenefitingServiceRoleMerge(fromBenefitingServiceRoleId, toBenefitingServiceRoleId);
		volunteerAssignmentDAO.bulkDeleteDuplicatesAfterChange(fromBenefitingServiceRoleId, toBenefitingServiceRoleId);

		occasionalWorkEntryDAO.bulkMove(fromBenefitingServiceRoleId, toBenefitingServiceRoleId);
		benefitingServiceRoleRequirementAssociationDAO.bulkDeleteByCriteria(null, fromBenefitingServiceRoleId, null,
				null, null);
		benefitingServiceRoleDAO.flush();

		try {
			if (!fromBsr.isRequiredAndReadOnly()) {
				benefitingServiceRoleDAO.delete(fromBsr);
			} else {
				Stream<BenefitingServiceRole> otherRoles = fromBs.getBenefitingServiceRoles().stream()
						.filter(p -> !p.equals(fromBsr));
				if (moveLocalSiblingsIfNecessary)
					otherRoles = otherRoles.filter(p -> p.isNational());

				if (otherRoles.findAny().isPresent()) {
					/* Stop */
					throw new ServiceValidationException(
							"benefitingServiceRole.error.mergingRequiredRoleButOthersPresent");
				}

				benefitingServiceDAO.flushAndRefresh(fromBs);

				if (moveLocalSiblingsIfNecessary) {

					List<BenefitingServiceRole> targetRoles = fromBs.getBenefitingServiceRoles().stream()
							.filter(p -> !p.equals(fromBsr) && !p.isNational()).collect(Collectors.toList());
					targetRoles.forEach(localRole -> {
						Set<String> existingNamesAtTargetLocation = toBs.getBenefitingServiceRoles().stream()
								.filter(q -> q.getFacility().equals(fromBs.getFacility())).map(q -> q.getName())
								.collect(Collectors.toSet());
						fromBs.getBenefitingServiceRoles().remove(localRole);
						localRole.setBenefitingService(toBs);

						String baseName = localRole.getName() + "-merged";
						String name = baseName;
						for (int i = 1; existingNamesAtTargetLocation.contains(name); i++)
							name = baseName + i;

						localRole.setName(name);
						localRole = benefitingServiceRoleDAO.saveOrUpdate(localRole);

						volunteerAssignmentDAO.bulkUpdateBenefitingServiceForRoleMove(localRole.getId());
						occasionalWorkEntryDAO.bulkUpdateBenefitingServiceForRoleMove(localRole.getId());
					});
				}

				benefitingServiceService.deleteBenefitingService(fromBs.getId());
			}
		} catch (Exception e) {
			if (throwExceptionOnMergeFailure)
				throw e;
		}
	}

	@Override
	@PreAuthorize("hasAuthority('" + Permission.BENEFITING_SERVICE_CREATE + "')")
	public void reactivate(long benefitingServiceRoleId) throws ServiceValidationException {
		BenefitingServiceRole existingServiceRole = benefitingServiceRoleDAO
				.findRequiredByPrimaryKey(benefitingServiceRoleId);
		boolean wasInactive = existingServiceRole.isInactive();

		ensureParentServiceAndTemplateAndLocationActive(existingServiceRole);

		if (wasInactive) {
			existingServiceRole.setInactive(false);
			existingServiceRole = benefitingServiceRoleDAO.saveOrUpdate(existingServiceRole);
		}
	}

}
