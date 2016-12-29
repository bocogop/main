package org.bocogop.wr.web;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.support.RequestContextUtils;

import org.bocogop.shared.model.Permission.PermissionType;
import org.bocogop.shared.model.lookup.sds.VAFacility;
import org.bocogop.shared.persistence.AppUserDAO;
import org.bocogop.shared.persistence.AppUserFacilityDAO;
import org.bocogop.shared.persistence.lookup.sds.GenderDAO;
import org.bocogop.shared.persistence.lookup.sds.StateDAO;
import org.bocogop.shared.persistence.lookup.sds.VAFacilityDAO;
import org.bocogop.shared.service.AppUserService;
import org.bocogop.shared.service.VAFacilityService;
import org.bocogop.shared.util.SecurityUtil;
import org.bocogop.shared.util.ServletUtil;
import org.bocogop.shared.util.context.SessionUtil;
import org.bocogop.wr.model.facility.Facility;
import org.bocogop.wr.persistence.dao.AdjustedHoursEntryDAO;
import org.bocogop.wr.persistence.dao.ApplicationParametersDAO;
import org.bocogop.wr.persistence.dao.AwardCodeDAO;
import org.bocogop.wr.persistence.dao.AwardDAO;
import org.bocogop.wr.persistence.dao.DonationDetailDAO;
import org.bocogop.wr.persistence.dao.DonationLogDAO;
import org.bocogop.wr.persistence.dao.DonationReferenceDAO;
import org.bocogop.wr.persistence.dao.DonationSummaryDAO;
import org.bocogop.wr.persistence.dao.DonorDAO;
import org.bocogop.wr.persistence.dao.ExpenditureDAO;
import org.bocogop.wr.persistence.dao.LedgerAdjustmentDAO;
import org.bocogop.wr.persistence.dao.LetterTemplateDAO;
import org.bocogop.wr.persistence.dao.MealTicketDAO;
import org.bocogop.wr.persistence.dao.NationalOfficialDAO;
import org.bocogop.wr.persistence.dao.NotificationDAO;
import org.bocogop.wr.persistence.dao.OccasionalWorkEntryDAO;
import org.bocogop.wr.persistence.dao.ParkingStickerDAO;
import org.bocogop.wr.persistence.dao.UniformDAO;
import org.bocogop.wr.persistence.dao.WorkEntryDAO;
import org.bocogop.wr.persistence.dao.benefitingService.BenefitingServiceDAO;
import org.bocogop.wr.persistence.dao.benefitingService.BenefitingServiceRoleDAO;
import org.bocogop.wr.persistence.dao.benefitingService.BenefitingServiceRoleTemplateDAO;
import org.bocogop.wr.persistence.dao.benefitingService.BenefitingServiceRoleTypeDAO;
import org.bocogop.wr.persistence.dao.benefitingService.BenefitingServiceTemplateDAO;
import org.bocogop.wr.persistence.dao.donGenPostFund.DonGenPostFundDAO;
import org.bocogop.wr.persistence.dao.facility.FacilityDAO;
import org.bocogop.wr.persistence.dao.facility.KioskDAO;
import org.bocogop.wr.persistence.dao.facility.LocationDAO;
import org.bocogop.wr.persistence.dao.leie.ExcludedEntityDAO;
import org.bocogop.wr.persistence.dao.lookup.AdministrativeUnitDAO;
import org.bocogop.wr.persistence.dao.lookup.DonationTypeDAO;
import org.bocogop.wr.persistence.dao.lookup.DonorTypeDAO;
import org.bocogop.wr.persistence.dao.lookup.FacilityTypeDAO;
import org.bocogop.wr.persistence.dao.lookup.LanguageDAO;
import org.bocogop.wr.persistence.dao.lookup.NACStatusDAO;
import org.bocogop.wr.persistence.dao.lookup.OrganizationTypeDAO;
import org.bocogop.wr.persistence.dao.lookup.RequirementDateTypeDAO;
import org.bocogop.wr.persistence.dao.lookup.RequirementStatusDAO;
import org.bocogop.wr.persistence.dao.lookup.StaffTitleDAO;
import org.bocogop.wr.persistence.dao.lookup.StdCreditCardTypeDAO;
import org.bocogop.wr.persistence.dao.lookup.StdVAVSTitleDAO;
import org.bocogop.wr.persistence.dao.lookup.TransportationMethodDAO;
import org.bocogop.wr.persistence.dao.lookup.VoluntaryServiceTypeDAO;
import org.bocogop.wr.persistence.dao.lookup.VolunteerStatusDAO;
import org.bocogop.wr.persistence.dao.organization.OrganizationDAO;
import org.bocogop.wr.persistence.dao.requirement.RequirementDAO;
import org.bocogop.wr.persistence.dao.requirement.VolunteerRequirementDAO;
import org.bocogop.wr.persistence.dao.views.CombinedFacilityDAO;
import org.bocogop.wr.persistence.dao.volunteer.VoluntaryServiceStaffDAO;
import org.bocogop.wr.persistence.dao.volunteer.VolunteerAssignmentDAO;
import org.bocogop.wr.persistence.dao.volunteer.VolunteerDAO;
import org.bocogop.wr.persistence.dao.volunteer.VolunteerHistoryEntryDAO;
import org.bocogop.wr.persistence.dao.volunteer.VolunteerOrganizationDAO;
import org.bocogop.wr.persistence.dao.volunteer.demographics.VolDemoDAO;
import org.bocogop.wr.service.AdjustedHoursEntryService;
import org.bocogop.wr.service.AwardCodeService;
import org.bocogop.wr.service.AwardService;
import org.bocogop.wr.service.BenefitingServiceRoleService;
import org.bocogop.wr.service.BenefitingServiceRoleTemplateService;
import org.bocogop.wr.service.BenefitingServiceService;
import org.bocogop.wr.service.BenefitingServiceTemplateService;
import org.bocogop.wr.service.DonGenPostFundService;
import org.bocogop.wr.service.DonationLogService;
import org.bocogop.wr.service.DonationReferenceService;
import org.bocogop.wr.service.DonationService;
import org.bocogop.wr.service.DonorService;
import org.bocogop.wr.service.ExcludedEntityService;
import org.bocogop.wr.service.ExpenditureService;
import org.bocogop.wr.service.FacilityService;
import org.bocogop.wr.service.KioskService;
import org.bocogop.wr.service.LedgerAdjustmentService;
import org.bocogop.wr.service.LetterTemplateService;
import org.bocogop.wr.service.LocationService;
import org.bocogop.wr.service.MealTicketService;
import org.bocogop.wr.service.NationalOfficialService;
import org.bocogop.wr.service.NotificationService;
import org.bocogop.wr.service.OrganizationService;
import org.bocogop.wr.service.ParkingStickerService;
import org.bocogop.wr.service.RequirementService;
import org.bocogop.wr.service.ServiceParametersService;
import org.bocogop.wr.service.StaffTitleService;
import org.bocogop.wr.service.UniformService;
import org.bocogop.wr.service.VoluntaryServiceStaffService;
import org.bocogop.wr.service.email.EmailService;
import org.bocogop.wr.service.requirement.VolunteerRequirementService;
import org.bocogop.wr.service.volunteer.VolunteerService;
import org.bocogop.wr.service.workEntry.OccasionalWorkEntryService;
import org.bocogop.wr.service.workEntry.WorkEntryService;
import org.bocogop.wr.util.DateUtil;
import org.bocogop.wr.web.ajax.AjaxRequestHandler;
import org.bocogop.wr.web.validation.WebValidationService;

