package org.bocogop.wr.web.organization;

import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
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

import com.fasterxml.jackson.annotation.JsonView;

import org.bocogop.shared.model.Permission;
import org.bocogop.shared.model.Permission.PermissionType;
import org.bocogop.shared.model.lookup.sds.State;
import org.bocogop.shared.service.validation.ServiceValidationException;
import org.bocogop.shared.util.SecurityUtil;
import org.bocogop.shared.util.StringUtil;
import org.bocogop.wr.model.donation.DonationLog;
import org.bocogop.wr.model.donation.Donor;
import org.bocogop.wr.model.facility.Facility;
import org.bocogop.wr.model.organization.AbstractBasicOrganization;
import org.bocogop.wr.model.organization.AbstractBasicOrganization.OrganizationView;
import org.bocogop.wr.model.organization.NationalOfficial;
import org.bocogop.wr.model.organization.Organization;
import org.bocogop.wr.model.organization.OrganizationBranch;
import org.bocogop.wr.model.organization.OrganizationBranch.OrganizationBranchView;
import org.bocogop.wr.model.organization.ScopeType;
import org.bocogop.wr.model.organization.StdVAVSTitle;
import org.bocogop.wr.persistence.dao.organization.OrgQuickSearchResult;
import org.bocogop.wr.persistence.queryCustomization.QueryCustomization;
import org.bocogop.wr.util.DateUtil;
import org.bocogop.wr.web.AbstractAppController;
import org.bocogop.wr.web.breadcrumbs.Breadcrumb;
import org.bocogop.wr.web.donation.DonationController;
import org.bocogop.wr.web.donation.DonorController;
import org.bocogop.wr.web.donation.DonorSearchParams;
import org.bocogop.wr.web.validation.ValidationException;

@Controller
@SessionAttributes(value = { AbstractAppController.DEFAULT_COMMAND_NAME })
public class OrganizationController extends AbstractAppController {

	@Autowired
	private OrganizationValidator organizationValidator;
	@Value("${organizationSearch.maxResults}")
	private int maxResults;
	@Value("${organizationQuickSearch.maxResults}")
	private int orgQuickSearchMaxResults;

	@RequestMapping("/organizationCreate.htm")
	@Breadcrumb("Create Organization")
	@PreAuthorize("hasAnyAuthority('" + Permission.ORG_CODE_NATIONAL_CREATE + ", " + Permission.ORG_CODE_LOCAL_CREATE
			+ "')")
	public String createOrganization(ModelMap model, HttpServletRequest request,
			@RequestParam(required = false) String fromPage) {
		Organization org = new Organization();

		if ("donor".equals(fromPage)) {
			DonorSearchParams params = (DonorSearchParams) request.getSession()
					.getAttribute(DonorController.DONOR_SEARCH_PARAMS);

			if (params != null) {
				org.setName(params.getOrgName());
				// clear out the search params that's carried via http session
				request.getSession().removeAttribute(DonorController.DONOR_SEARCH_PARAMS);
			}

			// If session contains donationLogId, then we are trying to add
			// edonation, repopulate add donor fields
			// with edonation data
			Long donationLogId = (Long) request.getSession().getAttribute(DonationController.DONATION_DEFAULTS);
			DonationLog donationLog = null;
			if (donationLogId != null)
				donationLog = donationLogDAO.findByPrimaryKey(donationLogId);

			if (donationLog != null) {
				org.setName(donationLog.getName());
				org.setAddressLine1(donationLog.getAddress());
				org.setCity(donationLog.getCity());
				org.setState(stateDAO.findStateByPostalCode(donationLog.getState()));
				org.setEmail(donationLog.getEmail());
				org.setPhone(donationLog.getPhone());
				org.setZip(donationLog.getZip());
			}
		}

		if (SecurityUtil.hasAllPermissionsAtCurrentFacility(PermissionType.ORG_CODE_NATIONAL_CREATE)) {
			if (isUserWorkingFacilityCO()) {
				org.setScope(ScopeType.NATIONAL);
			} else {
				setOrgAsLocal(org);
			}
		} else if (SecurityUtil.hasAllPermissionsAtCurrentFacility(PermissionType.ORG_CODE_LOCAL_CREATE)) {
			setOrgAsLocal(org);
		} else {
			throw new SecurityException("The user does not have permission to create organizations");
		}

		OrganizationCommand command = new OrganizationCommand(org, fromPage);
		// set default status to active
		org.setInactive(false);

		if (org.getScope() == ScopeType.LOCAL) {
			org.setOnNationalAdvisoryCommittee(false);
		}

		model.addAttribute(DEFAULT_COMMAND_NAME, command);

		createReferenceData(model);

		return "createOrganization";
	}

