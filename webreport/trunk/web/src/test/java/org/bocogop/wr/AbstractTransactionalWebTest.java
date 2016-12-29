package org.bocogop.wr;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.PlatformTransactionManager;

import org.bocogop.shared.persistence.AppUserDAO;
import org.bocogop.shared.persistence.AppUserFacilityDAO;
import org.bocogop.shared.persistence.lookup.sds.GenderDAO;
import org.bocogop.shared.persistence.lookup.sds.StateDAO;
import org.bocogop.shared.persistence.lookup.sds.VAFacilityDAO;
import org.bocogop.shared.service.VAFacilityService;
import org.bocogop.shared.test.AbstractTransactionalAppTest;
import org.bocogop.wr.config.WebAppConfig;
import org.bocogop.wr.config.testOnly.AppTestConfig;
import org.bocogop.wr.persistence.dao.ApplicationParametersDAO;
import org.bocogop.wr.persistence.dao.AwardDAO;
import org.bocogop.wr.persistence.dao.DonationDetailDAO;
import org.bocogop.wr.persistence.dao.DonationReferenceDAO;
import org.bocogop.wr.persistence.dao.DonationSummaryDAO;
import org.bocogop.wr.persistence.dao.DonorDAO;
import org.bocogop.wr.persistence.dao.LetterTemplateDAO;
import org.bocogop.wr.persistence.dao.NationalOfficialDAO;
import org.bocogop.wr.persistence.dao.OccasionalWorkEntryDAO;
import org.bocogop.wr.persistence.dao.ParkingStickerDAO;
import org.bocogop.wr.persistence.dao.PrintRequestDAO;
import org.bocogop.wr.persistence.dao.UniformDAO;
import org.bocogop.wr.persistence.dao.WorkEntryDAO;
import org.bocogop.wr.persistence.dao.benefitingService.BenefitingServiceDAO;
import org.bocogop.wr.persistence.dao.benefitingService.BenefitingServiceRoleDAO;
import org.bocogop.wr.persistence.dao.benefitingService.BenefitingServiceRoleTemplateDAO;
import org.bocogop.wr.persistence.dao.benefitingService.BenefitingServiceRoleTypeDAO;
import org.bocogop.wr.persistence.dao.benefitingService.BenefitingServiceTemplateDAO;
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
import org.bocogop.wr.persistence.dao.lookup.StaffTitleDAO;
import org.bocogop.wr.persistence.dao.lookup.StdCreditCardTypeDAO;
import org.bocogop.wr.persistence.dao.lookup.StdVAVSTitleDAO;
import org.bocogop.wr.persistence.dao.lookup.TransportationMethodDAO;
import org.bocogop.wr.persistence.dao.lookup.VoluntaryServiceTypeDAO;
import org.bocogop.wr.persistence.dao.organization.OrganizationDAO;
import org.bocogop.wr.persistence.dao.requirement.RequirementDAO;
import org.bocogop.wr.persistence.dao.views.CombinedFacilityDAO;
import org.bocogop.wr.persistence.dao.views.FacilityAndVisnDAO;
import org.bocogop.wr.persistence.dao.volunteer.VoluntaryServiceStaffDAO;
import org.bocogop.wr.persistence.dao.volunteer.VolunteerAssignmentDAO;
import org.bocogop.wr.persistence.dao.volunteer.VolunteerDAO;
import org.bocogop.wr.persistence.dao.volunteer.VolunteerOrganizationDAO;
import org.bocogop.wr.test.util.TestObjectFactory;

@ContextConfiguration(classes = { WebAppConfig.class, AppTestConfig.class })
public abstract class AbstractTransactionalWebTest extends AbstractTransactionalAppTest {

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
	protected DonationSummaryDAO donationSummaryDAO;
	@Autowired
	protected DonationReferenceDAO donationReferenceDAO;
	@Autowired
	protected DonationTypeDAO donationTypeDAO;
	@Autowired
	protected DonorDAO donorDAO;
	@Autowired
	protected DonorTypeDAO donorTypeDAO;
	@Autowired
	protected ExcludedEntityDAO excludedEntityDAO;
	@Autowired
	protected FacilityAndVisnDAO facilityAndVISNDAO;
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
	protected LetterTemplateDAO letterTemplateDAO;
	@Autowired
	protected LocationDAO locationDAO;
	@Autowired
	protected NACStatusDAO nacStatusDAO;
	@Autowired
	protected NationalOfficialDAO nationalOfficialDAO;
	@Autowired
	protected OccasionalWorkEntryDAO occasionalWorkEntryDAO;
	@Autowired
	protected OrganizationDAO organizationDAO;
	@Autowired
	protected OrganizationTypeDAO organizationTypeDAO;
	@Autowired
	protected ParkingStickerDAO parkingStickerDAO;
	@Autowired
	protected PrintRequestDAO printRequestDAO;
	@Autowired
	protected RequirementDAO requirementDAO;
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
	protected VolunteerDAO volunteerDAO;
	@Autowired
	protected VolunteerAssignmentDAO volunteerAssignmentDAO;
	@Autowired
	protected VolunteerOrganizationDAO volunteerOrganizationDAO;
	@Autowired
	protected VoluntaryServiceTypeDAO voluntaryServiceTypeDAO;
	@Autowired
	protected WorkEntryDAO workEntryDAO;

	@Autowired
	protected VAFacilityService vaFacilityService;

	@Autowired
	protected DataSource dataSource;
	@Autowired
	protected TestObjectFactory testObjectFactory;

	@Autowired
	protected MessageSource messageSource;

	@PersistenceContext
	protected EntityManager em;

	@Autowired
	@Qualifier("transactionManager")
	protected PlatformTransactionManager tm;

}