public abstract class AbstractCommonAppController {
	private static final Logger log = LoggerFactory.getLogger(AbstractCommonAppController.class);

	// ----------------------------------------- Static Fields and Constants

	public static final String DEFAULT_COMMAND_NAME = "command";

	public static final String FORM_READ_ONLY = "FORM_READ_ONLY";

	// ----------------------------------------- Fields

	// -------------------------- DAOs

	@Autowired
	protected AdjustedHoursEntryDAO adjustedHoursEntryDAO;
	@Autowired
	protected AdministrativeUnitDAO administrativeUnitDAO;
	@Autowired
	protected ApplicationParametersDAO applicationParameterDAO;
	@Autowired
	protected AppUserFacilityDAO appUserFacilityDAO;
	@Autowired
	protected AppUserDAO appUserDAO;
	@Autowired
	protected AwardDAO awardDAO;
	@Autowired
	protected AwardCodeDAO awardCodeDAO;
	@Autowired
	protected BenefitingServiceDAO benefitingServiceDAO;
	@Autowired
	protected BenefitingServiceRoleDAO benefitingServiceRoleDAO;
	@Autowired
	protected BenefitingServiceRoleTypeDAO benefitingServiceRoleTypeDAO;
	@Autowired
	protected BenefitingServiceTemplateDAO benefitingServiceTemplateDAO;
	@Autowired
	protected BenefitingServiceRoleTemplateDAO benefitingServiceRoleTemplateDAO;
	@Autowired
	protected CombinedFacilityDAO combinedInstitutionDAO;
	@Autowired
	protected DonationDetailDAO donationDetailDAO;
	@Autowired
	protected DonationLogDAO donationLogDAO;
	@Autowired
	protected DonationLogService donationLogService;
	@Autowired
	protected DonationSummaryDAO donationSummaryDAO;
	@Autowired
	protected DonationReferenceDAO donationReferenceDAO;
	@Autowired
	protected DonationTypeDAO donationTypeDAO;
	@Autowired
	protected DonGenPostFundDAO donGenPostFundDAO;
	@Autowired
	protected DonorDAO donorDAO;
	@Autowired
	protected DonorTypeDAO donorTypeDAO;
	@Autowired
	protected ExcludedEntityDAO excludedEntityDAO;
	@Autowired
	protected ExpenditureDAO expenditureDAO;
	@Autowired
	protected FacilityDAO facilityDAO;
	@Autowired
	protected FacilityTypeDAO facilityTypeDAO;
	@Autowired
	protected GenderDAO genderDAO;
	@Autowired
	protected KioskDAO kioskDAO;
	@Autowired
	protected LanguageDAO languageDAO;
	@Autowired
	protected LedgerAdjustmentDAO ledgerAdjustmentDAO;
	@Autowired
	protected LetterTemplateDAO letterTemplateDAO;
	@Autowired
	protected LocationDAO locationDAO;
	@Autowired
	protected MealTicketDAO mealTicketDAO;
	@Autowired
	protected NACStatusDAO nacStatusDAO;
	@Autowired
	protected NationalOfficialDAO nationalOfficialDAO;
	@Autowired
	protected NotificationDAO notificationDAO;
	@Autowired
	protected OccasionalWorkEntryDAO occasionalWorkEntryDAO;
	@Autowired
	protected OrganizationDAO organizationDAO;
	@Autowired
	protected OrganizationTypeDAO organizationTypeDAO;
	@Autowired
	protected ParkingStickerDAO parkingStickerDAO;
	@Autowired
	protected RequirementDAO requirementDAO;
	@Autowired
	protected RequirementStatusDAO requirementStatusDAO;
	@Autowired
	protected RequirementDateTypeDAO requirementDateTypeDAO;
	@Autowired
	protected StateDAO stateDAO;
	@Autowired
	protected StdCreditCardTypeDAO stdCreditCardTypeDAO;
	@Autowired
	protected StdVAVSTitleDAO stdVAVSTitleDAO;
	@Autowired
	protected StaffTitleDAO staffTitleDAO;
	@Autowired
	protected TransportationMethodDAO transportationMethodDAO;
	@Autowired
	protected UniformDAO uniformDAO;
	@Autowired
	protected VAFacilityDAO vaFacilityDAO;
	@Autowired
	protected VoluntaryServiceStaffDAO voluntaryServiceStaffDAO;
	@Autowired
	protected VoluntaryServiceTypeDAO voluntaryServiceTypeDAO;
	@Autowired
	protected VolunteerDAO volunteerDAO;
	@Autowired
	protected VolunteerAssignmentDAO volunteerAssignmentDAO;
	@Autowired
	protected VolDemoDAO volunteerDemographicsDAO;
	@Autowired
	protected VolunteerHistoryEntryDAO volunteerHistoryEntryDAO;
	@Autowired
	protected VolunteerOrganizationDAO volunteerOrganizationDAO;
	@Autowired
	protected VolunteerRequirementDAO volunteerRequirementDAO;
	@Autowired
	protected VolunteerStatusDAO volunteerStatusDAO;
	@Autowired
	protected WorkEntryDAO workEntryDAO;

