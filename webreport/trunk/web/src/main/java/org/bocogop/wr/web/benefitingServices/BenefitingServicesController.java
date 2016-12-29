package org.bocogop.wr.web.benefitingServices;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.annotation.JsonView;

import org.bocogop.shared.model.Permission.PermissionType;
import org.bocogop.shared.model.lookup.sds.VAFacility;
import org.bocogop.shared.service.validation.ServiceValidationException;
import org.bocogop.shared.util.PersistenceUtil;
import org.bocogop.shared.util.WebUtil;
import org.bocogop.wr.model.benefitingService.BenefitingService;
import org.bocogop.wr.model.benefitingService.BenefitingService.BenefitingServiceView;
import org.bocogop.wr.model.benefitingService.BenefitingServiceRole;
import org.bocogop.wr.model.benefitingService.BenefitingServiceRole.BenefitingServiceRoleView;
import org.bocogop.wr.model.benefitingService.BenefitingServiceRoleType;
import org.bocogop.wr.model.benefitingService.BenefitingServiceRoleType.BenefitingServiceRoleTypeValue;
import org.bocogop.wr.model.benefitingService.BenefitingServiceTemplate.BenefitingServiceTemplateView;
import org.bocogop.wr.model.facility.Facility;
import org.bocogop.wr.model.facility.Facility.FacilityView;
import org.bocogop.wr.persistence.dao.benefitingService.BenefitingServiceRoleQuickSearchResult;
import org.bocogop.wr.persistence.dao.benefitingService.BenefitingServiceTemplateDAO.BenefitingServiceAndRoleTemplates;
import org.bocogop.wr.web.AbstractAppController;
import org.bocogop.wr.web.breadcrumbs.Breadcrumb;

@Controller
public class BenefitingServicesController extends AbstractAppController {

	@Value("${benefitingServiceRoleQuickSearch.maxResults}")
	private int benefitingServiceRoleQuickSearchMaxResults;

	@RequestMapping("/manageBenefitingServices.htm")
	@Breadcrumb("Manage Benefiting Services")
	// @PreAuthorize("hasAnyAuthority('" + Permission.ORG_CODE_NATIONAL_READ +
	// ", " + Permission.ORG_CODE_LOCAL_READ
	// + "')")
	public String listBenefitingServices(ModelMap model) {
		model.addAttribute("allBenefitingServiceRoleTypes", benefitingServiceRoleTypeDAO.findAllSorted());
		WebUtil.addEnumToModel(BenefitingServiceRoleTypeValue.class, model);
		return "manageBenefitingServices";
	}

	@RequestMapping("/getManageServiceStations")
	@JsonView(FacilityView.Basic.class)
	public @ResponseBody SortedSet<Facility> getStationsForManageBenefitingServices() {
		Set<VAFacility> validFacilities = getCurrentUser()
				.getFacilitiesWhereUserHasAllPermissions(PermissionType.LOGIN_APPLICATION);
		Map<Long, Facility> facilitiesMap = facilityDAO.findByVAFacilities(validFacilities);
		return facilitiesMap.values().stream().filter(p -> p.isActive()).collect(Collectors.toCollection(TreeSet::new));
	}

	@RequestMapping("/benefitingServicesWithRoles")
	@JsonView(BenefitingServiceView.ListBenefitingServicesWithRoles.class)
	public @ResponseBody Map<String, Object> getBenefitingServicesIncludingRoles(@RequestParam long facilityId,
			@RequestParam(required = false, defaultValue = "false") boolean bypassCounts,
			@RequestParam(required = false) Boolean activeStatus) {
		List<BenefitingService> benefitingServices = benefitingServiceDAO.findByCriteria(null, null, null,
				Arrays.asList(facilityId), null, null, activeStatus, null);

		Map<String, Object> results = new HashMap<>();
		results.put("benefitingServices", new TreeSet<>(benefitingServices));

		if (!bypassCounts) {
			List<Long> allRoleIds = new ArrayList<>();
			for (BenefitingService bs : benefitingServices)
				allRoleIds.addAll(PersistenceUtil.translateObjectsToIds(bs.getBenefitingServiceRoles()));

			Map<Long, Integer[]> roleCounts = benefitingServiceRoleDAO
					.countVolunteersForBenefitingServiceRoleIds(allRoleIds);
			Map<Long, Integer> roleOccasionalHours = benefitingServiceDAO
					.countOccasionalHoursForBenefitingServiceRoleIds(allRoleIds);
			results.put("volunteerCountsForRoles", roleCounts);
			results.put("occasionalHoursForRoles", roleOccasionalHours);
		}

		return results;
	}

