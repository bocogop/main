package org.bocogop.wr.web.benefitingServices;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.annotation.JsonView;

import org.bocogop.shared.service.validation.ServiceValidationException;
import org.bocogop.shared.util.PersistenceUtil;
import org.bocogop.shared.util.WebUtil;
import org.bocogop.wr.model.benefitingService.BenefitingServiceRoleTemplate;
import org.bocogop.wr.model.benefitingService.BenefitingServiceRoleTemplate.BenefitingServiceRoleTemplateView;
import org.bocogop.wr.model.benefitingService.BenefitingServiceRoleType;
import org.bocogop.wr.model.benefitingService.BenefitingServiceRoleType.BenefitingServiceRoleTypeValue;
import org.bocogop.wr.model.benefitingService.BenefitingServiceTemplate;
import org.bocogop.wr.model.benefitingService.BenefitingServiceTemplate.BenefitingServiceTemplateView;
import org.bocogop.wr.service.BenefitingServiceRoleTemplateService.MergeErrorReport;
import org.bocogop.wr.service.impl.BenefitingServiceServiceImpl;
import org.bocogop.wr.web.AbstractAppController;
import org.bocogop.wr.web.breadcrumbs.Breadcrumb;

@Controller
public class BenefitingServiceTemplatesController extends AbstractAppController {

	@RequestMapping("/manageBenefitingServiceTemplates.htm")
	@Breadcrumb("Manage Benefiting Service Templates")
	// @PreAuthorize("hasAnyAuthority('" + Permission.ORG_CODE_NATIONAL_READ +
	// ", " + Permission.ORG_CODE_LOCAL_READ
	// + "')")
	public String listBenefitingServiceTemplates(ModelMap model) {
		model.addAttribute("allBenefitingServiceRoleTypes", benefitingServiceRoleTypeDAO.findAllSorted());
		WebUtil.addEnumToModel(BenefitingServiceRoleTypeValue.class, model);
		return "manageBenefitingServiceTemplates";
	}

	@RequestMapping("/benefitingServiceTemplatesWithRoles")
	@JsonView(BenefitingServiceTemplateView.ListBenefitingServiceTemplatesWithRoles.class)
	public @ResponseBody Map<String, Object> getBenefitingServiceTemplatesIncludingRoles(
			@RequestParam(required = false, defaultValue = "false") boolean bypassCounts,
			@RequestParam(required = false) Boolean activeStatus) {
		SortedSet<BenefitingServiceTemplate> benefitingServiceTemplates = activeStatus != null
				? new TreeSet<>(benefitingServiceTemplateDAO.findByCriteria(null, activeStatus, null, null))
				: benefitingServiceTemplateDAO.findAllSorted();

		Map<String, Object> results = new HashMap<>();
		results.put("benefitingServiceTemplates", benefitingServiceTemplates);

		if (!bypassCounts) {
			List<Long> allRoleIds = new ArrayList<>();
			for (BenefitingServiceTemplate bs : benefitingServiceTemplates)
				allRoleIds.addAll(PersistenceUtil.translateObjectsToIds(bs.getServiceRoleTemplates()));

			Map<Long, Integer[]> roleCounts = benefitingServiceRoleTemplateDAO
					.countVolunteersForBenefitingServiceRoleTemplateIds(allRoleIds);
			Map<Long, Integer> roleOccasionalHours = benefitingServiceTemplateDAO
					.countOccasionalHoursForBenefitingServiceRoleTemplateIds(allRoleIds);

			results.put("volunteerCountsForRoles", roleCounts);
			results.put("occasionalHoursForRoles", roleOccasionalHours);
		}

		return results;
	}

	@RequestMapping("/benefitingServiceTemplate")
	@JsonView(BenefitingServiceTemplateView.Extended.class)
	public @ResponseBody BenefitingServiceTemplate getBenefitingServiceTemplateDetails(@RequestParam long id) {
		BenefitingServiceTemplate bs = benefitingServiceTemplateDAO.findRequiredByPrimaryKey(id);
		return bs;
	}

	@RequestMapping("/benefitingServiceRoleTemplate")
	@JsonView(BenefitingServiceRoleTemplateView.Extended.class)
	public @ResponseBody BenefitingServiceRoleTemplate getBenefitingServiceRoleTemplateDetails(@RequestParam long id) {
		BenefitingServiceRoleTemplate bs = benefitingServiceRoleTemplateDAO.findRequiredByPrimaryKey(id);
		return bs;
	}

	@RequestMapping("/benefitingServiceTemplate/saveOrUpdate")
	public @ResponseBody boolean saveOrUpdateBenefitingService(
			@RequestParam(required = false) Long benefitingServiceTemplateId, @RequestParam String name,
			@RequestParam String abbreviation, @RequestParam String subdivision, @RequestParam boolean active,
			@RequestParam boolean gamesRelated) throws ServiceValidationException {
		BenefitingServiceTemplate service = new BenefitingServiceTemplate();
		if (benefitingServiceTemplateId != null) {
			service = benefitingServiceTemplateDAO.findRequiredByPrimaryKey(benefitingServiceTemplateId);
		} else {
			BenefitingServiceRoleTemplate bsr = new BenefitingServiceRoleTemplate();
			bsr.setBenefitingServiceTemplate(service);
			bsr.setName(BenefitingServiceServiceImpl.DEFAULT_GENERAL_ROLE_NAME);
			bsr.setRoleType(benefitingServiceRoleTypeDAO.findByLookup(BenefitingServiceRoleTypeValue.GENERAL));
			bsr.setRequiredAndReadOnly(true);
			service.getServiceRoleTemplates().add(bsr);
		}

		service.setName(name);
		service.setAbbreviation(abbreviation);
		service.setSubdivision(subdivision);
		service.setInactive(!active);
		service.setGamesRelated(gamesRelated);

		service = benefitingServiceTemplateService.saveOrUpdate(service);
		return true;
	}