	// -------------------------- Services

	@Autowired
	protected AwardService awardService;
	@Autowired
	protected AwardCodeService awardCodeService;
	@Autowired
	protected AdjustedHoursEntryService adjustedHoursEntryService;
	@Autowired
	protected AppUserService appUserService;
	@Autowired
	protected BenefitingServiceService benefitingServiceService;
	@Autowired
	protected BenefitingServiceTemplateService benefitingServiceTemplateService;
	@Autowired
	protected BenefitingServiceRoleService benefitingServiceRoleService;
	@Autowired
	protected BenefitingServiceRoleTemplateService benefitingServiceRoleTemplateService;
	@Autowired
	protected DonationService donationService;
	@Autowired
	protected DonationReferenceService donationReferenceService;
	@Autowired
	protected DonGenPostFundService donGenPostFundService;
	@Autowired
	protected DonorService donorService;
	@Autowired
	protected EmailService emailService;
	@Autowired
	protected ExcludedEntityService excludedEntityService;
	@Autowired
	protected ExpenditureService expenditureService;
	@Autowired
	protected FacilityService facilityService;
	@Autowired
	protected KioskService kioskService;
	@Autowired
	protected LedgerAdjustmentService ledgerAdjustmentService;
	@Autowired
	protected LetterTemplateService letterTemplateService;
	@Autowired
	protected LocationService locationService;
	@Autowired
	protected MealTicketService mealTicketService;
	@Autowired
	protected NationalOfficialService nationalOfficialService;
	@Autowired
	protected NotificationService notificationService;
	@Autowired
	protected OccasionalWorkEntryService occasionalWorkEntryService;
	@Autowired
	protected OrganizationService organizationService;
	@Autowired
	protected ParkingStickerService parkingStickerService;
	@Autowired
	protected RequirementService requirementService;
	@Autowired
	protected StaffTitleService staffTitleService;
	@Autowired
	protected VolunteerRequirementService volunteerRequirementService;
	@Autowired
	protected ServiceParametersService voluntaryServiceParametersService;
	@Autowired
	protected UniformService uniformService;
	@Autowired
	protected VAFacilityService vaFacilityService;
	@Autowired
	protected VoluntaryServiceStaffService voluntaryServiceStaffService;
	@Autowired
	protected VolunteerService volunteerService;
	@Autowired
	protected WorkEntryService workEntryService;

