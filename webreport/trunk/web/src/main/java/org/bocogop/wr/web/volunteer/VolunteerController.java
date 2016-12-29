package org.bocogop.wr.web.volunteer;

import static org.bocogop.shared.model.Permission.PermissionType.VOLUNTEER_CREATE;
import static org.bocogop.shared.util.SecurityUtil.hasAllPermissionsAtCurrentFacility;
import static org.bocogop.wr.model.volunteer.VolunteerStatus.VolunteerStatusType.ACTIVE;
import static org.bocogop.wr.model.volunteer.VolunteerStatus.VolunteerStatusType.TERMINATED;
import static org.bocogop.wr.model.volunteer.VolunteerStatus.VolunteerStatusType.TERMINATED_WITH_CAUSE;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
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
import org.bocogop.shared.model.Permission.PermissionType;
import org.bocogop.shared.model.lookup.sds.VAFacility;
import org.bocogop.shared.service.validation.ServiceValidationException;
import org.bocogop.shared.util.StringUtil;
import org.bocogop.shared.util.WebUtil;
import org.bocogop.wr.model.donation.Donor;
import org.bocogop.wr.model.facility.Facility;
import org.bocogop.wr.model.facility.Facility.FacilityView;
import org.bocogop.wr.model.leie.ExcludedEntity;
import org.bocogop.wr.model.organization.ScopeType;
import org.bocogop.wr.model.requirement.RequirementApplicationType;
import org.bocogop.wr.model.requirement.RequirementDateType.RequirementDateTypeValue;
import org.bocogop.wr.model.requirement.RequirementStatus.RequirementStatusValue;
import org.bocogop.wr.model.requirement.RequirementType;
import org.bocogop.wr.model.volunteer.Volunteer;
import org.bocogop.wr.model.volunteer.Volunteer.VolunteerView;
import org.bocogop.wr.model.volunteer.VolunteerHistoryEntry;
import org.bocogop.wr.model.volunteer.VolunteerStatus;
import org.bocogop.wr.model.volunteer.VolunteerStatus.VolunteerStatusType;
import org.bocogop.wr.persistence.dao.TimeSummary;
import org.bocogop.wr.persistence.dao.volunteer.VolunteerQuickSearchResult;
import org.bocogop.wr.persistence.dao.volunteer.VolunteerQuickSearchResult.VolunteerQuickSearchResultView;
import org.bocogop.wr.service.scheduledJobs.VolunteerAutoInactivationJob;
import org.bocogop.wr.util.DateUtil;
import org.bocogop.wr.web.AbstractAppController;
import org.bocogop.wr.web.breadcrumbs.Breadcrumb;
import org.bocogop.wr.web.validation.ValidationException;
import org.bocogop.wr.web.volunteer.VolunteerSearchResults.Parameters;

@Controller
@SessionAttributes(value = { AbstractAppController.DEFAULT_COMMAND_NAME })
public class VolunteerController extends AbstractAppController {

	public static final String SESSION_ATTR_VOLUNTEER_SEARCH_MOST_RECENT = "volunteerSearchMostRecent";
	public static final String VOLUNTEER_SEARCH_PARAMS = "volunteerSearchParams";

	@Autowired
	private VolunteerValidator volunteerValidator;
	@Autowired
	private VolunteerAutoInactivationJob autoInactivationJob;

	// ------------------------------------------------------- Volunteer Search
	// popup support methods

	@RequestMapping("/volunteerSearch/facilities")
	@JsonView(FacilityView.Basic.class)
	public @ResponseBody SortedSet<Facility> getFacilitiesForVolunteerSearch() {
		// Set<VAFacility> validFacilities =
		// SecurityUtil.getAllFacilitiesWhereUserHasPermissions(VOLUNTEER_READ);
		// return new TreeSet<>(validFacilities);
		return facilityDAO.findAllSorted(); // findVAFacilitiesWithLinkToFacility();
	}

