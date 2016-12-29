package org.bocogop.wr.service.impl;

import java.time.LocalDate;
import java.time.ZoneId;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;

import org.bocogop.shared.model.AppUser;
import org.bocogop.shared.model.CoreUserDetails;
import org.bocogop.shared.model.lookup.sds.VAFacility;
import org.bocogop.shared.persistence.AppUserDAO;
import org.bocogop.shared.persistence.lookup.PermissionDAO;
import org.bocogop.shared.persistence.lookup.RoleDAO;
import org.bocogop.shared.persistence.lookup.sds.GenderDAO;
import org.bocogop.shared.persistence.lookup.sds.StateDAO;
import org.bocogop.shared.persistence.lookup.sds.VAFacilityDAO;
import org.bocogop.shared.persistence.lookup.sds.VAFacilityTypeDAO;
import org.bocogop.shared.service.validation.ServiceValidationException;
import org.bocogop.shared.util.SecurityUtil;
import org.bocogop.wr.model.facility.Facility;
import org.bocogop.wr.persistence.dao.AdjustedHoursEntryDAO;
import org.bocogop.wr.persistence.dao.ApplicationParametersDAO;
import org.bocogop.wr.persistence.dao.AvailableIdentifyingCodeDAO;
import org.bocogop.wr.persistence.dao.AwardCodeDAO;
import org.bocogop.wr.persistence.dao.AwardDAO;
import org.bocogop.wr.persistence.dao.BinaryObjectDAO;
import org.bocogop.wr.persistence.dao.DonationDetailDAO;
import org.bocogop.wr.persistence.dao.DonationLogDAO;
import org.bocogop.wr.persistence.dao.DonationLogFileDAO;
import org.bocogop.wr.persistence.dao.DonationReferenceDAO;
import org.bocogop.wr.persistence.dao.DonationSummaryDAO;
import org.bocogop.wr.persistence.dao.DonorDAO;
import org.bocogop.wr.persistence.dao.ExpenditureDAO;
import org.bocogop.wr.persistence.dao.HolidayDAO;
import org.bocogop.wr.persistence.dao.LedgerAdjustmentDAO;
import org.bocogop.wr.persistence.dao.LetterTemplateDAO;
import org.bocogop.wr.persistence.dao.NationalOfficialDAO;
import org.bocogop.wr.persistence.dao.NotificationDAO;
import org.bocogop.wr.persistence.dao.OccasionalWorkEntryDAO;
import org.bocogop.wr.persistence.dao.ParkingStickerDAO;
import org.bocogop.wr.persistence.dao.PrintRequestDAO;
import org.bocogop.wr.persistence.dao.ServiceParametersDAO;
import org.bocogop.wr.persistence.dao.StationParametersDAO;
import org.bocogop.wr.persistence.dao.TemplateDAO;
import org.bocogop.wr.persistence.dao.UniformDAO;
import org.bocogop.wr.persistence.dao.WorkEntryDAO;
import org.bocogop.wr.persistence.dao.audit.AuditLogEntryDAO;
import org.bocogop.wr.persistence.dao.benefitingService.BenefitingServiceDAO;
import org.bocogop.wr.persistence.dao.benefitingService.BenefitingServiceRoleDAO;
import org.bocogop.wr.persistence.dao.benefitingService.BenefitingServiceRoleTemplateDAO;
import org.bocogop.wr.persistence.dao.benefitingService.BenefitingServiceRoleTypeDAO;
import org.bocogop.wr.persistence.dao.benefitingService.BenefitingServiceTemplateDAO;
import org.bocogop.wr.persistence.dao.donGenPostFund.DonGenPostFundDAO;
import org.bocogop.wr.persistence.dao.facility.FacilityDAO;
import org.bocogop.wr.persistence.dao.facility.KioskDAO;
import org.bocogop.wr.persistence.dao.facility.LocationDAO;
import org.bocogop.wr.persistence.dao.facility.UpdateableLocationDAO;
import org.bocogop.wr.persistence.dao.leie.ExcludedEntityDAO;
import org.bocogop.wr.persistence.dao.lookup.AdministrativeUnitDAO;
import org.bocogop.wr.persistence.dao.lookup.DonorTypeDAO;
import org.bocogop.wr.persistence.dao.lookup.FacilityTypeDAO;
import org.bocogop.wr.persistence.dao.lookup.LanguageDAO;
import org.bocogop.wr.persistence.dao.lookup.RequirementStatusDAO;
import org.bocogop.wr.persistence.dao.lookup.StaffTitleDAO;
import org.bocogop.wr.persistence.dao.lookup.VolunteerStatusDAO;
import org.bocogop.wr.persistence.dao.organization.OrganizationDAO;
import org.bocogop.wr.persistence.dao.requirement.BenefitingServiceRoleRequirementAssociationDAO;
import org.bocogop.wr.persistence.dao.requirement.BenefitingServiceRoleTemplateRequirementAssociationDAO;
import org.bocogop.wr.persistence.dao.requirement.RequirementDAO;
import org.bocogop.wr.persistence.dao.requirement.VolunteerRequirementDAO;
import org.bocogop.wr.persistence.dao.views.FacilityAndVisnDAO;
import org.bocogop.wr.persistence.dao.volunteer.VoluntaryServiceStaffDAO;
import org.bocogop.wr.persistence.dao.volunteer.VolunteerAssignmentDAO;
import org.bocogop.wr.persistence.dao.volunteer.VolunteerDAO;
import org.bocogop.wr.persistence.dao.volunteer.VolunteerOrganizationDAO;
import org.bocogop.wr.service.email.EmailService;
import org.bocogop.wr.util.DateUtil;
import org.bocogop.wr.util.context.SessionUtil;