	// -------------------------- Others

	@Autowired
	protected AjaxRequestHandler ajaxRequestHandler;
	@Autowired
	protected DateUtil dateUtil;
	@Autowired
	protected org.bocogop.wr.util.context.SessionUtil sessionUtil;
	@Autowired
	protected Environment env;
	@Autowired
	protected MessageSource messageSource;
	@Autowired
	protected WebValidationService webValidationService;

	@Autowired
	protected UserNotifier userNotifier;

	protected boolean isAjax(HttpServletRequest r) {
		return AjaxRequestHandler.isAjax(r);
	}

	protected String getMessage(String code) {
		return getMessage(code, new Object[] {});
	}

	protected String getMessage(String code, Object[] args) {
		return messageSource.getMessage(code, args, Locale.getDefault());
	}

	@ExceptionHandler(Throwable.class)
	public ModelAndView processError(Throwable ex, HttpServletRequest request, HttpServletResponse response) {
		if (isAjax(request)) {
			/*
			 * Necessary to trigger the jQuery error() handler as opposed to the
			 * success() handler - CPB
			 */
			response.setStatus(HttpStatus.BAD_REQUEST.value());
			return ajaxRequestHandler.getExceptionModelAndView(ex, request);
		} else {
			return new ModelAndView("error", "exceptionStackTrace", ExceptionUtils.getFullStackTrace(ex));
		}
	}