	@RequestMapping("/volunteerSearch/find")
	// @PreAuthorize("hasAuthority('" + Permission.VOLUNTEER_VIEW + "')")
	@JsonView(VolunteerView.Search.class)
	public @ResponseBody SortedSet<Volunteer> findVolunteers(@RequestParam(required = false) String firstName,
			@RequestParam(required = false) String lastName, @RequestParam(required = false) String code,
			@RequestParam(required = false) String email,
			@DateTimeFormat(pattern = DateUtil.TWO_DIGIT_DATE_ONLY) @RequestParam(required = false) LocalDate dob,
			@RequestParam String scope, @RequestParam(required = false) Long facilityId,
			@RequestParam boolean includeInactive, HttpSession session) {
		if (StringUtil.allBlank(firstName, lastName, code, email) && dob == null)
			throw new IllegalArgumentException("Please specify at least one piece of search criteria");

		if ("National".equals(scope))
			facilityId = null;

		SortedSet<Volunteer> volunteers = new TreeSet<>(
				volunteerDAO.findByCriteria(firstName, null, lastName, false, false, code, dob, null, null, null, null,
						null, email, includeInactive ? null : VolunteerStatusType.ACTIVE,
						facilityId != null ? Arrays.asList(facilityId) : null));

		// save the search params into a session object
		VolunteerSearchParams searchParams = new VolunteerSearchParams(lastName, firstName, code, dob, email);
		session.setAttribute(VOLUNTEER_SEARCH_PARAMS, searchParams);

		session.setAttribute(SESSION_ATTR_VOLUNTEER_SEARCH_MOST_RECENT,
				new VolunteerSearchResults(volunteers, getFacilityContextId(),
						new Parameters(firstName, lastName, code, email, dob, scope, facilityId, includeInactive)));

		return volunteers;
	}

	@RequestMapping("/volunteerSearch/mostRecent")
	// @PreAuthorize("hasAuthority('" + Permission.VOLUNTEER_VIEW + "')")
	@JsonView(VolunteerView.Search.class)
	public @ResponseBody VolunteerSearchResults volunteerSearchMostRecent(HttpSession session) {
		VolunteerSearchResults r = (VolunteerSearchResults) session
				.getAttribute(SESSION_ATTR_VOLUNTEER_SEARCH_MOST_RECENT);
		if (r == null)
			return null;

		if (r.facilityId != getFacilityContextId()) {
			session.removeAttribute(SESSION_ATTR_VOLUNTEER_SEARCH_MOST_RECENT);
			return null;
		}

		return r;
	}

	// ------------------------------------------------------- Time Entry
	// support methods

	@RequestMapping("/volunteer/quickSearch/currentFacility")
	@JsonView(VolunteerQuickSearchResultView.TimeEntrySearch.class)
	@PreAuthorize("hasAnyAuthority('" + Permission.TIME_CREATE + ", " + Permission.TIME_READ + "')")
	public @ResponseBody Map<String, Object> quickSearchVolunteersForCurrentFacility(
			@RequestParam(required = false) String name) {
		long facilityId = getFacilityContextId();

		Map<String, Object> resultMap = new HashMap<>();

		SortedSet<VolunteerQuickSearchResult> quickSearch = volunteerDAO.quickSearch(name, null, facilityId, true, true,
				true);

		/*
		 * Only show volunteers that have at least one active assignment and
		 * organization - CPB
		 */
		SortedSet<VolunteerQuickSearchResult> results = quickSearch.stream()
				.filter(p -> !p.getAssignments().isEmpty() && !p.getOrganizations().isEmpty())
				.collect(Collectors.toCollection(TreeSet::new));

		resultMap.put("volunteers", results);

		return resultMap;
	}

	@RequestMapping("/volunteer/quickSearch/individualPlusAssignmentsAndOrgs")
	@JsonView(VolunteerQuickSearchResultView.TimeEntrySearch.class)
	@PreAuthorize("hasAnyAuthority('" + Permission.TIME_CREATE + ", " + Permission.TIME_READ + "')")
	public @ResponseBody VolunteerQuickSearchResult volunteerWithAssignmentsAndOrgsAtCurrentFacility(
			@RequestParam long volunteerId, @RequestParam boolean onlyActiveAssignmentsAndOrgs) {
		long facilityId = getFacilityContextId();
		SortedSet<VolunteerQuickSearchResult> results = volunteerDAO.quickSearch(null, volunteerId, facilityId, true,
				true, onlyActiveAssignmentsAndOrgs);
		return results.isEmpty() ? null : results.iterator().next();
	}

	// ------------------------------------------------------- Other Ajax
	// methods

	@RequestMapping(value = "/volunteer/history", method = RequestMethod.POST)
	// @PreAuthorize("hasAuthority('" + Permission.VOLUNTEER_VIEW + "')")
	public @ResponseBody Map<Integer, VolunteerHistoryEntry> findVolunteerHistoryEntries(@RequestParam long volunteerId,
			@RequestParam(name = "versions[]") int[] versions) {
		Map<Integer, VolunteerHistoryEntry> results = volunteerHistoryEntryDAO.findByVersions(volunteerId, versions);
		return results;
	}

	@RequestMapping(value = "/volunteer/setPrimaryFacility", method = RequestMethod.POST)
	// @PreAuthorize("hasAuthority('" + Permission.VOLUNTEER_VIEW + "')")
	public @ResponseBody boolean primaryFacilitySet(@RequestParam long volunteerId, @RequestParam long facilityId) {
		volunteerService.setPrimaryFacility(volunteerId, facilityId);
		return true;
	}

