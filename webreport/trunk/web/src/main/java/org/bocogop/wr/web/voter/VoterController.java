package org.bocogop.wr.web.voter;

import static org.bocogop.wr.util.SecurityUtil.hasAllPermissionsAtCurrentPrecinct;

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
import org.bocogop.wr.model.Permission;
import org.bocogop.wr.model.Permission.PermissionType;
import org.bocogop.wr.model.precinct.Precinct;
import org.bocogop.wr.model.precinct.Precinct.PrecinctView;
import org.bocogop.wr.model.voter.Voter;
import org.bocogop.wr.model.voter.Voter.VoterView;
import org.bocogop.wr.service.validation.ServiceValidationException;
import org.bocogop.wr.model.voter.VoterHistoryEntry;
import org.bocogop.wr.util.DateUtil;
import org.bocogop.wr.util.StringUtil;
import org.bocogop.wr.web.AbstractAppController;
import org.bocogop.wr.web.breadcrumbs.Breadcrumb;
import org.bocogop.wr.web.validation.ValidationException;
import org.bocogop.wr.web.voter.VoterSearchResults.Parameters;
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

@Controller
@SessionAttributes(value = { AbstractAppController.DEFAULT_COMMAND_NAME })
public class VoterController extends AbstractAppController {

	public static final String SESSION_ATTR_VOTER_SEARCH_MOST_RECENT = "voterSearchMostRecent";
	public static final String VOTER_SEARCH_PARAMS = "voterSearchParams";

	@Autowired
	private VoterValidator voterValidator;

	// ------------------------------------------------------- Voter Search
	// popup support methods

	@RequestMapping("/voterSearch/precincts")
	@JsonView(PrecinctView.Basic.class)
	public @ResponseBody SortedSet<Precinct> getPrecinctsForVoterSearch() {
		// Set<Precinct> validPrecincts =
		// SecurityUtil.getAllPrecinctsWhereUserHasPermissions(VOTER_READ);
		// return new TreeSet<>(validPrecincts);
		return precinctDAO.findAllSorted(); // findPrecinctsWithLinkToPrecinct();
	}

	@RequestMapping("/voterSearch/find")
	// @PreAuthorize("hasAuthority('" + Permission.VOTER_VIEW + "')")
	@JsonView(VoterView.Search.class)
	public @ResponseBody SortedSet<Voter> findVoters(@RequestParam(required = false) String firstName,
			@RequestParam(required = false) String lastName, @RequestParam(required = false) String code,
			@RequestParam(required = false) String email,
			@DateTimeFormat(pattern = DateUtil.TWO_DIGIT_DATE_ONLY) @RequestParam(required = false) LocalDate dob,
			@RequestParam String scope, @RequestParam(required = false) Long precinctId,
			@RequestParam boolean includeInactive, HttpSession session) {
		if (StringUtil.allBlank(firstName, lastName, code, email) && dob == null)
			throw new IllegalArgumentException("Please specify at least one piece of search criteria");

		if ("National".equals(scope))
			precinctId = null;

		SortedSet<Voter> voters = new TreeSet<>(voterDAO.findByCriteria(firstName, null, lastName, false, false, code,
				dob, null, null, null, null, null, email, precinctId != null ? Arrays.asList(precinctId) : null));

		// save the search params into a session object
		VoterSearchParams searchParams = new VoterSearchParams(lastName, firstName, code, dob, email);
		session.setAttribute(VOTER_SEARCH_PARAMS, searchParams);

		session.setAttribute(SESSION_ATTR_VOTER_SEARCH_MOST_RECENT, new VoterSearchResults(voters,
				new Parameters(firstName, lastName, code, email, dob, scope, includeInactive)));

		return voters;
	}

	@RequestMapping("/voterSearch/mostRecent")
	// @PreAuthorize("hasAuthority('" + Permission.VOTER_VIEW + "')")
	@JsonView(VoterView.Search.class)
	public @ResponseBody VoterSearchResults voterSearchMostRecent(HttpSession session) {
		VoterSearchResults r = (VoterSearchResults) session.getAttribute(SESSION_ATTR_VOTER_SEARCH_MOST_RECENT);
		return r;
	}

	// ------------------------------------------------------- Other Ajax
	// methods

	@RequestMapping(value = "/voter/history", method = RequestMethod.POST)
	// @PreAuthorize("hasAuthority('" + Permission.VOTER_VIEW + "')")
	public @ResponseBody Map<Integer, VoterHistoryEntry> findVoterHistoryEntries(@RequestParam long voterId,
			@RequestParam(name = "versions[]") int[] versions) {
		Map<Integer, VoterHistoryEntry> results = voterHistoryEntryDAO.findByVersions(voterId, versions);
		return results;
	}

	// ------------------------------------------------------- Voter form
	// display and submit methods