	// ----------------------------------------- Utility Methods

	protected void requirePermissionsAtFacility(long vaFacilityId, PermissionType... requiredPermissionsAtStation) {
		if (ArrayUtils.isNotEmpty(requiredPermissionsAtStation)) {
			SecurityUtil.ensureAllPermissionsAtFacility(vaFacilityId, requiredPermissionsAtStation);
		}
	}

	protected void requirePermissionsAtCurrentFacility(PermissionType... requiredPermissionsAtFacility) {
		if (ArrayUtils.isNotEmpty(requiredPermissionsAtFacility)) {
			SecurityUtil.ensureAllPermissionsAtCurrentFacility(requiredPermissionsAtFacility);
		}
	}

	protected void setFormAsReadOnly(ModelMap model, boolean readOnly) {
		model.put(FORM_READ_ONLY, readOnly);
	}

	protected boolean formIsReadOnly(ModelMap model) {
		Boolean b = (Boolean) model.get(FORM_READ_ONLY);
		return b != null && b;
	}

	protected void setFormAsReadOnlyUnlessUserHasPermissions(ModelMap model, PermissionType... permissions) {
		boolean userHasPermissions = SecurityUtil.hasAllPermissionsAtCurrentFacility(permissions);
		setFormAsReadOnly(model, !userHasPermissions);
	}

	protected Locale getLocale(HttpServletRequest portletRequest) {
		HttpServletRequest request = ServletUtil.getThreadBoundServletRequest();
		Locale locale = request == null ? Locale.getDefault() : RequestContextUtils.getLocale(request);
		return locale;
	}

	protected String getCurrentUserName() {
		return SecurityUtil.getCurrentUserName();
	}

	protected VAFacility getSiteContext() {
		return SessionUtil.getSiteContext();
	}

	protected VAFacility getRequiredSiteContext() {
		VAFacility siteContext = getSiteContext();
		if (siteContext == null)
			throw new IllegalArgumentException("Site context required in this controller but not found");
		return siteContext;
	}

	protected void setFacilityContext(Facility f) {
		sessionUtil.setFacilityContext(f.getVaFacility(), f);
	}

	protected Long getFacilityContextId() {
		Facility f = org.bocogop.wr.util.context.SessionUtil.getFacilityContext();
		return f == null ? null : f.getId();
	}

	protected Facility getFacilityContext() {
		Facility f = org.bocogop.wr.util.context.SessionUtil.getFacilityContext();
		if (f == null) {
			VAFacility siteContext = SessionUtil.getSiteContext();
			if (siteContext == null)
				return null;

			f = facilityDAO.findByVAFacility(siteContext.getId());
			if (f == null)
				throw new IllegalStateException(
						"No Facility is currently linked to the VA site " + siteContext.getStationNumber()
								+ "; please contact support to remedy this via the Facility Management screen.");
			setFacilityContext(f);
			return f;
		} else {
			// reattach
			f = facilityDAO.findRequiredByPrimaryKey(f.getId());
			return f;
		}
	}

	protected Facility getRequiredFacilityContext() {
		Facility f = getFacilityContext();
		if (f == null)
			throw new IllegalArgumentException("Required facility context was not found");
		return f;
	}

	protected LocalDate getTodayAtFacility() {
		return LocalDate.now(getFacilityTimeZone());
	}

	protected ZoneId getFacilityTimeZone() {
		Facility facilityContext = getFacilityContext();
		return facilityContext == null ? ZoneId.systemDefault() : facilityContext.getTimeZone();
	}

}
