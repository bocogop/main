package org.bocogop.wr;

import javax.security.auth.login.LoginContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import org.bocogop.shared.model.core.IdentifiedPersistent;
import org.bocogop.shared.persistence.lookup.sds.StateDAO;
import org.bocogop.shared.test.AbstractTransactionalDAOTest;
import org.bocogop.wr.config.WebAppConfig;
import org.bocogop.wr.config.testOnly.AppTestConfig;
import org.bocogop.wr.model.facility.Facility;
import org.bocogop.wr.persistence.dao.DonationLogDAO;
import org.bocogop.wr.persistence.dao.ExpenditureDAO;
import org.bocogop.wr.persistence.dao.LedgerAdjustmentDAO;
import org.bocogop.wr.persistence.dao.NationalOfficialDAO;
import org.bocogop.wr.persistence.dao.NotificationDAO;
import org.bocogop.wr.persistence.dao.OccasionalWorkEntryDAO;
import org.bocogop.wr.persistence.dao.WorkEntryDAO;
import org.bocogop.wr.persistence.dao.benefitingService.BenefitingServiceDAO;
import org.bocogop.wr.persistence.dao.benefitingService.BenefitingServiceRoleDAO;
import org.bocogop.wr.persistence.dao.benefitingService.BenefitingServiceRoleTemplateDAO;
import org.bocogop.wr.persistence.dao.benefitingService.BenefitingServiceRoleTypeDAO;
import org.bocogop.wr.persistence.dao.benefitingService.BenefitingServiceTemplateDAO;
import org.bocogop.wr.persistence.dao.donGenPostFund.DonGenPostFundDAO;
import org.bocogop.wr.persistence.dao.facility.FacilityDAO;
import org.bocogop.wr.persistence.dao.facility.LocationDAO;
import org.bocogop.wr.persistence.dao.lookup.FacilityTypeDAO;
import org.bocogop.wr.persistence.dao.lookup.LanguageDAO;
import org.bocogop.wr.persistence.dao.lookup.OrganizationTypeDAO;
import org.bocogop.wr.persistence.dao.lookup.StdVAVSTitleDAO;
import org.bocogop.wr.persistence.dao.organization.OrganizationDAO;
import org.bocogop.wr.persistence.dao.requirement.RequirementDAO;
import org.bocogop.wr.persistence.dao.views.FacilityAndVisnDAO;
import org.bocogop.wr.persistence.dao.volunteer.VoluntaryServiceStaffDAO;
import org.bocogop.wr.persistence.dao.volunteer.VolunteerAssignmentDAO;
import org.bocogop.wr.persistence.dao.volunteer.VolunteerDAO;
import org.bocogop.wr.test.util.TestObjectFactory;

@ContextConfiguration(classes = { WebAppConfig.class, AppTestConfig.class })
public abstract class AbstractTransactionalWebDAOTest<T extends IdentifiedPersistent>
		extends AbstractTransactionalDAOTest<T> {

	protected LoginContext vistaLoginContext = null;

	@Autowired
	protected BenefitingServiceDAO benefitingServiceDAO;
	@Autowired
	protected BenefitingServiceRoleDAO benefitingServiceRoleDAO;
	@Autowired
	protected BenefitingServiceTemplateDAO benefitingServiceTemplateDAO;
	@Autowired
	protected BenefitingServiceRoleTemplateDAO benefitingServiceRoleTemplateDAO;
	@Autowired
	protected BenefitingServiceRoleTypeDAO benefitingServiceRoleTypeDAO;
	@Autowired
	protected DonationLogDAO donationLogDAO;
	@Autowired
	protected DonGenPostFundDAO donGenPostFundDAO;
	@Autowired
	protected ExpenditureDAO expenditureDAO;
	@Autowired
	protected FacilityDAO facilityDAO;
	@Autowired
	protected FacilityTypeDAO facilityTypeDAO;
	@Autowired
	protected FacilityAndVisnDAO facilityAndVISNDAO;
	@Autowired
	protected LanguageDAO languageDAO;
	@Autowired
	protected LedgerAdjustmentDAO ledgerAdjustmentDAO;
	@Autowired
	protected LocationDAO locationDAO;
	@Autowired
	protected NationalOfficialDAO nationalOfficalDAO;
	@Autowired
	protected NotificationDAO notificationDAO;
	@Autowired
	protected OccasionalWorkEntryDAO occasionalWorkEntryDAO;
	@Autowired
	protected OrganizationDAO organizationDAO;
	@Autowired
	protected OrganizationTypeDAO organizationTypeDAO;
	@Autowired
	protected RequirementDAO requirementDAO;
	@Autowired
	protected StateDAO stateDAO;
	@Autowired
	protected StdVAVSTitleDAO stdVAVSTitleDAO;
	@Autowired
	protected VolunteerAssignmentDAO volunteerAssignmentDAO;
	@Autowired
	protected VoluntaryServiceStaffDAO voluntaryServiceStaffDAO;
	@Autowired
	protected VolunteerDAO volunteerDAO;
	@Autowired
	protected WorkEntryDAO workEntryDAO;

	@Autowired
	protected TestObjectFactory testObjectFactory;

	protected Facility getFacility() {
		return facilityDAO.findByStationNumber(TEST_STATION_NUMBER);
	}

}
