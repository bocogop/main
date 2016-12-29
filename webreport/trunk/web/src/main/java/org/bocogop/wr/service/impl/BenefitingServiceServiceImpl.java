package org.bocogop.wr.service.impl;

import static org.bocogop.wr.model.benefitingService.BenefitingServiceRoleType.BenefitingServiceRoleTypeValue.GENERAL;
import static java.util.Arrays.asList;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import org.bocogop.shared.service.validation.ServiceValidationException;
import org.bocogop.shared.util.PersistenceUtil;
import org.bocogop.wr.model.benefitingService.BenefitingService;
import org.bocogop.wr.model.benefitingService.BenefitingServiceRole;
import org.bocogop.wr.model.benefitingService.BenefitingServiceRoleTemplate;
import org.bocogop.wr.model.benefitingService.BenefitingServiceTemplate;
import org.bocogop.wr.model.benefitingService.ScopeType;
import org.bocogop.wr.model.facility.AbstractUpdateableLocation;
import org.bocogop.wr.model.facility.Facility;
import org.bocogop.wr.persistence.queryCustomization.QueryCustomization;
import org.bocogop.wr.persistence.queryCustomization.fieldTypes.benefitingService.BenefitingServiceFieldType;
import org.bocogop.wr.service.BenefitingServiceService;

@Service
public class BenefitingServiceServiceImpl extends AbstractServiceImpl implements BenefitingServiceService {
	private static final Logger log = LoggerFactory.getLogger(BenefitingServiceServiceImpl.class);

	public static final String DEFAULT_GENERAL_ROLE_NAME = "General";

	public void saveAtLocationsOrUpdate(Long facilityId, List<Long> locationIds, Long benefitingServiceId, String name,
			String abbreviation, String subdivision, boolean active, boolean gamesRelated)
			throws ServiceValidationException {
		BenefitingService service = new BenefitingService();
		if (benefitingServiceId != null) {
			service = benefitingServiceDAO.findRequiredByPrimaryKey(benefitingServiceId);
		} else {
			// making a new custom service
			Facility facility = facilityDAO.findRequiredByPrimaryKey(facilityId);
			service.setFacility(facility);
			service.setScope(ScopeType.LOCAL);

			List<Long> finalLocationIds = new ArrayList<>(locationIds);
			/*
			 * Move the Main Facility to the front of the list so that we use it
			 * for the required and read only flag - CPB
			 */
			if (finalLocationIds.contains(-1L)) {
				finalLocationIds.remove(-1L);
				finalLocationIds.add(0, -1L);
			}

			boolean addedRequired = false;
			for (Long locationId : finalLocationIds) {
				boolean isMainFacility = locationId == -1L;
				AbstractUpdateableLocation<?> location = isMainFacility ? facility
						: locationDAO.findRequiredByPrimaryKey(locationId);

				BenefitingServiceRole bsr = new BenefitingServiceRole();
				bsr.setBenefitingService(service);
				bsr.setFacility(location);
				BenefitingServiceRoleServiceImpl.setInternalRoleFields(bsr, DEFAULT_GENERAL_ROLE_NAME, null, null, null,
						null, benefitingServiceRoleTypeDAO.findByLookup(GENERAL), !active, ScopeType.LOCAL);
				if (!addedRequired) {
					bsr.setRequiredAndReadOnly(true);
					addedRequired = true;
				}
				service.getBenefitingServiceRoles().add(bsr);
			}
		}

		service.setName(name);
		service.setAbbreviation(abbreviation);
		service.setSubdivision(subdivision);
		service.setInactive(!active);
		service.setGamesRelated(gamesRelated);

		service = saveOrUpdate(service);
	}

	public BenefitingService saveOrUpdate(BenefitingService benefitingService) throws ServiceValidationException {
		benefitingServiceDAO.detach(benefitingService);

		List<Long> dupNameIds = PersistenceUtil.translateObjectsToIds(
				benefitingServiceDAO.findByCriteria(benefitingService.getName(), benefitingService.getSubdivision(),
						null, Arrays.asList(benefitingService.getFacility().getId()), null, null, null, null));
		if (benefitingService.isPersistent())
			dupNameIds.remove(benefitingService.getId());
		if (!dupNameIds.isEmpty())
			throw new ServiceValidationException("benefitingService.error.duplicateNameWithinFacility");

		boolean inactivating = false;
		boolean activating = false;
		if (benefitingService.isPersistent()) {
			BenefitingService existingService = benefitingServiceDAO
					.findRequiredByPrimaryKey(benefitingService.getId());
			inactivating = !existingService.isInactive() && benefitingService.isInactive();
			activating = existingService.isInactive() && !benefitingService.isInactive();
		}

		benefitingService.setName(StringUtils.trim(benefitingService.getName()));

		benefitingService = benefitingServiceDAO.saveOrUpdate(benefitingService);
		if (inactivating) {
			cascadeInactivation(benefitingService.getId());
		} else if (activating) {
			ensureParentTemplateAndLocationActive(benefitingService);
			cascadeActivation(benefitingService.getId());
		}

		benefitingServiceDAO.flushAndRefresh(benefitingService);
		return benefitingService;
	}