	@RequestMapping("/voter/preSubmitChecks")
	@JsonView(VoterView.Search.class)
	public @ResponseBody Map<String, Object> preSubmitCheckFindDuplicateVoters(@RequestParam String firstName,
			@RequestParam String lastName,
			@DateTimeFormat(pattern = DateUtil.TWO_DIGIT_DATE_ONLY) @RequestParam LocalDate dob,
			@RequestParam(required = false) Long voterId) {
		if (StringUtils.isBlank(lastName) || StringUtils.isBlank(firstName))
			throw new IllegalArgumentException("Both last name and first name are required.");

		boolean isEdit = voterId != null;

		Map<String, Object> results = new HashMap<>();

		SortedSet<Voter> voters = new TreeSet<>();
		List<Voter> nameMatches = voterDAO.findByCriteria(firstName, null, lastName, true, true, null, dob, null, null,
				null, null, null, null, null, null);
		voters.addAll(nameMatches);

		if (isEdit) {
			voters = voters.stream().filter(p -> !p.getId().equals(voterId))
					.collect(Collectors.toCollection(TreeSet::new));
		}
		results.put("potentialDuplicates", voters);

		return results;
	}

	@RequestMapping("/voterCreate.htm")
	// Don't want a Breadcrumb here since we want to force them to search first
	// every time - CPB
	// @Breadcrumb("Create Voter")
	@PreAuthorize("hasAuthority('" + Permission.VOTER_EDIT + "')")
	public String voterCreate(ModelMap model, HttpServletRequest request) {
		Voter voter = new Voter();
		/*
		 * Will be overwritten during the saveOrUpdate; this prevents the UI
		 * from displaying this initially - CPB
		 */
		voter.setMealsEligible(1);
		voter.setStatusDate(LocalDate.now());

		VoterSearchParams params = (VoterSearchParams) request.getSession().getAttribute(VOTER_SEARCH_PARAMS);

		if (params != null) {
			voter.setLastName(params.getLastName());
			voter.setFirstName(params.getFirstName());
			voter.setDateOfBirth(params.getDob());
			voter.setEmail(params.getEmail());

			// clear out the search params that's carried via http session
			request.getSession().removeAttribute(VOTER_SEARCH_PARAMS);
		}

		VoterCommand command = new VoterCommand(voter, null);
		model.addAttribute(DEFAULT_COMMAND_NAME, command);
		createReferenceData(command, model);
		return "createVoter";
	}

	@RequestMapping("/voterEdit.htm")
	@Breadcrumb("Edit Voter")
	public String voterEdit(@RequestParam long id, @RequestParam(required = false) String fromPage, ModelMap model,
			HttpServletRequest request) {
		Voter voter = voterDAO.findRequiredByPrimaryKey(id);

		VoterCommand command = new VoterCommand(voter, fromPage);
		model.addAttribute(DEFAULT_COMMAND_NAME, command);
		// WebUtil.addClassConstantsToModel(RequirementDateType.class, model);
		// WebUtil.addEnumToModel(RequirementDateTypeValue.class, model);

		createReferenceData(command, model);

		return "editVoter";
	}

	private void createReferenceData(VoterCommand command, ModelMap model) {
		Voter voter = command.getVoter();
		model.put("allGenders", genderDAO.findAllSorted());

		Set<Precinct> editSites = getCurrentUser().getPrecinctsWhereUserHasAllPermissions(PermissionType.VOTER_EDIT);
		List<Long> precinctIds = editSites.stream().map(p -> p.getId()).collect(Collectors.toList());
		model.addAttribute("voterEditSiteIds", StringUtils.join(precinctIds, ','));

		model.addAttribute("backgroundStatus", "No Issue Reported");
		if (voter.isPersistent()) {
			model.addAttribute("allVoterPrecincts", voterDAO.findPrecinctsForVoter(voter.getId()));
		}

		if (!hasAllPermissionsAtCurrentPrecinct(PermissionType.VOTER_EDIT))
			setFormAsReadOnly(model, true);
	}

	@RequestMapping("/voterSubmit.htm")
	@PreAuthorize("hasAuthority('" + Permission.VOTER_EDIT + "')")
	public String voterSubmit(@ModelAttribute(DEFAULT_COMMAND_NAME) VoterCommand command, BindingResult result,
			SessionStatus status, ModelMap model, HttpServletRequest request) throws ValidationException {
		Voter voter = command.getVoter();

		boolean isEdit = voter.isPersistent();

		voterValidator.validate(command, result, false, "voter");
		boolean hasErrors = result.hasErrors();

		if (!hasErrors) {
			try {
				voter = voterService.saveOrUpdate(voter, false, false);
				userNotifier.notifyUserOnceWithMessage(request,
						getMessage(isEdit ? "voter.update.success" : "voter.create.success"));
			} catch (ServiceValidationException e) {
				webValidationService.handle(e, result);
				hasErrors = true;
			}
		}

		if (hasErrors) {
			createReferenceData(command, model);
			return isEdit ? "editVoter" : "createVoter";
		} else {
			status.setComplete();

			String toPage = "/voterEdit.htm?id=" + voter.getId();
			return "redirect:" + toPage;
		}
	}

}
