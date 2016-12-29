package org.bocogop.wr.service.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.bocogop.shared.service.validation.ServiceValidationException;
import org.bocogop.wr.model.benefitingService.BenefitingServiceRole;
import org.bocogop.wr.model.benefitingService.BenefitingServiceRoleTemplate;
import org.bocogop.wr.model.benefitingService.BenefitingServiceTemplate;
import org.bocogop.wr.model.facility.AbstractUpdateableLocation;
import org.bocogop.wr.service.BenefitingServiceRoleService;
import org.bocogop.wr.service.BenefitingServiceRoleTemplateService;
import org.bocogop.wr.service.BenefitingServiceService;

@Service
public class BenefitingServiceRoleTemplateServiceImpl extends AbstractServiceImpl
		implements BenefitingServiceRoleTemplateService {
	private static final Logger log = LoggerFactory.getLogger(BenefitingServiceRoleTemplateServiceImpl.class);

	@Autowired
	private BenefitingServiceService benefitingServiceService;
	@Autowired
	private BenefitingServiceRoleService benefitingServiceRoleService;

	public BenefitingServiceRoleTemplate saveOrUpdate(BenefitingServiceRoleTemplate benefitingServiceRoleTemplate)
			throws ServiceValidationException {
		benefitingServiceRoleTemplateDAO.detach(benefitingServiceRoleTemplate);

		boolean inactivating = false;
		boolean activating = false;

		boolean persistent = benefitingServiceRoleTemplate.isPersistent();
		Long id = benefitingServiceRoleTemplate.getId();

		if (persistent) {
			BenefitingServiceRoleTemplate existingServiceRoleTemplate = benefitingServiceRoleTemplateDAO
					.findRequiredByPrimaryKey(id);
			inactivating = !existingServiceRoleTemplate.isInactive() && benefitingServiceRoleTemplate.isInactive();
			activating = existingServiceRoleTemplate.isInactive() && !benefitingServiceRoleTemplate.isInactive();
		}

		benefitingServiceRoleTemplate.setName(StringUtils.trim(benefitingServiceRoleTemplate.getName()));

		benefitingServiceRoleTemplate = benefitingServiceRoleTemplateDAO.saveOrUpdate(benefitingServiceRoleTemplate);

		if (inactivating) {
			cascadeInactivation(id);
		} else if (activating) {
			ensureParentServiceTemplateActive(benefitingServiceRoleTemplate);
		}

		if (persistent) {
			benefitingServiceRoleDAO.bulkUpdateByCriteria(null, id, null, null, null,
					benefitingServiceRoleTemplate.getName(), null, benefitingServiceRoleTemplate.getRoleType());
		}

		benefitingServiceRoleTemplateDAO.flushAndRefresh(benefitingServiceRoleTemplate);

		return benefitingServiceRoleTemplate;
	}

	public void ensureParentServiceTemplateActive(BenefitingServiceRoleTemplate benefitingServiceRoleTemplate)
			throws ServiceValidationException {
		BenefitingServiceTemplate benefitingServiceTemplate = benefitingServiceRoleTemplate
				.getBenefitingServiceTemplate();
		if (benefitingServiceTemplate.isInactive())
			throw new ServiceValidationException("benefitingServiceRoleTemplate.error.parentInactive",
					new Serializable[] { benefitingServiceTemplate.getDisplayName() });
	}

	public void deleteOrInactivateBenefitingServiceRoleTemplate(long benefitingServiceRoleTemplateId)
			throws ServiceValidationException {
		if (canBeDeleted(benefitingServiceRoleTemplateId)) {
			deleteBenefitingServiceRoleTemplateInternal(benefitingServiceRoleTemplateId, false);
		} else {
			inactivateBenefitingServiceRoleTemplate(benefitingServiceRoleTemplateId);
		}
	}

	public void deleteBenefitingServiceRoleTemplate(long benefitingServiceRoleTemplateId) {
		deleteBenefitingServiceRoleTemplateInternal(benefitingServiceRoleTemplateId, true);
	}

	private void deleteBenefitingServiceRoleTemplateInternal(long benefitingServiceRoleTemplateId, boolean check) {
		if (!check || canBeDeleted(benefitingServiceRoleTemplateId)) {
			volunteerAssignmentDAO.bulkDeleteByCriteria(null, benefitingServiceRoleTemplateId, null, null);
			benefitingServiceRoleDAO.bulkDeleteByCriteria(null, benefitingServiceRoleTemplateId, null);
			benefitingServiceRoleTemplateRequirementAssociationDAO.bulkDeleteByCriteria(null,
					benefitingServiceRoleTemplateId, null);
			benefitingServiceRoleTemplateDAO.delete(benefitingServiceRoleTemplateId);
		}
	}

	public void inactivateBenefitingServiceRoleTemplate(long benefitingServiceRoleTemplateId)
			throws ServiceValidationException {
		BenefitingServiceRoleTemplate benefitingServiceRoleTemplate = benefitingServiceRoleTemplateDAO
				.findRequiredByPrimaryKey(benefitingServiceRoleTemplateId);
		if (benefitingServiceRoleTemplate.isRequiredAndReadOnly())
			throw new ServiceValidationException("benefitingServiceRoleTemplate.error.inactivateRequiredRole",
					benefitingServiceRoleTemplate.getBenefitingServiceTemplate().getDisplayName());

		benefitingServiceRoleTemplate.setInactive(true);
		benefitingServiceRoleTemplate = benefitingServiceRoleTemplateDAO.saveOrUpdate(benefitingServiceRoleTemplate);

		cascadeInactivation(benefitingServiceRoleTemplateId);
	}

	public boolean canBeDeleted(long benefitingServiceRoleTemplateId) {
		return !workEntryDAO.existsForCriteria(null, benefitingServiceRoleTemplateId, null, null)
				&& !occasionalWorkEntryDAO.existsForCriteria(null, benefitingServiceRoleTemplateId, null, null);
	}

	public void cascadeInactivation(long benefitingServiceRoleTemplateId) {
		volunteerAssignmentDAO.bulkInactivateByCriteria(null, benefitingServiceRoleTemplateId, null, null, null);
		benefitingServiceRoleDAO.bulkUpdateByCriteria(null, benefitingServiceRoleTemplateId, null, null, null, null,
				false, null);
	}

	@Override
	public MergeErrorReport merge(long fromBenefitingServiceRoleTemplateId, long toBenefitingServiceRoleTemplateId)
			throws ServiceValidationException {
		BenefitingServiceRoleTemplate fromRoleTemplate = benefitingServiceRoleTemplateDAO
				.findRequiredByPrimaryKey(fromBenefitingServiceRoleTemplateId);
		BenefitingServiceTemplate fromServiceTemplate = fromRoleTemplate.getBenefitingServiceTemplate();

		if (fromRoleTemplate.isRequiredAndReadOnly()
				&& fromServiceTemplate.getServiceRoleTemplates().stream().anyMatch(p -> !p.equals(fromRoleTemplate)))
			throw new ServiceValidationException("benefitingServiceRoleTemplate.error.mergingRequiredRoleButOthersPresent");

		BenefitingServiceRoleTemplate toRoleTemplate = benefitingServiceRoleTemplateDAO
				.findRequiredByPrimaryKey(toBenefitingServiceRoleTemplateId);
		BenefitingServiceTemplate toServiceTemplate = toRoleTemplate.getBenefitingServiceTemplate();

		Map<Long, Map<Long, BenefitingServiceRole>> fromRolesByLocationByFacilityMap = buildClaimedFacilitiesAndLocationsMap(
				fromBenefitingServiceRoleTemplateId);
		Map<Long, Map<Long, BenefitingServiceRole>> toRolesByLocationByFacilityMap = buildClaimedFacilitiesAndLocationsMap(
				toBenefitingServiceRoleTemplateId);

		MergeErrorReport mer = new MergeErrorReport();

		/* For all the facilities that already have claimed our "from" role */
		for (Long fromFacilityId : fromRolesByLocationByFacilityMap.keySet()) {
			/*
			 * Build a map of their physical locations (or -1 for the main
			 * facility) and the claimed role
			 */
			Map<Long, BenefitingServiceRole> fromRolesByLocationMap = fromRolesByLocationByFacilityMap
					.get(fromFacilityId);
			/*
			 * Build a map of physical locations where the destination role
			 * already exists (including -1 at the main facility)
			 */
			Map<Long, BenefitingServiceRole> toRolesByLocationMap = toRolesByLocationByFacilityMap.get(fromFacilityId);
			/*
			 * If the destination role doesn't exist anywhere at our to site
			 * yet, make this map empty
			 */
			if (toRolesByLocationMap == null)
				toRolesByLocationMap = new HashMap<>();

			/*
			 * We can't merge the role at the main facility (and subsequently
			 * unclaim / remove the service) until all other locations have been
			 * moved first - so we enforce that ordering here - CPB
			 */
			List<Long> orderedLocations = new ArrayList<>(fromRolesByLocationMap.keySet());
			if (orderedLocations.contains(-1L)) {
				orderedLocations.remove(-1L);
				orderedLocations.add(-1L);
			}

			for (Long fromLocationId : orderedLocations) {
				BenefitingServiceRole fromRole = fromRolesByLocationMap.get(fromLocationId);
				BenefitingServiceRole toRoleTest = toRolesByLocationMap.get(fromLocationId);
				try {
					/*
					 * For each facility/location that's claimed the FROM role,
					 * see if it also claimed the TO role - if not, claim it for
					 * them automatically here
					 */
					if (toRoleTest == null) {
						Map<Long, List<BenefitingServiceRole>> newItem = benefitingServiceService
								.linkBenefitingServicesAndRoles(fromFacilityId, Arrays.asList(fromLocationId),
										Arrays.asList(toServiceTemplate.getId()),
										Arrays.asList(toRoleTemplate.getId()));
						toRoleTest = newItem.values().stream().flatMap(List::stream)
								.filter(p -> p.getTemplate().equals(toRoleTemplate)).findFirst().orElse(null);
					}

					/* Perform the merge*/
					benefitingServiceRoleService.merge(fromRole.getId(), toRoleTest.getId(), false, true);
				} catch (Exception e) {
					AbstractUpdateableLocation<?> l = (fromLocationId == -1)
							? facilityDAO.findRequiredByPrimaryKey(fromFacilityId)
							: locationDAO.findRequiredByPrimaryKey(fromLocationId);
					mer.getLocationMergeErrors().put(l, e);
				}
			}
		}

		if (mer.getLocationMergeErrors().isEmpty()) {
			try {
				benefitingServiceRoleDAO.bulkDeleteByCriteria(null, fromBenefitingServiceRoleTemplateId, null);

				BenefitingServiceTemplate fromTemplate = fromRoleTemplate.getBenefitingServiceTemplate();
				benefitingServiceTemplateDAO.refresh(fromTemplate);
				int numRoleTemplates = fromTemplate.getServiceRoleTemplates().size();
				int numBenefitingServices = fromTemplate.getBenefitingServices().size();
				benefitingServiceRoleTemplateRequirementAssociationDAO.bulkDeleteByCriteria(null, fromBenefitingServiceRoleTemplateId, null);
				benefitingServiceRoleTemplateDAO.delete(fromBenefitingServiceRoleTemplateId);
				if (numRoleTemplates == 1 && numBenefitingServices == 0) {
					benefitingServiceTemplateDAO.delete(fromTemplate);
				}
				benefitingServiceRoleTemplateDAO.flush();
			} catch (Exception e) {
				mer.setExecutionException(e);
			}
		}

		return mer;
	}

	private Map<Long, Map<Long, BenefitingServiceRole>> buildClaimedFacilitiesAndLocationsMap(
			long benefitingServiceRoleTemplateId) {
		BenefitingServiceRoleTemplate t = benefitingServiceRoleTemplateDAO
				.findRequiredByPrimaryKey(benefitingServiceRoleTemplateId);
		List<BenefitingServiceRole> benefitingServiceRoles = t.getBenefitingServiceRoles();
		if (benefitingServiceRoles.isEmpty())
			return new HashMap<>();

		Map<Long, Map<Long, BenefitingServiceRole>> rolesByLocationByFacilityMap = new HashMap<>();
		for (BenefitingServiceRole r : benefitingServiceRoles) {
			long rootFacilityId = r.getFacility().getRootFacilityId();
			Map<Long, BenefitingServiceRole> rolesByLocationId = rolesByLocationByFacilityMap.get(rootFacilityId);
			if (rolesByLocationId == null) {
				rolesByLocationId = new HashMap<>();
				rolesByLocationByFacilityMap.put(rootFacilityId, rolesByLocationId);
			}
			rolesByLocationId.put(r.getLocationId(), r);
		}
		return rolesByLocationByFacilityMap;
	}

	@Override
	public void reactivate(long benefitingServiceRoleTemplateId) throws ServiceValidationException {
		BenefitingServiceRoleTemplate existingServiceRoleTemplate = benefitingServiceRoleTemplateDAO
				.findRequiredByPrimaryKey(benefitingServiceRoleTemplateId);
		boolean wasInactive = existingServiceRoleTemplate.isInactive();

		ensureParentServiceTemplateActive(existingServiceRoleTemplate);

		if (wasInactive) {
			existingServiceRoleTemplate.setInactive(false);
			existingServiceRoleTemplate = benefitingServiceRoleTemplateDAO.saveOrUpdate(existingServiceRoleTemplate);
		}
	}

}
