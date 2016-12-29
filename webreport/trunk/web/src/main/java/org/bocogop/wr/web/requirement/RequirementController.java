package org.bocogop.wr.web.requirement;

import static org.bocogop.wr.model.requirement.RequirementApplicationType.ROLE_TYPE;
import static org.bocogop.wr.model.requirement.RequirementApplicationType.SPECIFIC_ROLES;
import static org.bocogop.wr.model.requirement.RequirementScopeType.FACILITY;
import static org.bocogop.wr.model.requirement.RequirementScopeType.GLOBAL;

import java.util.List;
import java.util.Map;
import java.util.SortedSet;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;

import com.fasterxml.jackson.annotation.JsonView;

import org.bocogop.shared.model.Permission;
import org.bocogop.shared.service.validation.ServiceValidationException;
import org.bocogop.shared.util.CollectionUtil;
import org.bocogop.shared.util.CollectionUtil.SynchronizeCollectionsOps;
import org.bocogop.shared.util.WebUtil;
import org.bocogop.wr.model.benefitingService.BenefitingServiceRole;
import org.bocogop.wr.model.benefitingService.BenefitingServiceRoleTemplate;
import org.bocogop.wr.model.benefitingService.BenefitingServiceRoleType;
import org.bocogop.wr.model.requirement.AbstractRequirement;
import org.bocogop.wr.model.requirement.AbstractRequirement.RequirementView;
import org.bocogop.wr.model.requirement.RequirementDateType.RequirementDateTypeValue;
import org.bocogop.wr.model.requirement.RequirementStatus.RequirementStatusValue;
import org.bocogop.wr.model.requirement.BenefitingServiceRoleRequirementAssociation;
import org.bocogop.wr.model.requirement.BenefitingServiceRoleTemplateRequirementAssociation;
import org.bocogop.wr.model.requirement.FacilityRequirement;
import org.bocogop.wr.model.requirement.FacilityRoleRequirement;
import org.bocogop.wr.model.requirement.FacilityRoleTypeRequirement;
import org.bocogop.wr.model.requirement.GlobalRequirement;
import org.bocogop.wr.model.requirement.GlobalRoleRequirement;
import org.bocogop.wr.model.requirement.GlobalRoleTypeRequirement;
import org.bocogop.wr.model.requirement.RequirementApplicationType;
import org.bocogop.wr.model.requirement.RequirementAvailableStatus;
import org.bocogop.wr.model.requirement.RequirementDateType;
import org.bocogop.wr.model.requirement.RequirementScopeType;
import org.bocogop.wr.model.requirement.RequirementStatus;
import org.bocogop.wr.model.requirement.RequirementType;
import org.bocogop.wr.web.AbstractAppController;
import org.bocogop.wr.web.breadcrumbs.Breadcrumb;
import org.bocogop.wr.web.validation.ValidationException;

@Controller
@SessionAttributes(value = { AbstractAppController.DEFAULT_COMMAND_NAME })
public class RequirementController extends AbstractAppController {

	@Autowired
	private RequirementValidator requirementValidator;

	@RequestMapping("/manageRequirements.htm")
	@Breadcrumb("Manage Requirements")
	@PreAuthorize("hasAnyAuthority('" + Permission.REQUIREMENTS_LOCAL_MANAGE + ", "
			+ Permission.REQUIREMENTS_GLOBAL_MANAGE + "')")
	public String manageRequirements(ModelMap model) {
		model.put("allRequirementDateTypes", requirementDateTypeDAO.findAllSorted());
		WebUtil.addEnumToModel(RequirementApplicationType.class, model);
		WebUtil.addEnumToModel(RequirementScopeType.class, model);
		model.addAttribute("DateValueNotApplicable", RequirementDateTypeValue.NOT_APPLICABLE);

		return "manageRequirements";
	}

	@RequestMapping("/requirements/local")
	@JsonView(RequirementView.Basic.class)
	public @ResponseBody List<AbstractRequirement> getFacilityRequirements() {
		List<AbstractRequirement> results = requirementDAO.findByCriteria(getFacilityContextId(), null);
		return results;
	}

	@RequestMapping("/requirements/global")
	@JsonView(RequirementView.Basic.class)
	public @ResponseBody SortedSet<GlobalRequirement> getGlobalRequirements() {
		SortedSet<GlobalRequirement> results = requirementDAO.findAllSortedByType(GlobalRequirement.class);
		return results;
	}