	private boolean isUserWorkingFacilityCO() {
		return getRequiredSiteContext().isCentralOffice();
	}

	private void setOrgAsLocal(Organization org) {
		org.setScope(ScopeType.LOCAL);
		Facility facility = getRequiredFacilityContext();
		org.setFacility(facility);
	}

	@RequestMapping("/organizationEdit.htm")
	@Breadcrumb("Edit Organization")
	@PreAuthorize("hasAnyAuthority('" + Permission.ORG_CODE_NATIONAL_READ + ", " + Permission.ORG_CODE_LOCAL_READ
			+ "')")
	public String editOrganization(@RequestParam long id, @RequestParam(required = false) String fromPage,
			@RequestParam(required = false) Long branchLoad, ModelMap model, HttpServletRequest request) {
		AbstractBasicOrganization org = organizationDAO.findRequiredByPrimaryKey(id);
		if ("Branch".equals(org.getScale())) {
			if (org.getRootOrganization() == null || org.getRootOrganization().getId() == id)
				throw new RuntimeException("Invalid data detected with org " + id);

			String url = "redirect:/organizationEdit.htm?id=" + org.getRootOrganization().getId() + "&branchLoad=" + id;
			if (fromPage != null)
				url += "&fromPage=" + fromPage;

			return url;
		}

		OrganizationCommand command = new OrganizationCommand(org, fromPage);
		model.addAttribute(DEFAULT_COMMAND_NAME, command);

		model.addAttribute("branchLoad", branchLoad);

		if ("donor".equals(fromPage)) {
			Donor d = donorDAO.findByOrganizationFK(branchLoad != null ? branchLoad : id);
			model.addAttribute("targetSubmissionPage", "/donorEdit.htm?id=" + d.getId());
		} else {
			model.addAttribute("targetSubmissionPage", "/organizationEdit.htm?id=" + id);
		}

		createReferenceData(model);

		if (org.getScope() == ScopeType.NATIONAL) {
			if (isUserWorkingFacilityCO()) {
				setFormAsReadOnlyUnlessUserHasPermissions(model, PermissionType.ORG_CODE_NATIONAL_CREATE);
			} else {
				SecurityUtil.ensureAllPermissionsAtCurrentFacility(PermissionType.ORG_CODE_NATIONAL_READ);
				setFormAsReadOnly(model, true);
			}
		} else {
			if (org.getFacility() != null && getRequiredSiteContext().equals(org.getFacility().getVaFacility())) {
				setFormAsReadOnlyUnlessUserHasPermissions(model, PermissionType.ORG_CODE_LOCAL_CREATE);
			} else {
				SecurityUtil.ensureAllPermissionsAtCurrentFacility(PermissionType.ORG_CODE_LOCAL_READ);
				setFormAsReadOnly(model, true);
			}
		}

		return "editOrganization";
	}

	@RequestMapping("/organizationList.htm")
	@Breadcrumb("List Organizations")
	@PreAuthorize("hasAnyAuthority('" + Permission.ORG_CODE_NATIONAL_READ + ", " + Permission.ORG_CODE_LOCAL_READ
			+ "')")
	public String listOrganization(ModelMap model, HttpServletRequest request) {
		Long staId = getFacilityContextId();

		boolean includeNational = SecurityUtil
				.hasAllPermissionsAtCurrentFacility(PermissionType.ORG_CODE_NATIONAL_READ);
		boolean includeLocal = SecurityUtil.hasAllPermissionsAtCurrentFacility(PermissionType.ORG_CODE_LOCAL_READ);
		List<Long> stationsToInclude = new ArrayList<Long>();
		stationsToInclude.add(staId);
		List<AbstractBasicOrganization> organizations = organizationDAO.findByCriteria(null, includeNational,
				includeLocal, true, stationsToInclude, null, null, null, null);

		OrganizationListCommand command = new OrganizationListCommand(organizations);
		model.addAttribute(DEFAULT_COMMAND_NAME, command);
		return "organizationList";
	}

	private void createReferenceData(ModelMap model) {
		model.put("allTypes", organizationTypeDAO.findAllSorted());
		model.put("allNacStatuses", nacStatusDAO.findAll());
		model.put("allMonths", Month.values());
		model.put("allOfficialVAVSTitles", stdVAVSTitleDAO.findAll());
	}