	@RequestMapping("/benefitingServiceTemplate/reactivate")
	public @ResponseBody boolean reactivateBenefitingServiceTemplate(@RequestParam long benefitingServiceTemplateId) {
		benefitingServiceTemplateService.reactivate(benefitingServiceTemplateId);
		return true;
	}

	@RequestMapping("/benefitingServiceRoleTemplate/saveOrUpdate")
	public @ResponseBody boolean saveOrUpdateBenefitingServiceRole(
			@RequestParam(required = false) Long benefitingServiceRoleTemplateId,
			@RequestParam(required = false) Long benefitingServiceTemplateId, @RequestParam String name,
			@RequestParam boolean active, @RequestParam(required = false) BenefitingServiceRoleType roleType)
			throws ServiceValidationException {
		BenefitingServiceRoleTemplate role = new BenefitingServiceRoleTemplate();
		BenefitingServiceTemplate benefitingServiceTemplate;

		if (benefitingServiceRoleTemplateId != null) {
			role = benefitingServiceRoleTemplateDAO.findRequiredByPrimaryKey(benefitingServiceRoleTemplateId);
			benefitingServiceTemplate = role.getBenefitingServiceTemplate();
		} else {
			benefitingServiceTemplate = benefitingServiceTemplateDAO
					.findRequiredByPrimaryKey(benefitingServiceTemplateId);
			role.setBenefitingServiceTemplate(benefitingServiceTemplate);
		}

		role.setName(name);
		role.setInactive(!active);
		role.setRoleType(roleType);
		role.setInactive(benefitingServiceTemplate.isInactive());

		role = benefitingServiceRoleTemplateService.saveOrUpdate(role);
		return true;
	}

	@RequestMapping("/benefitingServiceRoleTemplate/merge")
	public @ResponseBody boolean mergeBenefitingServiceRoleTemplate(
			@RequestParam long fromBenefitingServiceRoleTemplateId,
			@RequestParam long toBenefitingServiceRoleTemplateId) throws Exception {
		MergeErrorReport errorReport = benefitingServiceRoleTemplateService.merge(fromBenefitingServiceRoleTemplateId,
				toBenefitingServiceRoleTemplateId);
		if (errorReport.hasErrors()) {
			Exception e = errorReport.getException();
			throw new Exception("Error performing merge", e);
		}

		return true;
	}

	@RequestMapping("/benefitingServiceTemplate/deleteOrInactivate")
	public @ResponseBody boolean deleteOrInactivateBenefitingService(@RequestParam long benefitingServiceTemplateId) {
		benefitingServiceTemplateService.deleteOrInactivateBenefitingServiceTemplate(benefitingServiceTemplateId);
		return true;
	}

	@RequestMapping("/benefitingServiceTemplate/delete")
	public @ResponseBody boolean deleteBenefitingServiceTemplate(@RequestParam long benefitingServiceTemplateId) {
		benefitingServiceTemplateService.deleteBenefitingServiceTemplate(benefitingServiceTemplateId);
		return true;
	}

	@RequestMapping("/benefitingServiceTemplate/inactivate")
	public @ResponseBody boolean inactivateBenefitingServiceTemplate(@RequestParam long benefitingServiceTemplateId) {
		benefitingServiceTemplateService.inactivateBenefitingServiceTemplate(benefitingServiceTemplateId);
		return true;
	}

	@RequestMapping("/benefitingServiceRoleTemplate/reactivate")
	public @ResponseBody boolean reactivateBenefitingServiceRoleTemplate(
			@RequestParam long benefitingServiceRoleTemplateId) throws ServiceValidationException {
		benefitingServiceRoleTemplateService.reactivate(benefitingServiceRoleTemplateId);
		return true;
	}

	@RequestMapping("/benefitingServiceRoleTemplate/deleteOrInactivate")
	public @ResponseBody boolean deleteOrInactivateBenefitingServiceRole(
			@RequestParam long benefitingServiceRoleTemplateId) throws ServiceValidationException {
		benefitingServiceRoleTemplateService
				.deleteOrInactivateBenefitingServiceRoleTemplate(benefitingServiceRoleTemplateId);
		return true;
	}

	@RequestMapping("/benefitingServiceRoleTemplate/delete")
	public @ResponseBody boolean deleteBenefitingServiceRoleTemplate(
			@RequestParam long benefitingServiceRoleTemplateId) {
		benefitingServiceRoleTemplateService.deleteBenefitingServiceRoleTemplate(benefitingServiceRoleTemplateId);
		return true;
	}

	@RequestMapping("/benefitingServiceRoleTemplate/inactivate")
	public @ResponseBody boolean inactivateBenefitingServiceRoleTemplate(
			@RequestParam long benefitingServiceRoleTemplateId) throws ServiceValidationException {
		benefitingServiceRoleTemplateService.inactivateBenefitingServiceRoleTemplate(benefitingServiceRoleTemplateId);
		return true;
	}

}