	@RequestMapping("/benefitingServiceAndRoleTemplates/assignable")
	@JsonView(BenefitingServiceTemplateView.SearchUnused.class)
	public @ResponseBody SortedSet<BenefitingServiceAndRoleTemplates> getAssignableServiceAndRoleTemplates(
			@RequestParam long facilityId, @RequestParam(required = false) Long benefitingServiceId,
			@RequestParam boolean skipRequiredAndReadOnlyRoles) {
		SortedSet<BenefitingServiceAndRoleTemplates> u = benefitingServiceTemplateDAO
				.getAssignableBenefitingServiceAndRoleTemplates(facilityId, benefitingServiceId,
						/*
						 * When adding roles to an existing service, we want to
						 * show all roles, so that they can add existing roles
						 * to new physical locations. When adding new services
						 * entirely, we can skip any services we've previously
						 * added since roles for those services are added via
						 * the role-add button (which calls this method, but
						 * passes in a specific benefitingServiceId) - CPB
						 */
						benefitingServiceId == null, skipRequiredAndReadOnlyRoles);
		return u;
	}

	@RequestMapping("/benefitingService")
	@JsonView(BenefitingServiceView.Extended.class)
	public @ResponseBody BenefitingService getBenefitingServiceDetails(@RequestParam long id) {
		BenefitingService bs = benefitingServiceDAO.findRequiredByPrimaryKey(id);
		return bs;
	}

	@RequestMapping("/benefitingServiceRole")
	@JsonView(BenefitingServiceRoleView.Extended.class)
	public @ResponseBody BenefitingServiceRole getBenefitingServiceRoleDetails(@RequestParam long id) {
		BenefitingServiceRole bs = benefitingServiceRoleDAO.findRequiredByPrimaryKey(id);
		return bs;
	}

	@RequestMapping("/benefitingService/saveOrUpdate")
	public @ResponseBody boolean saveOrUpdateBenefitingService(@RequestParam(required = false) Long facilityId,
			@RequestParam(name = "locationId[]") List<Long> locationIds,
			@RequestParam(required = false) Long benefitingServiceId, @RequestParam String name,
			@RequestParam String abbreviation, @RequestParam String subdivision, @RequestParam boolean active,
			@RequestParam boolean gamesRelated) throws ServiceValidationException {
		benefitingServiceService.saveAtLocationsOrUpdate(facilityId, locationIds, benefitingServiceId, name,
				abbreviation, subdivision, active, gamesRelated);
		return true;
	}

	@RequestMapping("/benefitingService/reactivate")
	public @ResponseBody boolean reactivateBenefitingService(@RequestParam long benefitingServiceId)
			throws ServiceValidationException {
		benefitingServiceService.reactivate(benefitingServiceId);
		return true;
	}

	@RequestMapping("/benefitingService/linkServicesAndRoles")
	public @ResponseBody boolean linkBenefitingServicesAndRoles(@RequestParam long facilityId,
			@RequestParam(name = "locationId[]") List<Long> locationIds,
			@RequestParam(required = false) Long benefitingServiceId,
			@RequestParam(name = "newServices[]", required = false) List<Long> newServices,
			@RequestParam(name = "newRoles[]", required = false) List<Long> newRoles)
			throws ServiceValidationException {
		if (CollectionUtils.isEmpty(newServices) || CollectionUtils.isEmpty(locationIds))
			return false;

		benefitingServiceService.linkBenefitingServicesAndRoles(facilityId, locationIds, newServices, newRoles);
		return true;
	}