@Transactional(rollbackFor = ServiceValidationException.class)
public class AbstractServiceImpl {

	@Autowired
	protected AdjustedHoursEntryDAO adjustedHoursEntryDAO;
	@Autowired
	protected AdministrativeUnitDAO administrativeUnitDAO;
	@Autowired
	protected ApplicationParametersDAO applicationParametersDAO;
	@Autowired
	protected AppUserDAO appUserDAO;
	@Autowired
	protected AuditLogEntryDAO auditLogEntryDAO;
	@Autowired
	protected AvailableIdentifyingCodeDAO availableIdentifyingCodeDAO;
	@Autowired
	protected AwardDAO awardDAO;
	@Autowired
	protected AwardCodeDAO awardCodeDAO;
	@Autowired
	protected BenefitingServiceDAO benefitingServiceDAO;
	@Autowired
	protected BenefitingServiceRoleDAO benefitingServiceRoleDAO;
	@Autowired
	protected BenefitingServiceRoleRequirementAssociationDAO benefitingServiceRoleRequirementAssociationDAO;
	@Autowired
	protected BenefitingServiceRoleTemplateDAO benefitingServiceRoleTemplateDAO;
	@Autowired
	protected BenefitingServiceRoleTemplateRequirementAssociationDAO benefitingServiceRoleTemplateRequirementAssociationDAO;
	@Autowired
	protected BenefitingServiceRoleTypeDAO benefitingServiceRoleTypeDAO;
	@Autowired
	protected BenefitingServiceTemplateDAO benefitingServiceTemplateDAO;
	@Autowired
	protected BinaryObjectDAO binaryObjectDAO;
	@Autowired
	protected DonationLogDAO donationLogDAO;
	@Autowired
	protected DonationLogFileDAO donationLogFileDAO;
	@Autowired
	protected DonationDetailDAO donationDetailDAO;
	@Autowired
	protected DonationSummaryDAO donationSummaryDAO;
	@Autowired
	protected DonationReferenceDAO donationReferenceDAO;
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
	protected HolidayDAO holidayDAO;
	@Autowired
	protected FacilityAndVisnDAO facilityAndVisnDAO;
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
	protected NationalOfficialDAO nationalOfficialDAO;
	@Autowired
	protected NotificationDAO notificationDAO;
	@Autowired
	protected OccasionalWorkEntryDAO occasionalWorkEntryDAO;
	@Autowired
	protected OrganizationDAO organizationDAO;
	@Autowired
	protected ParkingStickerDAO parkingStickerDAO;
	@Autowired
	protected PermissionDAO permissionDAO;
	@Autowired
	protected PrintRequestDAO printRequestDAO;
	@Autowired
	protected RequirementDAO requirementDAO;
	@Autowired
	protected RequirementStatusDAO requirementStatusDAO;
	@Autowired
	protected RoleDAO roleDAO;
	@Autowired
	protected ServiceParametersDAO serviceParametersDAO;
	@Autowired
	protected StaffTitleDAO staffTitleDAO;
	@Autowired
	protected StateDAO stateDAO;
	@Autowired
	protected StationParametersDAO stationParametersDAO;
	@Autowired
	protected TemplateDAO templateDAO;
	@Autowired
	protected UniformDAO uniformDAO;
	@Autowired
	protected UpdateableLocationDAO updateableLocationDAO;
	@Autowired
	protected VAFacilityDAO vaFacilityDAO;
	@Autowired
	protected VAFacilityTypeDAO vaFacilityTypeDAO;
	@Autowired
	protected VoluntaryServiceStaffDAO voluntaryServiceStaffDAO;
	@Autowired
	protected VolunteerDAO volunteerDAO;
	@Autowired
	protected VolunteerAssignmentDAO volunteerAssignmentDAO;
	@Autowired
	protected VolunteerOrganizationDAO volunteerOrganizationDAO;
	@Autowired
	protected VolunteerRequirementDAO volunteerRequirementDAO;
	@Autowired
	protected VolunteerStatusDAO volunteerStatusDAO;
	@Autowired
	protected WorkEntryDAO workEntryDAO;