	@RequestMapping("/organizationSubmit.htm")
	@PreAuthorize("hasAnyAuthority('" + Permission.ORG_CODE_NATIONAL_CREATE + ", " + Permission.ORG_CODE_LOCAL_CREATE
			+ "')")
	public String submitOrganization(@ModelAttribute(DEFAULT_COMMAND_NAME) OrganizationCommand command,
			BindingResult result, SessionStatus status, ModelMap model, HttpServletRequest request)
			throws ValidationException, ServiceValidationException {
		AbstractBasicOrganization org = command.getOrganization();

		boolean isEdit = org.isPersistent();

		/* Validation step (JSR303, other custom logic in the validator) */
		organizationValidator.validate(command, result, false, "organization");
		boolean hasErrors = result.hasErrors();

		// check if the organization name exists in National Organizations
		if (isEdit) {
			AbstractBasicOrganization persistedOrg = organizationDAO.findByPrimaryKey(org.getId());
			if (!org.getDisplayName().equalsIgnoreCase(persistedOrg.getDisplayName()))
				hasErrors = checkOrgNameDuplication(request, org);
		} else {
			hasErrors = checkOrgNameDuplication(request, org);
		}

		if (!hasErrors) {
			try {
				boolean previousStatus = isEdit ? organizationDAO.findRequiredByPrimaryKey(org.getId()).isActive()
						: org.isActive();
				org = organizationService.saveOrUpdate(org, previousStatus, isEdit);
				userNotifier.notifyUserOnceWithMessage(request,
						getMessage(isEdit ? "organization.update.success" : "organization.create.success"));
			} catch (DataIntegrityViolationException e) {
				hasErrors = true;
				webValidationService.handle(e, result, "organization.create.error.dataIntegrityError");
			} catch (ServiceValidationException e) {
				webValidationService.handle(e, result);
				hasErrors = true;
			}
		}

		String toPage = "/organizationEdit.htm?id=" + org.getId();

		if ("donor".equals(command.getFromPage())) {
			Donor donor = ((Organization) org).getDonor();
			if (donor == null) {
				// create donor
				try {
					donor = donorService.linkOrganization(null, org.getId());
					userNotifier.notifyUserOnceWithMessage(request,
							getMessage(isEdit ? "donor.update.success" : "donor.create.success"));
				} catch (ServiceValidationException e) {
					webValidationService.handle(e, result);
					hasErrors = true;
				}

				// clear out the donor search params that's carried via http
				// session
				request.getSession().removeAttribute("donorSearchParams");

				if (request.getSession().getAttribute(DonationController.DONATION_DEFAULTS) != null) {
					// if session contains donationLogId, then we are in the
					// process of adding an edonation
					// upon completion of adding the donor, go straight to add
					// donation page to allow edonation to be added
					return "redirect:/donationCreate.htm?donorId=" + donor.getId();
				}
			}
			toPage = "/donorEdit.htm?id=" + donor.getId();
		}

		if (hasErrors) {
			createReferenceData(model);
			return isEdit ? "editOrganization" : "createOrganization";
		} else {
			status.setComplete();

		}

		return "redirect:" + toPage;

	}

	// ======================= National Official ==============================

	@RequestMapping("/nationalOfficialList")
	@PreAuthorize("hasAnyAuthority('" + Permission.ORG_CODE_NATIONAL_READ + ", " + Permission.ORG_CODE_LOCAL_READ
			+ "')")
	public @ResponseBody List<NationalOfficial> getOfficialForOrganization(@RequestParam long orgId) {
		List<NationalOfficial> officials = nationalOfficialDAO.findByCriteria(orgId, null);
		return officials;
	}