	@RequestMapping("/benefitingServiceRole/saveOrUpdate")
	public @ResponseBody boolean saveOrUpdateBenefitingServiceRole(@RequestParam(required = false) Long facilityId,
			@RequestParam(name = "locationId[]") List<Long> locationIds,
			@RequestParam(required = false) Long benefitingServiceRoleId,
			@RequestParam(required = false) Long benefitingServiceId, @RequestParam String name,
			@RequestParam String description, @RequestParam String contactName, @RequestParam String contactEmail,
			@RequestParam String contactPhone, @RequestParam boolean active,
			@RequestParam(required = false) BenefitingServiceRoleType roleType) throws ServiceValidationException {
		boolean isEdit = benefitingServiceRoleId != null;

		// benefitingServiceRoleService.saveOrUpdateAtLocations()

		benefitingServiceRoleService.saveOrUpdateAtLocations(locationIds, benefitingServiceRoleId, benefitingServiceId,
				name, description, contactName, contactEmail, contactPhone, roleType, isEdit);

		return true;
	}

	@RequestMapping("/benefitingServiceRole/merge")
	public @ResponseBody boolean mergeBenefitingServiceRole(@RequestParam long fromBenefitingServiceRoleId,
			@RequestParam long toBenefitingServiceRoleId) throws ServiceValidationException {
		benefitingServiceRoleService.merge(fromBenefitingServiceRoleId, toBenefitingServiceRoleId, true, false);
		return true;
	}

	@RequestMapping("/benefitingService/deleteOrInactivate")
	public @ResponseBody boolean deleteOrInactivateBenefitingService(@RequestParam long benefitingServiceId) {
		benefitingServiceService.deleteOrInactivateBenefitingService(benefitingServiceId);
		return true;
	}

	@RequestMapping("/benefitingService/delete")
	public @ResponseBody boolean deleteBenefitingService(@RequestParam long benefitingServiceId) {
		benefitingServiceService.deleteBenefitingService(benefitingServiceId);
		return true;
	}

	@RequestMapping("/benefitingService/inactivate")
	public @ResponseBody boolean inactivateBenefitingService(@RequestParam long benefitingServiceId) {
		benefitingServiceService.inactivateBenefitingService(benefitingServiceId);
		return true;
	}

	@RequestMapping("/benefitingServiceRole/reactivate")
	public @ResponseBody boolean reactivateBenefitingServiceRole(@RequestParam long benefitingServiceRoleId)
			throws ServiceValidationException {
		benefitingServiceRoleService.reactivate(benefitingServiceRoleId);
		return true;
	}

	@RequestMapping("/benefitingServiceRole/deleteOrInactivate")
	public @ResponseBody boolean deleteOrInactivateBenefitingServiceRole(@RequestParam long benefitingServiceRoleId)
			throws ServiceValidationException {
		benefitingServiceRoleService.deleteOrInactivateBenefitingServiceRole(benefitingServiceRoleId);
		return true;
	}

	@RequestMapping("/benefitingServiceRole/delete")
	public @ResponseBody boolean deleteBenefitingServiceRole(@RequestParam long benefitingServiceRoleId) {
		benefitingServiceRoleService.deleteBenefitingServiceRole(benefitingServiceRoleId);
		return true;
	}

	@RequestMapping("/benefitingServiceRole/inactivate")
	public @ResponseBody boolean inactivateBenefitingServiceRole(@RequestParam long benefitingServiceRoleId)
			throws ServiceValidationException {
		benefitingServiceRoleService.inactivateBenefitingServiceRole(benefitingServiceRoleId);
		return true;
	}

	@RequestMapping("/benefitingServiceRole/quickSearch/currentFacility")
	public @ResponseBody Map<String, Object> findBenefitingServiceRolesByNameAtWorkingFacility(
			@RequestParam(required = false) String name) {
		long facilityId = getFacilityContextId();
		Map<String, Object> resultMap = new HashMap<>();
		SortedSet<BenefitingServiceRoleQuickSearchResult> results = benefitingServiceRoleDAO.quickSearch(name,
				facilityId, null);
		resultMap.put("benefitingServiceRoles", results);
		return resultMap;
	}

}