	@RequestMapping("/runAutoInactivation")
	public @ResponseBody String runAutoInactivation() throws IOException {
		return autoInactivationJob.inactivateStaleVolunteersWithStatus();
	}

	// ------------------------------------------------------- Volunteer form
	// display and submit methods

	@RequestMapping("/volunteer/preSubmitChecks")
	@JsonView(VolunteerView.Search.class)
	public @ResponseBody Map<String, Object> preSubmitCheckFindDuplicateVolunteers(@RequestParam String firstName,
			@RequestParam String lastName,
			@DateTimeFormat(pattern = DateUtil.TWO_DIGIT_DATE_ONLY) @RequestParam LocalDate dob,
			@RequestParam(required = false) Long volunteerId) {
		if (StringUtils.isBlank(lastName) || StringUtils.isBlank(firstName))
			throw new IllegalArgumentException("Both last name and first name are required.");

		boolean isEdit = volunteerId != null;

		Map<String, Object> results = new HashMap<>();

		SortedSet<Volunteer> volunteers = new TreeSet<>();
		List<Volunteer> nameMatches = volunteerDAO.findByCriteria(firstName, null, lastName, true, true, null, dob,
				null, null, null, null, null, null, null, null);
		volunteers.addAll(nameMatches);

		if (isEdit) {
			volunteers = volunteers.stream().filter(p -> !p.getId().equals(volunteerId))
					.collect(Collectors.toCollection(TreeSet::new));
		}
		results.put("potentialDuplicates", volunteers);

		if (!isEdit) {
			List<ExcludedEntity> matches = excludedEntityDAO.findExcludedEntitiesForVolunteerInfo(lastName, firstName,
					dob, null);
			results.put("leieMatches", matches);
		}

		return results;
	}

	@RequestMapping("/volunteerCreate.htm")
	// Don't want a Breadcrumb here since we want to force them to search first
	// every time - CPB
	// @Breadcrumb("Create Volunteer")
	@PreAuthorize("hasAuthority('" + Permission.VOLUNTEER_CREATE + "')")
	public String volunteerCreate(ModelMap model, HttpServletRequest request) {
		Volunteer volunteer = new Volunteer();
		/*
		 * Will be overwritten during the saveOrUpdate; this prevents the UI
		 * from displaying this initially - CPB
		 */
		volunteer.setStatus(volunteerStatusDAO.findByLookup(ACTIVE));
		volunteer.setMealsEligible(1);
		volunteer.setStatusDate(getTodayAtFacility());

		VolunteerSearchParams params = (VolunteerSearchParams) request.getSession()
				.getAttribute(VOLUNTEER_SEARCH_PARAMS);

		if (params != null) {
			volunteer.setLastName(params.getLastName());
			volunteer.setFirstName(params.getFirstName());
			volunteer.setDateOfBirth(params.getDob());
			volunteer.setEmail(params.getEmail());

			// clear out the search params that's carried via http session
			request.getSession().removeAttribute(VOLUNTEER_SEARCH_PARAMS);
		}

		VolunteerCommand command = new VolunteerCommand(volunteer, null, null);
		model.addAttribute(DEFAULT_COMMAND_NAME, command);
		createReferenceData(command, model);
		return "createVolunteer";
	}

	@RequestMapping("/volunteerEdit.htm")
	@Breadcrumb("Edit Volunteer")
	@PreAuthorize("hasAuthority('" + Permission.VOLUNTEER_READ + "')")
	public String volunteerEdit(@RequestParam long id, @RequestParam(required = false) String fromPage, ModelMap model,
			HttpServletRequest request) {
		Volunteer volunteer = volunteerDAO.findRequiredByPrimaryKey(id);

		TimeSummary summary = volunteerDAO.getTimeSummary(id, getFacilityTimeZone());
		VolunteerCommand command = new VolunteerCommand(volunteer, summary, fromPage);
		model.addAttribute(DEFAULT_COMMAND_NAME, command);
		model.addAttribute("DateValueNotApplicable", RequirementDateTypeValue.NOT_APPLICABLE);
		// WebUtil.addClassConstantsToModel(RequirementDateType.class, model);
		// WebUtil.addEnumToModel(RequirementDateTypeValue.class, model);

		createReferenceData(command, model);

		return "editVolunteer";
	}