	@RequestMapping("/requirementCreate.htm")
	@Breadcrumb("Create Requirement")
	@PreAuthorize("hasAnyAuthority('" + Permission.REQUIREMENTS_GLOBAL_MANAGE + ", "
			+ Permission.REQUIREMENTS_LOCAL_MANAGE + "')")
	public String requirementCreate(@RequestParam(name = "scope") RequirementScopeType scope, ModelMap model,
			HttpServletRequest request) {
		RequirementDateType notApplicable = requirementDateTypeDAO
				.findByLookup(RequirementDateTypeValue.NOT_APPLICABLE);
		RequirementCommand command = new RequirementCommand(scope, notApplicable);
		addRequiredStatuses(command);
		model.addAttribute(DEFAULT_COMMAND_NAME, command);
		createReferenceData(command, model);
		return "createRequirement";
	}

	public void addRequiredStatuses(RequirementCommand command) {
		command.getValidStatuses().add(requirementStatusDAO.findByLookup(RequirementStatusValue.NEW));
		command.getValidStatuses().add(requirementStatusDAO.findByLookup(RequirementStatusValue.MET));
	}

	@RequestMapping("/requirementEdit.htm")
	@Breadcrumb("Edit Requirement")
	@PreAuthorize("hasAnyAuthority('" + Permission.REQUIREMENTS_GLOBAL_MANAGE + ", "
			+ Permission.REQUIREMENTS_LOCAL_MANAGE + "')")
	public String requirementEdit(@RequestParam long id, ModelMap model, HttpServletRequest request) {
		AbstractRequirement r = requirementDAO.findByPrimaryKey(id);
		RequirementCommand command = new RequirementCommand(r);
		addRequiredStatuses(command);
		model.addAttribute(DEFAULT_COMMAND_NAME, command);
		createReferenceData(command, model);
		return "editRequirement";
	}

	@RequestMapping("/requirement/inactivate")
	public @ResponseBody boolean inactivateRequirement(@RequestParam long id) {
		requirementService.inactivateRequirement(id);
		return true;
	}

	@RequestMapping("/requirement/reactivate")
	public @ResponseBody boolean reactivateRequirement(@RequestParam long id) throws ServiceValidationException {
		requirementService.reactivateRequirement(id);
		return true;
	}