	public void ensureParentTemplateAndLocationActive(BenefitingService benefitingService)
			throws ServiceValidationException {
		BenefitingServiceTemplate template = benefitingService.getTemplate();
		if (template != null && template.isInactive())
			throw new ServiceValidationException("benefitingService.error.templateInactive");

		AbstractUpdateableLocation<?> l = benefitingService.getFacility();
		if (l.isInactive())
			throw new ServiceValidationException("benefitingService.error.facilityOrLocationInactive",
					new Serializable[] { l.getDisplayName() });
	}

	public boolean canBeDeleted(long benefitingServiceId) {
		return !workEntryDAO.existsForCriteria(null, null, benefitingServiceId, null)
				&& !occasionalWorkEntryDAO.existsForCriteria(null, null, benefitingServiceId, null);
	}

	public void deleteOrInactivateBenefitingService(long benefitingServiceId) {
		if (canBeDeleted(benefitingServiceId)) {
			deleteBenefitingServiceInternal(benefitingServiceId, false);
		} else {
			inactivateBenefitingService(benefitingServiceId);
		}
	}

	public void deleteBenefitingService(long benefitingServiceId) {
		deleteBenefitingServiceInternal(benefitingServiceId, true);
	}

	private void deleteBenefitingServiceInternal(long benefitingServiceId, boolean check) {
		if (!check || canBeDeleted(benefitingServiceId)) {
			volunteerAssignmentDAO.bulkDeleteByCriteria(null, null, benefitingServiceId, null);
			benefitingServiceDAO.flush();
			benefitingServiceRoleDAO.bulkDeleteByCriteria(null, null, benefitingServiceId);
			benefitingServiceDAO.flush();
			benefitingServiceRoleRequirementAssociationDAO.bulkDeleteByCriteria(null, null, benefitingServiceId, null,
					null);
			benefitingServiceDAO.flush();
			benefitingServiceDAO.delete(benefitingServiceId);
			benefitingServiceDAO.flush();
		}
	}

	public void inactivateBenefitingService(long benefitingServiceId) {
		BenefitingService benefitingService = benefitingServiceDAO.findRequiredByPrimaryKey(benefitingServiceId);
		benefitingService.setInactive(true);
		benefitingService = benefitingServiceDAO.saveOrUpdate(benefitingService);
		cascadeInactivation(benefitingServiceId);
	}

	public void cascadeInactivation(long benefitingServiceId) {
		volunteerAssignmentDAO.bulkInactivateByCriteria(null, null, benefitingServiceId, null, null);
		benefitingServiceRoleDAO.bulkUpdateByCriteria(null, null, benefitingServiceId, null, null, null, false, null);
	}

	public void cascadeActivation(long benefitingServiceId) {
		benefitingServiceRoleDAO.bulkUpdateByCriteria(null, null, benefitingServiceId, null, true, null, true, null);
	}