	private void createReferenceData(VolunteerCommand command, ModelMap model) {
		Volunteer volunteer = command.getVolunteer();
		model.put("allGenders", genderDAO.findAllSorted());
		model.put("allLanguages", languageDAO.findAllSorted());
		model.put("allTransportationMethods", transportationMethodDAO.findAllSorted());
		model.put("allAwards", awardDAO.findAllSorted());
		model.put("allShirtSizes", uniformDAO.findAllShirtSizes());
		model.put("allRequirementStatuses", requirementStatusDAO.findAllSorted());
		WebUtil.addEnumToModel(RequirementApplicationType.class, model);
		WebUtil.addEnumToModel(RequirementType.class, model);
		WebUtil.addEnumToModel(RequirementStatusValue.class, model);

		Set<VAFacility> editSites = getCurrentUser()
				.getFacilitiesWhereUserHasAllPermissions(PermissionType.VOLUNTEER_CREATE);
		Map<Long, Facility> i = facilityDAO.findByVAFacilities(editSites);
		List<Long> facilityIds = i.values().stream().filter(p -> !p.isCentralOffice()).map(p -> p.getId())
				.collect(Collectors.toList());
		model.addAttribute("volunteerEditSiteIds", StringUtils.join(facilityIds, ','));

		model.addAttribute("backgroundStatus", "No Issue Reported");
		if (volunteer.isPersistent()) {
			model.addAttribute("allVolunteerFacilities", volunteerDAO.findFacilitiesForVolunteer(volunteer.getId()));
			model.addAttribute("yearsVolunteering", workEntryDAO.getNumYearsWorked(volunteer.getId()));
		}

		model.addAttribute("volunteerHasActiveAssignmentAtCurrentFacility", volunteer.getVolunteerAssignments().stream()
				.anyMatch(p -> p.isActive() && p.getFacility().getRootFacilityId() == getFacilityContextId()));
		model.addAttribute("volunteerHasActiveOrganizationAtCurrentFacility",
				volunteer.getVolunteerOrganizations().stream()
						.anyMatch(p -> p.isActive() && (p.getOrganization().getScope() == ScopeType.NATIONAL
								|| p.getOrganization().getFacility().getRootFacilityId() == getFacilityContextId())));
		if (!hasAllPermissionsAtCurrentFacility(VOLUNTEER_CREATE) || command.getTerminationDate() != null)
			setFormAsReadOnly(model, true);
		if (hasAllPermissionsAtCurrentFacility(VOLUNTEER_CREATE) && command.getTerminationDate() != null)
			model.addAttribute("enableTerminationControls", true);
	}

	@RequestMapping("/volunteerSubmit.htm")
	@PreAuthorize("hasAuthority('" + Permission.VOLUNTEER_CREATE + "')")
	public String volunteerSubmit(@ModelAttribute(DEFAULT_COMMAND_NAME) VolunteerCommand command, BindingResult result,
			SessionStatus status, ModelMap model, HttpServletRequest request) throws ValidationException {
		Volunteer volunteer = command.getVolunteer();

		boolean isEdit = volunteer.isPersistent();

		volunteerValidator.validate(command, result, false, "volunteer");
		boolean hasErrors = result.hasErrors();

		if (!hasErrors) {

			if (command.getTerminationDate() != null) {
				volunteer.setStatus(volunteerStatusDAO
						.findByLookup(command.isVolunteerTerminatedWithCause() ? TERMINATED_WITH_CAUSE : TERMINATED));
				volunteer.setStatusDate(command.getTerminationDate());
			} else {
				VolunteerStatus newStatus = volunteerStatusDAO.findByPrimaryKey(volunteer.getStatus().getId());
				if (command.getTerminationDate() == null && newStatus.getLookupType().isTerminated()) {
					/*
					 * saveOrUdpate call below will reset this to INACTIVE if
					 * necessary - CPB
					 */
					volunteer.setStatus(volunteerStatusDAO.findByLookup(ACTIVE));
					volunteer.setStatusDate(getTodayAtFacility());
				}
			}
			volunteer.setTerminationRemarks(command.getTerminationRemarks());

			try {
				volunteer = volunteerService.saveOrUpdate(volunteer, false, false);
				userNotifier.notifyUserOnceWithMessage(request,
						getMessage(isEdit ? "volunteer.update.success" : "volunteer.create.success"));
			} catch (ServiceValidationException e) {
				webValidationService.handle(e, result);
				hasErrors = true;
			}
		}

		if (hasErrors) {
			createReferenceData(command, model);
			return isEdit ? "editVolunteer" : "createVolunteer";
		} else {
			status.setComplete();

			String toPage = "/volunteerEdit.htm?id=" + volunteer.getId();
			if ("donor".equals(command.getFromPage())) {
				Donor donor = volunteer.getDonor();
				if (donor != null)
					toPage = "/donorEdit.htm?id=" + donor.getId();
			}
			return "redirect:" + toPage;
		}
	}

}
