package org.bocogop.wr.web.serviceParameters;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.SortedSet;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;

import org.bocogop.shared.model.Permission;
import org.bocogop.shared.model.Permission.PermissionType;
import org.bocogop.shared.service.validation.ServiceValidationException;
import org.bocogop.wr.model.facility.Facility;
import org.bocogop.wr.model.facility.StaffTitle;
import org.bocogop.wr.model.voluntaryService.VoluntaryServiceParameters;
import org.bocogop.wr.model.voluntaryService.VoluntaryServiceStaff;
import org.bocogop.wr.model.voluntaryService.VoluntaryServiceType;
import org.bocogop.wr.model.voluntaryService.VoluntaryServiceType.VoluntaryServiceTypeValue;
import org.bocogop.wr.util.DateUtil;
import org.bocogop.wr.web.AbstractAppController;
import org.bocogop.wr.web.AbstractCommonAppController;
import org.bocogop.wr.web.breadcrumbs.Breadcrumb;
import org.bocogop.wr.web.validation.ValidationException;

@Controller
@SessionAttributes(value = { AbstractCommonAppController.DEFAULT_COMMAND_NAME })
public class ServiceParametersController extends AbstractAppController {

	@Autowired
	private ServiceParametersValidator serviceParametersValidator;

	@RequestMapping("/editVoluntaryService.htm")
	@Breadcrumb("Edit Voluntary Service")
	@PreAuthorize("hasAuthority('" + Permission.PERM_CODE_SERVICE_READ + "')")
	public String editVoluntaryService(ModelMap model, HttpServletRequest request) {
		Facility facility = getRequiredFacilityContext();

		VoluntaryServiceParameters serviceParameters = facility.getVoluntaryServiceParameters();
		if (serviceParameters.getVoluntaryServiceType() == null) {
			VoluntaryServiceType voluntaryServiceType = voluntaryServiceTypeDAO
					.findByLookup(VoluntaryServiceTypeValue.SERVICE);
			serviceParameters.setVoluntaryServiceType(voluntaryServiceType);
		}
		ServiceParametersCommand command = new ServiceParametersCommand(serviceParameters);
		model.addAttribute(DEFAULT_COMMAND_NAME, command);
		createReferenceData(model);
		setFormAsReadOnlyUnlessUserHasPermissions(model, PermissionType.PERM_CODE_SERVICE_CREATE);
		return "editVoluntaryService";
	}

	@RequestMapping("/voluntaryServiceStaff")
	// @PreAuthorize("hasAuthority('" + Permission.PERM_CODE_SERVICE_READ +
	// "')")
	public @ResponseBody List<VoluntaryServiceStaff> getStaffForCurrentStation() {
		List<VoluntaryServiceStaff> staff = voluntaryServiceStaffDAO.findByCriteria(getFacilityContextId(), null);
		return staff;
	}

	@RequestMapping("/voluntaryServiceStaffCreateOrUpdate")
	// @PreAuthorize("hasAuthority('" + Permission.PERM_CODE_SERVICE_READ +
	// "')")
	public @ResponseBody VoluntaryServiceStaff createOrUpdateStaff(@RequestParam Long serviceStaffId,
			@RequestParam String newStaffUserName, @RequestParam String nickName, @RequestParam String staffNamePrefix,
			@RequestParam StaffTitle staffVavsRole, @RequestParam String staffGrade,
			@DateTimeFormat(pattern = DateUtil.TWO_DIGIT_DATE_ONLY) @RequestParam LocalDate staffVavsStartDate,
			@DateTimeFormat(pattern = DateUtil.TWO_DIGIT_DATE_ONLY) @RequestParam LocalDate staffVavsEndDate,
			@DateTimeFormat(pattern = DateUtil.TWO_DIGIT_DATE_ONLY) @RequestParam LocalDate staffRetirementEligibleDate,
			@DateTimeFormat(pattern = DateUtil.TWO_DIGIT_DATE_ONLY) @RequestParam LocalDate staffRetirementEstimateDate,
			@RequestParam boolean staffIsVavsLeadership, @RequestParam String staffComment, @RequestParam boolean staffEmailNotifications)
			throws ServiceValidationException {

		VoluntaryServiceStaff serviceStaff = null;
		if (serviceStaffId == null) {
			serviceStaff = voluntaryServiceStaffService.createOrRetrieveServiceStaff(newStaffUserName,
					getFacilityContextId());
		} else {
			serviceStaff = voluntaryServiceStaffDAO.findRequiredByPrimaryKey(serviceStaffId);
		}

		serviceStaff.setNickName(nickName);
		serviceStaff.setNamePrefix(staffNamePrefix);
		serviceStaff.setStaffTitle(staffVavsRole);
		serviceStaff.setGrade(staffGrade);
		serviceStaff.setVavsStartDate(staffVavsStartDate);
		serviceStaff.setVavsEndDate(staffVavsEndDate);
		serviceStaff.setRetirementEligibleDate(staffRetirementEligibleDate);
		serviceStaff.setRetirementEstimateDate(staffRetirementEstimateDate);
		serviceStaff.setVavsLeadership(staffIsVavsLeadership);
		serviceStaff.setComment(staffComment);
		serviceStaff.setEmailNotifications(staffEmailNotifications);
		serviceStaff = voluntaryServiceStaffService.saveOrUpdate(serviceStaff);

		return serviceStaff;
	}

	@RequestMapping("/deleteServiceStaff")
	@PreAuthorize("hasAuthority('" + Permission.VOL_SVC_STAFF_DELETE + "')")
	public @ResponseBody boolean deleteStaff(@RequestParam long serviceStaffId) throws ServiceValidationException {
		voluntaryServiceStaffService.delete(serviceStaffId);
		return true;
	}

	private void createReferenceData(ModelMap model) {
		model.put("allServiceTypes", voluntaryServiceTypeDAO.findAllSorted());
		SortedSet<StaffTitle> activeTitles = staffTitleDAO.findAllActiveSorted();
		model.put("allActiveStaffTitles",activeTitles );
	}

	@RequestMapping("/serviceParametersSubmit.htm")
	@PreAuthorize("hasAuthority('" + Permission.PERM_CODE_SERVICE_CREATE + "')")
	public String submitServiceParameters(@ModelAttribute(DEFAULT_COMMAND_NAME) ServiceParametersCommand command,
			BindingResult result, SessionStatus status, ModelMap model, HttpServletRequest request)
			throws ValidationException {
		VoluntaryServiceParameters serviceParameters = command.getServiceParameters();

		/*
		 * Perform any remaining binding steps from Command obj -> Voluntary
		 * Service Parameters here
		 */

		/* Validation step (JSR303, other custom logic in the validator) */
		serviceParametersValidator.validate(command, result, false, "serviceParameters");
		boolean hasErrors = result.hasErrors();

		if (!hasErrors) {
			try {
				if (command.isChangeLastReviewedDate())
					serviceParameters.setLastUpdated(ZonedDateTime.now());
				serviceParameters = voluntaryServiceParametersService.saveOrUpdate(serviceParameters);
				userNotifier.notifyUserOnceWithMessage(request, getMessage("serviceParameters.update.success"));
			} catch (ServiceValidationException e) {
				webValidationService.handle(e, result);
				hasErrors = true;
			}
		}

		if (hasErrors) {
			createReferenceData(model);
			return "editVoluntaryService";
		} else {
			status.setComplete();
			return "redirect:/editVoluntaryService.htm";
		}
	}

	final String[] DISALLOWED_FIELDS = new String[] {"command.voluntaryServiceStaff.appUser"};

}