	@Override
	public Map<Long, List<BenefitingServiceRole>> linkBenefitingServicesAndRoles(long facilityId,
			List<Long> locationIds, List<Long> newServiceTemplateIds, List<Long> newRoleTemplateIds)
			throws ServiceValidationException {
		List<Long> finalLocationIds = new ArrayList<>(locationIds);
		/*
		 * Move the Main Facility to the front of the list so that we prioritize
		 * it for the required and read only flag - CPB
		 */
		if (finalLocationIds.contains(-1L)) {
			finalLocationIds.remove(-1L);
			finalLocationIds.add(0, -1L);
		}

		// ----------- Build a map of services and roles we want to add

		Map<Long, BenefitingServiceTemplate> serviceTemplatesById = benefitingServiceTemplateDAO
				.findRequiredByPrimaryKeys(newServiceTemplateIds);
		Map<Long, BenefitingServiceRoleTemplate> roleTemplatesById = newRoleTemplateIds == null ? new HashMap<>()
				: benefitingServiceRoleTemplateDAO.findRequiredByPrimaryKeys(newRoleTemplateIds);

		Map<BenefitingServiceTemplate, List<BenefitingServiceRoleTemplate>> addMap = new HashMap<>();
		for (BenefitingServiceTemplate t : serviceTemplatesById.values())
			addMap.put(t, new ArrayList<>());

		for (BenefitingServiceRoleTemplate r : roleTemplatesById.values()) {
			List<BenefitingServiceRoleTemplate> roleList = addMap.get(r.getBenefitingServiceTemplate());
			if (roleList == null)
				continue;
			roleList.add(r);
		}

		/* Ensure all required & read-only roles are present */
		for (BenefitingServiceTemplate b : serviceTemplatesById.values()) {
			List<BenefitingServiceRoleTemplate> list = addMap.get(b);

			for (BenefitingServiceRoleTemplate r : b.getServiceRoleTemplates()) {
				if (r.isRequiredAndReadOnly() && !list.contains(r))
					list.add(r);
			}
		}

		// ---------- Compare to existing benefiting services and roles at each
		// facility and add only missing ones

		Facility facility = facilityDAO.findRequiredByPrimaryKey(facilityId);
		List<BenefitingService> existingBenefitingServicesLinkedToATemplate = benefitingServiceDAO.findByCriteria(null,
				null, null, asList(facility.getId()), false, null, null, null,
				new QueryCustomization(BenefitingServiceFieldType.TEMPLATE));
		Map<BenefitingServiceTemplate, BenefitingService> servicesByTemplate = existingBenefitingServicesLinkedToATemplate
				.stream().collect(toMap(BenefitingService::getTemplate, identity(), (a, b) -> a));

		Map<Long, List<BenefitingServiceRole>> addedItems = new HashMap<>();

		for (Entry<BenefitingServiceTemplate, List<BenefitingServiceRoleTemplate>> entry : addMap.entrySet()) {
			BenefitingServiceTemplate s = entry.getKey();
			List<BenefitingServiceRoleTemplate> rList = entry.getValue();
			BenefitingService existingService = servicesByTemplate.get(s);

			if (existingService == null) {
				existingService = new BenefitingService(s, facility);
				existingService = benefitingServiceDAO.saveOrUpdate(existingService);
			}

			Map<BenefitingServiceRoleTemplate, Map<AbstractUpdateableLocation<?>, BenefitingServiceRole>> rolesByLocationByTemplate = new HashMap<>();
			for (BenefitingServiceRole bsr : existingService.getBenefitingServiceRoles()) {
				Map<AbstractUpdateableLocation<?>, BenefitingServiceRole> rolesByLocation = rolesByLocationByTemplate
						.get(bsr.getTemplate());
				if (rolesByLocation == null) {
					rolesByLocation = new HashMap<>();
					rolesByLocationByTemplate.put(bsr.getTemplate(), rolesByLocation);
				}
				rolesByLocation.put(bsr.getFacility(), bsr);
			}

			boolean requiredNeeded = true;
			for (BenefitingServiceRole bsr : existingService.getBenefitingServiceRoles())
				if (bsr.isRequiredAndReadOnly())
					requiredNeeded = false;

			for (BenefitingServiceRoleTemplate r : rList) {
				Map<AbstractUpdateableLocation<?>, BenefitingServiceRole> rolesByLocation = rolesByLocationByTemplate
						.get(r);
				if (rolesByLocation == null)
					rolesByLocation = new HashMap<>();

				for (Long locationId : finalLocationIds) {
					// -1 means main facility
					boolean isMainFacility = locationId == -1L;
					AbstractUpdateableLocation<?> l = isMainFacility ? facility
							: locationDAO.findRequiredByPrimaryKey(locationId);

					BenefitingServiceRole role = rolesByLocation.get(l);
					if (role == null) {
						role = new BenefitingServiceRole(r, existingService, l);
						if (requiredNeeded) {
							role.setRequiredAndReadOnly(true);
							requiredNeeded = false;
						}

						role = benefitingServiceRoleDAO.saveOrUpdate(role);

						addedItems.computeIfAbsent(locationId, k -> new ArrayList<BenefitingServiceRole>()).add(role);
					}
				}
			}
		}

		return addedItems;
	}

	@Override
	public void reactivate(long benefitingServiceId) throws ServiceValidationException {
		BenefitingService existingService = benefitingServiceDAO.findRequiredByPrimaryKey(benefitingServiceId);
		boolean wasInactive = existingService.isInactive();
		if (wasInactive) {
			ensureParentTemplateAndLocationActive(existingService);

			existingService.setInactive(false);
			existingService = benefitingServiceDAO.saveOrUpdate(existingService);
			cascadeActivation(benefitingServiceId);
		}
	}

}