	@RequestMapping("/nationalOfficalCreateOrUpdate")
	@PreAuthorize("hasAnyAuthority('" + Permission.ORG_CODE_NATIONAL_CREATE + ", " + Permission.ORG_CODE_LOCAL_CREATE
			+ "')")
	public @ResponseBody NationalOfficial createOrUpdateNationalOfficial(@RequestParam long organizationId,
			@RequestParam Long nationalOfficialId, @RequestParam String lastName, @RequestParam String firstName,
			@RequestParam String middleName, @RequestParam String suffix, @RequestParam String prefix,
			@RequestParam String title, @RequestParam boolean certifyingOfficial, @RequestParam String email,
			@RequestParam String streetAddress, @RequestParam String city, @RequestParam State state,
			@RequestParam String zip, @RequestParam String phone, @RequestParam StdVAVSTitle stdVAVSTitle,
			@DateTimeFormat(pattern = DateUtil.TWO_DIGIT_DATE_ONLY) @RequestParam LocalDate vavsStartDate,
			@DateTimeFormat(pattern = DateUtil.TWO_DIGIT_DATE_ONLY) @RequestParam LocalDate vavsEndDate,
			@RequestParam boolean nationalCommitteeMember,
			@DateTimeFormat(pattern = DateUtil.TWO_DIGIT_DATE_ONLY) @RequestParam LocalDate nacStartDate,
			@DateTimeFormat(pattern = DateUtil.TWO_DIGIT_DATE_ONLY) @RequestParam LocalDate nacEndDate)
			throws ServiceValidationException {

		NationalOfficial official = null;
		if (nationalOfficialId == null) {
			official = new NationalOfficial();
			AbstractBasicOrganization org = organizationDAO.findByPrimaryKey(organizationId);
			if (org == null) {
				throw new IllegalArgumentException("Organization not found for id " + organizationId);
			}
			official.setOrganization(org);
		} else {
			official = nationalOfficialDAO.findRequiredByPrimaryKey(nationalOfficialId);
		}

		official.setLastName(lastName);
		official.setFirstName(firstName);
		official.setMiddleName(middleName);
		official.setSuffix(suffix);
		official.setPrefix(prefix);
		official.setTitle(title);
		official.setCertifyingOfficial(certifyingOfficial);
		official.setEmail(email);
		official.setStreetAddress(streetAddress);
		official.setCity(city);
		official.setZip(zip);
		official.setPhone(phone);
		official.setStdVAVSTitle(stdVAVSTitle);
		official.setVavsStartDate(vavsStartDate);
		official.setVavsEndDate(vavsEndDate);
		official.setNationalCommitteeMember(nationalCommitteeMember);
		official.setNacStartDate(nacStartDate);
		official.setNacEndDate(nacEndDate);
		official.setState(state);

		official = nationalOfficialService.saveOrUpdate(official);

		return official;
	}

	@RequestMapping("/nationalOfficialDelete")
	@PreAuthorize("hasAnyAuthority('" + Permission.ORG_CODE_NATIONAL_CREATE + ", " + Permission.ORG_CODE_LOCAL_CREATE
			+ "')")
	public @ResponseBody boolean deleteNationalOfficial(@RequestParam long officialId)
			throws ServiceValidationException {
		nationalOfficialService.delete(officialId);
		return true;
	}

	// ======================= Local Branch ==============================
	@RequestMapping("/localBranchCreateOrUpdate")
	@PreAuthorize("hasAnyAuthority('" + Permission.ORG_CODE_NATIONAL_CREATE + ", " + Permission.ORG_CODE_LOCAL_CREATE
			+ "')")
	public @ResponseBody OrganizationBranch createOrUpdateLocalBranch(@RequestParam long organizationId,
			@RequestParam(required = false) Long localBranchId, @RequestParam boolean status,
			@RequestParam String branchName, @RequestParam(required = false) String contactName,
			@RequestParam(required = false) String contactTitle, @RequestParam(required = false) String addressLine1,
			@RequestParam(required = false) String addressLine2, @RequestParam(required = false) String city,
			@RequestParam(required = false) State state, @RequestParam(required = false) String zip,
			@RequestParam(required = false) String phone, @RequestParam(required = false) String email,
			HttpServletRequest request) throws ServiceValidationException {

		OrganizationBranch localBranch = null;
		boolean isEdit = (localBranchId != null);

		if (!isEdit) {
			localBranch = new OrganizationBranch();
			Organization org = (Organization) organizationDAO.findRequiredByPrimaryKey(organizationId);
			Facility facility = getRequiredFacilityContext();
			localBranch.setFacility(facility);
			localBranch.setOrganization(org);
		} else {
			localBranch = (OrganizationBranch) organizationDAO.findRequiredByPrimaryKey(localBranchId);
		}

		localBranch.setName(branchName);
		boolean previousStatus = localBranch.isActive();
		localBranch.setInactive(!status);
		localBranch.setContactName(contactName);
		localBranch.setContactTitle(contactTitle);
		localBranch.setAddressLine1(addressLine1);
		localBranch.setAddressLine2(addressLine2);
		localBranch.setCity(city);
		localBranch.setPhone(phone);
		localBranch.setEmail(email);
		localBranch.setZip(zip);
		localBranch.setState(state);

		localBranch = (OrganizationBranch) organizationService.saveOrUpdate(localBranch, previousStatus,
				(localBranchId != null));

		userNotifier.notifyUserOnce(request, "organization.saveOrUpdate.success");

		return localBranch;
	}