	@RequestMapping(value = "/requirementSubmit.htm", method = RequestMethod.POST)
	@PreAuthorize("hasAnyAuthority('" + Permission.REQUIREMENTS_LOCAL_MANAGE + ", "
			+ Permission.REQUIREMENTS_GLOBAL_MANAGE + "')")
	public String requirementSubmit(@ModelAttribute(DEFAULT_COMMAND_NAME) RequirementCommand command,
			BindingResult result, SessionStatus status, ModelMap model,
			@RequestParam(required = false) List<Long> roleSelectItems,
			@RequestParam(required = false) List<Long> roleTemplateSelectItems, HttpServletRequest request)
			throws ValidationException {

		RequirementScopeType scope = command.getScope();
		RequirementApplicationType appType = command.getApplicationType();
		RequirementType type = command.getType();

		// ----------------------------- Build/Update Requirement

		AbstractRequirement r;
		boolean isEdit = command.getRequirementId() != null;
		if (isEdit) {
			r = requirementDAO.findRequiredByPrimaryKey(command.getRequirementId());
		} else {
			r = AbstractRequirement.getInstance(scope, appType);
			if (scope == RequirementScopeType.FACILITY)
				((FacilityRequirement) r).setFacility(getRequiredFacilityContext());
		}

		r.setType(type);
		if (type != RequirementType.STANDARD) {
			r.setName(type.getName());
		} else {
			r.setName(command.getName());
		}

		r.setDescription(command.getDescription());
		r.setTmsCourseId(command.getTmsCourseId());
		r.setDateType(command.getDateType());
		r.setDaysNotification(command.getDaysNotification());
		r.setInactive(!command.isActive());
		r.setPreventTimeposting(command.isPreventTimeposting());

		addRequiredStatuses(command);

		final AbstractRequirement rFinal = r;
		CollectionUtil.synchronizeCollections(r.getAvailableStatuses(), command.getValidStatuses(),
				new SynchronizeCollectionsOps<RequirementAvailableStatus, RequirementStatus>() {
					@Override
					public RequirementAvailableStatus convert(RequirementStatus u) {
						return new RequirementAvailableStatus(rFinal, u);
					}
				});

		if (scope == FACILITY) {
			if (appType == SPECIFIC_ROLES) {
				Map<Long, BenefitingServiceRole> selectedRoleMap = benefitingServiceRoleDAO
						.findByPrimaryKeys(roleSelectItems);
				command.setSpecificRoles(selectedRoleMap.values());

				final FacilityRoleRequirement grr = (FacilityRoleRequirement) r;
				CollectionUtil.synchronizeCollections(grr.getBenefitingServiceRoleAssociations(),
						selectedRoleMap.values(),
						new SynchronizeCollectionsOps<BenefitingServiceRoleRequirementAssociation, BenefitingServiceRole>() {
							@Override
							public BenefitingServiceRoleRequirementAssociation convert(BenefitingServiceRole u) {
								return new BenefitingServiceRoleRequirementAssociation(grr, u);
							}
						});
			} else if (appType == ROLE_TYPE) {
				FacilityRoleTypeRequirement frtr = (FacilityRoleTypeRequirement) r;
				frtr.setRoleType(command.getRoleType());
			}
		} else if (scope == GLOBAL) {
			if (appType == SPECIFIC_ROLES) {
				Map<Long, BenefitingServiceRoleTemplate> selectedRoleTemplateMap = benefitingServiceRoleTemplateDAO
						.findByPrimaryKeys(roleTemplateSelectItems);
				command.setSpecificRoleTemplates(selectedRoleTemplateMap.values());

				final GlobalRoleRequirement grr = (GlobalRoleRequirement) r;
				CollectionUtil.synchronizeCollections(grr.getBenefitingServiceRoleTemplateAssociations(),
						selectedRoleTemplateMap.values(),
						new SynchronizeCollectionsOps<BenefitingServiceRoleTemplateRequirementAssociation, BenefitingServiceRoleTemplate>() {
							@Override
							public BenefitingServiceRoleTemplateRequirementAssociation convert(
									BenefitingServiceRoleTemplate u) {
								return new BenefitingServiceRoleTemplateRequirementAssociation(grr, u);
							}
						});
			} else if (appType == ROLE_TYPE) {
				GlobalRoleTypeRequirement grtr = (GlobalRoleTypeRequirement) r;
				grtr.setRoleType(command.getRoleType());
			}
		}

		// ----------------------------- Validate & update

		requirementValidator.validate(command, result, false);
		boolean hasErrors = result.hasErrors();

		if (!hasErrors) {
			try {
				r = requirementService.saveOrUpdate(r);
				userNotifier.notifyUserOnceWithMessage(request,
						getMessage(isEdit ? "requirement.update.success" : "requirement.create.success"));
			} catch (ServiceValidationException e) {
				webValidationService.handle(e, result);
				hasErrors = true;
			}
		}

		if (hasErrors) {
			createReferenceData(command, model);
			return isEdit ? "editRequirement" : "createRequirement";
		} else {
			status.setComplete();
			return "redirect:/requirementEdit.htm?id=" + r.getId();
		}
	}

	@RequestMapping(value = "/requirementChangeType.htm", method = RequestMethod.POST)
	@PreAuthorize("hasAnyAuthority('" + Permission.REQUIREMENTS_LOCAL_MANAGE + ", "
			+ Permission.REQUIREMENTS_GLOBAL_MANAGE + "')")
	public String requirementChangeType(@RequestParam long requirementId,
			@RequestParam RequirementApplicationType requirementChangeNewType,
			@RequestParam BenefitingServiceRoleType requirementChangeNewRoleType, HttpServletRequest request) {
		requirementService.changeType(requirementId, requirementChangeNewType, requirementChangeNewRoleType);
		userNotifier.notifyUserOnceWithMessage(request, getMessage("requirement.update.success"));
		return "redirect:/requirementEdit.htm?id=" + requirementId;
	}

	@RequestMapping("/requirement/deleteCheck")
	public @ResponseBody int deleteCheck(@RequestParam long id) throws ServiceValidationException {
		return volunteerRequirementDAO.countByCriteria(id);
	}

	@RequestMapping("/requirement/delete")
	public @ResponseBody boolean delete(@RequestParam long id) throws ServiceValidationException {
		requirementService.delete(id);
		return true;
	}

	private void createReferenceData(RequirementCommand command, ModelMap model) {
		model.put("allRequirementDateTypes", requirementDateTypeDAO.findAllSorted(true));
		model.put("allRequirementStatuses", requirementStatusDAO.findAllSorted(true));
		model.put("allBenefitingServiceRoleTypes", benefitingServiceRoleTypeDAO.findAllSorted(true));
		WebUtil.addEnumToModel(RequirementScopeType.class, model);
		WebUtil.addEnumToModel(RequirementApplicationType.class, model);
		WebUtil.addEnumToModel(RequirementStatusValue.class, model);
		WebUtil.addEnumToModel(RequirementDateTypeValue.class, model);
		WebUtil.addEnumToModel(RequirementType.class, model);
	}

}
