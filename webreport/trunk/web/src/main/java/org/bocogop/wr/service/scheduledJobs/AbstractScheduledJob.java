package org.bocogop.wr.service.scheduledJobs;

import java.util.Collection;
import java.util.concurrent.Callable;

import org.apache.commons.collections15.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.PlatformTransactionManager;

import org.bocogop.shared.model.AppUser;
import org.bocogop.shared.model.Permission.PermissionType;
import org.bocogop.shared.model.Role.RoleType;
import org.bocogop.shared.persistence.AppUserDAO;
import org.bocogop.shared.persistence.LdapPersonDAO;
import org.bocogop.shared.persistence.lookup.sds.VAFacilityDAO;
import org.bocogop.shared.service.AppUserService;
import org.bocogop.shared.util.SecurityUtil;
import org.bocogop.wr.model.SystemUserDetails;
import org.bocogop.wr.model.facility.Facility;
import org.bocogop.wr.persistence.dao.ApplicationParametersDAO;
import org.bocogop.wr.persistence.dao.DonationLogFileDAO;
import org.bocogop.wr.persistence.dao.PrintRequestDAO;
import org.bocogop.wr.persistence.dao.facility.FacilityDAO;
import org.bocogop.wr.persistence.dao.facility.KioskDAO;
import org.bocogop.wr.persistence.dao.views.FacilityAndVisnDAO;
import org.bocogop.wr.service.DonationLogService;
import org.bocogop.wr.service.ExcludedEntityService;
import org.bocogop.wr.service.KioskService;
import org.bocogop.wr.service.NotificationService;
import org.bocogop.wr.service.PrintRequestService;
import org.bocogop.wr.service.requirement.VolunteerRequirementService;
import org.bocogop.wr.service.volunteer.VolunteerService;
import org.bocogop.wr.util.context.BasicContextManager;
import org.bocogop.wr.util.context.SessionUtil;

public abstract class AbstractScheduledJob {

	@Autowired
	protected ApplicationParametersDAO applicationParametersDAO;
	@Autowired
	protected AppUserDAO appUserDAO;
	@Autowired
	protected AppUserService appUserService;
	@Autowired
	protected DonationLogService donationLogService;
	@Autowired
	protected DonationLogFileDAO donationLogFileDAO;
	@Autowired
	protected FacilityAndVisnDAO institutionAndVisnDAO;
	@Autowired
	protected FacilityDAO facilityDAO;
	@Autowired
	protected ExcludedEntityService excludedEntityService;
	@Autowired
	protected LdapPersonDAO ldapPersonDAO;
	@Autowired
	protected NotificationService notificationService;
	@Autowired
	protected KioskDAO kioskDAO;
	@Autowired
	protected KioskService kioskService;
	@Autowired
	protected PrintRequestDAO printRequestDAO;
	@Autowired
	protected PrintRequestService printRequestService;
	@Autowired
	protected VAFacilityDAO vAFacilityDAO;
	@Autowired
	protected VolunteerService volunteerService;
	@Autowired
	protected VolunteerRequirementService volunteerRequirementService;

	@Autowired
	@Qualifier("transactionManager")
	protected PlatformTransactionManager tm;

	protected AppUser queryUser() {
		return appUserDAO.findByUsername(SecurityUtil.getCurrentUserName(), false);
	}

	protected void runAsBatchJobUser(Callable<?> c) throws Exception {
		Collection<GrantedAuthority> allRolesAndPermissions = CollectionUtils
				.union(PermissionType.getAllAsGrantedAuthorities(), RoleType.getAllAsGrantedAuthorities());
		SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
				new SystemUserDetails(allRolesAndPermissions), "unimportant", allRolesAndPermissions));

		Facility station101 = facilityDAO.findByStationNumber("101");
		BasicContextManager contextManager = new BasicContextManager(station101, station101.getDisplayName(), 0, true);

		SessionUtil.runWithContext(c, contextManager);
	}

}