	@RequestMapping("/localBranchList")
	@PreAuthorize("hasAnyAuthority('" + Permission.ORG_CODE_LOCAL_READ + ", " + Permission.ORG_CODE_NATIONAL_READ
			+ "')")
	@JsonView(OrganizationBranchView.Basic.class)
	public @ResponseBody List<OrganizationBranch> getLocalBranchesForOrganization(@RequestParam Long orgId) {
		if (orgId == null)
			throw new IllegalArgumentException("Organization Id cannot be null");

		Long facilityId = isUserWorkingFacilityCO() ? null : getFacilityContextId();
		return organizationDAO.getLocalBranchesForOrgId(orgId, facilityId, null);
	}

	@RequestMapping("/getOrganizationSearchStations")
	public @ResponseBody List<AbstractBasicOrganization> getStationsForVolunteerSearch() {
		return organizationDAO.findByCriteria(null, true, true, true, Arrays.asList(getFacilityContextId()), null, null,
				null, null);
	}

	@RequestMapping("/findOrganizations")
	// @PreAuthorize("hasAuthority('" + Permission.VOLUNTEER_VIEW + "')")
	@JsonView(OrganizationView.Search.class)
	public @ResponseBody List<AbstractBasicOrganization> findOrganizations(@RequestParam(required = false) String name,
			@RequestParam(required = false) Integer code, @RequestParam(required = false) String abbreviation,
			@RequestParam boolean includeInactive, @RequestParam String mode) {
		if (StringUtil.allBlank(name, abbreviation) && code == null)
			throw new IllegalArgumentException("Please specify at least one piece of search criteria");

		boolean isDonorLink = "donorLink".equals(mode);
		boolean isVolunteerLink = "volunteerLink".equals(mode);

		List<PermissionType> permTypes = new ArrayList<>();
		/*
		 * Assume they need to have both for now to see anything, since the
		 * dialog displays a merged sets of results - CPB
		 */
		permTypes.add(PermissionType.ORG_CODE_LOCAL_READ);
		permTypes.add(PermissionType.ORG_CODE_NATIONAL_READ);

		if (isVolunteerLink) {
			permTypes.add(PermissionType.VOLUNTEER_CREATE);
		} else {
			if (isDonorLink) {
				// donor perm here?
			}
		}

		List<Long> facilityIds = null;
		// if (isDonorLink) {
		facilityIds = Arrays.asList(getFacilityContextId());
		// } else {
		// Set<VAFacility> f = SecurityUtil
		// .getFacilitiesWhereUserHasAllPermissions(permTypes.toArray(new
		// PermissionType[0]));
		// Map<Long, Facility> facilities = facilityDAO.findByVAFacilities(f);
		// facilityIds =
		// PersistenceUtil.translateObjectsToIds(facilities.values());
		// }

		List<AbstractBasicOrganization> volunteers = organizationDAO.findByCriteria(name, true, true, true, facilityIds,
				includeInactive ? null : true, abbreviation, null, null,
				new QueryCustomization().setRowLimitation(maxResults));

		return volunteers;
	}

	@RequestMapping("/organization/quickSearch/currentFacility")
	public @ResponseBody Map<String, Object> findOrganizationsByNameAtWorkingFacility(
			@RequestParam(required = false) String name) {
		long facilityId = getFacilityContextId();
		Map<String, Object> resultMap = new HashMap<>();
		SortedSet<OrgQuickSearchResult> results = organizationDAO.quickSearch(name, facilityId, null);
		resultMap.put("organizations", results);
		return resultMap;
	}

	private boolean checkOrgNameDuplication(HttpServletRequest request, AbstractBasicOrganization org) {

		boolean nameDuplicated = false;
		AbstractBasicOrganization nationalOrg = organizationDAO.getOrganizationByName(org.getName(), true, null);
		if (nationalOrg != null) {
			nameDuplicated = true;
			userNotifier.notifyUserOnceWithMessage(request, getMessage("organization.create.nationalOrg.nameExists"));
		} else if (ScopeType.LOCAL == org.getScope()) {
			AbstractBasicOrganization localOrg = organizationDAO.getOrganizationByName(org.getName(), false,
					org.getFacility().getId());
			if (localOrg != null) {
				nameDuplicated = true;
				userNotifier.notifyUserOnceWithMessage(request, getMessage("organization.create.localOrg.nameExists"));
			}
		}
		return nameDuplicated;
	}
}