	@Autowired
	protected SessionUtil sessionUtil;
	@Autowired
	protected DateUtil dateUtil;
	@Autowired
	protected EmailService emailService;
	@Autowired
	protected MessageSource messageSource;
	@Autowired
	@Qualifier("transactionManager")
	protected PlatformTransactionManager tm;

	protected VAFacility getSiteContext() {
		return org.bocogop.shared.util.context.SessionUtil.getSiteContext();
	}

	protected VAFacility getRequiredSiteContext() {
		VAFacility f = getSiteContext();
		if (f == null)
			throw new IllegalStateException("A site context was required but not found.");
		return f;
	}

	protected Facility getFacilityContext() {
		VAFacility siteContext = getSiteContext();
		if (siteContext == null)
			return null;

		Facility f = facilityDAO.findByVAFacility(siteContext.getId());
		return f;
	}

	protected Facility getRequiredFacilityContext() {
		Facility f = getFacilityContext();
		if (f == null)
			throw new IllegalStateException("A facility context was required but not found.");
		return f;
	}

	protected ZoneId getFacilityTimeZone() {
		Facility fc = getFacilityContext();
		return fc == null ? ZoneId.systemDefault() : fc.getTimeZone();
	}

	protected LocalDate getTodayAtFacility() {
		return LocalDate.now(getFacilityTimeZone());
	}

	/**
	 * Returns the current AppUser, or null if there is no current user or the
	 * current user is not an AppUser (e.g. is a background daemon user). See
	 * SecurityUtil.getCurrentUserAsOrNull for details. CPB
	 */
	protected CoreUserDetails getCurrentUser() {
		return SecurityUtil.getCurrentUserAsOrNull(CoreUserDetails.class);
	}

	protected <T extends CoreUserDetails> T getCurrentUserAsOrNull(Class<T> c) {
		return SecurityUtil.getCurrentUserAsOrNull(c);
	}

	protected AppUser getBatchJobUser() {
		AppUser appUser = new AppUser();
		appUser.setEnabled(true);
		appUser.setLastName("System Batch Job");
		appUser.setTimeZone(ZoneId.systemDefault());
		return appUser;
	}
}
