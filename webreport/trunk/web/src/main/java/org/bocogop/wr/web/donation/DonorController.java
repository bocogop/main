package org.bocogop.wr.web.donation;

import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
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
import org.bocogop.shared.util.StringUtil;
import org.bocogop.wr.model.donation.DonationLog;
import org.bocogop.wr.model.donation.DonationSummary;
import org.bocogop.wr.model.donation.Donor;
import org.bocogop.wr.model.donation.Donor.DonorView;
import org.bocogop.wr.model.donation.DonorType;
import org.bocogop.wr.model.donation.DonorType.DonorTypeValue;
import org.bocogop.wr.persistence.dao.lookup.DonorTypeDAO;
import org.bocogop.wr.persistence.impl.DonorDAOImpl.DonorSearchResult;
import org.bocogop.wr.web.AbstractAppController;
import org.bocogop.wr.web.breadcrumbs.Breadcrumb;
import org.bocogop.wr.web.validation.ValidationException;

@Controller
@SessionAttributes(value = { AbstractAppController.DEFAULT_COMMAND_NAME, "donorSearchParms" })
public class DonorController extends AbstractAppController {

	public static final String DONOR_SEARCH_PARAMS = "donorSearchParams";

	@Autowired
	private DonorValidator donorValidator;

	@Autowired
	private DonorTypeDAO donorTypeDao;

	@RequestMapping("/donorCreate.htm")
	@PreAuthorize("hasAuthority('" + Permission.DONATION_CREATE + "')")
	public String createDonor(@RequestParam String type, ModelMap model, HttpServletRequest request) {
		Donor donor = new Donor();

		DonorSearchParams params = (DonorSearchParams) request.getSession().getAttribute(DONOR_SEARCH_PARAMS);

		if (params != null) {
			if (params.getDonorType() == DonorType.DonorTypeValue.INDIVIDUAL.getId()) {
				donor.setLastName(params.getLastName());
				donor.setFirstName(params.getFirstName());
				donor.setCity(params.getCity());
				donor.setState(params.getState());
				donor.setEmail(params.getEmail());
				donor.setPhone(params.getPhone());
				donor.setZip(params.getZip());

				// clear out the search params that's carried via http session
				request.getSession().removeAttribute(DONOR_SEARCH_PARAMS);
			}
			// else organization
			// can't remove the session object from the session since we need to
			// use it in the organization search popup
		}

		// If session contains donationLogId, then we are trying to add
		// edonation, repopulate add donor fields
		// with edonation data
		Long donationLogId = (Long) request.getSession().getAttribute(DonationController.DONATION_DEFAULTS);
		DonationLog donationLog = null;
		if (donationLogId != null)
			donationLog = donationLogDAO.findByPrimaryKey(donationLogId);

		if (donationLog != null) {
			donor.setLastName(donationLog.parseLastName());
			donor.setFirstName(donationLog.parseFirstName());
			donor.setAddressLine1(donationLog.getAddress());
			donor.setCity(donationLog.getCity());
			donor.setState(stateDAO.findStateByPostalCode(donationLog.getState()));
			donor.setEmail(donationLog.getEmail());
			donor.setPhone(donationLog.getPhone());
			donor.setZip(donationLog.getZip());
		}

		DonorCommand command = new DonorCommand(donor);

		if ("organization".equals(type)) {
			donor.setDonorType(donorTypeDao.findByLookup(DonorTypeValue.ORGANIZATION));

		} else {
			// only two donor types are allowed to be created from the Create
			// screen
			donor.setDonorType(donorTypeDao.findByLookup(DonorTypeValue.INDIVIDUAL));
		}

		model.addAttribute(DEFAULT_COMMAND_NAME, command);

		createReferenceData(model);

		return "createDonor";
	}

	@RequestMapping("/donorEdit.htm")
	@Breadcrumb("Edit Donor")
	@PreAuthorize("hasAuthority('" + Permission.DONATION_READ + "')")
	public String editDonor(@RequestParam long id, @RequestParam(required = false) Long printMemo,
			@RequestParam(required = false) Long printReceipt, @RequestParam(required = false) Long printThankYou,
			@RequestParam(required = false) String printFormat, ModelMap model, HttpServletRequest request) {
		Donor donor = donorDAO.findRequiredByPrimaryKey(id);
		DonorCommand command = new DonorCommand(donor);

		model.addAttribute(DEFAULT_COMMAND_NAME, command);
		createReferenceData(model);

		model.addAttribute("printMemo", printMemo);
		model.addAttribute("printReceipt", printReceipt);
		model.addAttribute("printThankYou", printThankYou);
		model.addAttribute("printFormat", printFormat);

		if (DonorTypeValue.ORG_AND_INDIVIDUAL.equals(donor.getDonorType().getLookupType())
				|| DonorTypeValue.OTHER_AND_INDIVIDUAL.equals(donor.getDonorType().getLookupType())
				|| DonorTypeValue.OTHER_GROUPS.equals(donor.getDonorType().getLookupType())) {
			setFormAsReadOnly(model, true);
		} else {
			setFormAsReadOnlyUnlessUserHasPermissions(model, PermissionType.DONATION_CREATE);
		}

		DonationSummary lastDonation = null;
		LinkedHashMap<DonationSummary, Boolean> results = new LinkedHashMap<>();
		for (DonationSummary s : donor.getDonations()) {
			if (lastDonation == null || lastDonation.getDonationDate().isBefore(s.getDonationDate()))
				lastDonation = s;
			results.put(s, isDonationSummaryEditable(s));
		}
		model.addAttribute("donationMap", results);
		if (lastDonation != null) {
			model.addAttribute("lastDonationFacility", lastDonation.getFacility().getDisplayName());
			String lastDonationDate = lastDonation.getDonationDate().format(DateTimeFormatter.ofPattern("MM/dd/yyyy"));
			model.addAttribute("lastDonationDate", lastDonationDate);
		}
		if (!(Boolean) model.get(FORM_READ_ONLY))
			model.put(FORM_READ_ONLY, shouldDisableAddDonation(donor));

		return "editDonor";
	}

	private void createReferenceData(ModelMap model) {
		appendCommonReportParams(model);
	}

	@RequestMapping("/donorSubmit.htm")
	@PreAuthorize("hasAuthority('" + Permission.DONATION_CREATE + "')")
	public String submitDonor(@ModelAttribute(DEFAULT_COMMAND_NAME) DonorCommand command, BindingResult result,
			SessionStatus status, ModelMap model, HttpServletRequest request) throws ValidationException {
		Donor donor = command.getDonor();

		boolean isEdit = donor.isPersistent();

		if (isEdit) {
			if ("individual".equals(command.getDesiredIndividualType()) && donor.getVolunteer() != null) {
				donor.setVolunteer(null);
			}
		}

		/* Validation step (JSR303, other custom logic in the validator) */
		donorValidator.validate(command, result, false, "donor");
		boolean hasErrors = result.hasErrors();

		if (!hasErrors) {
			try {
				donor = donorService.saveOrUpdate(donor);
				userNotifier.notifyUserOnceWithMessage(request,
						getMessage(isEdit ? "donor.update.success" : "donor.create.success"));
			} catch (ServiceValidationException e) {
				webValidationService.handle(e, result);
				hasErrors = true;
			}
		}

		if (hasErrors) {
			createReferenceData(model);
			return isEdit ? "editDonor" : "createDonor";
		} else {
			status.setComplete();

			if (request.getSession().getAttribute(DonationController.DONATION_DEFAULTS) != null) {
				// if session contains donationLogId, then we are in the process
				// of adding an edonation
				// upon completion of adding the donor, go straight to add
				// donation page to allow edonation to be added
				return "redirect:/donationCreate.htm?donorId=" + donor.getId();
			}

			return "redirect:/donorEdit.htm?id=" + donor.getId();
		}
	}

	@RequestMapping("/findDonors")
	@PreAuthorize("hasAuthority('" + Permission.DONATION_READ + "')")
	@JsonView(DonorView.Search.class)
	public @ResponseBody List<DonorSearchResult> findDonors(@RequestParam long donorType,
			@RequestParam(required = false) String firstName, @RequestParam(required = false) String lastName,
			@RequestParam(required = false) String orgName, @RequestParam(required = false) String city,
			@RequestParam(required = false) State state, @RequestParam(required = false) String zip,
			@RequestParam(required = false) String email, @RequestParam(required = false) String phone,
			@RequestParam(required = false) String facilityScope, HttpSession session) {
		if (StringUtil.allBlank(firstName, lastName, orgName, city, state != null ? state.getId().toString() : "", zip,
				email, phone, facilityScope))
			throw new IllegalArgumentException("Please specify at least one piece of search criteria");

		// save the search params into a session object
		DonorSearchParams searchParams = new DonorSearchParams(donorType, lastName, firstName, orgName, city, state,
				zip, email, phone);
		session.setAttribute(DONOR_SEARCH_PARAMS, searchParams);

		DonorType dt = donorTypeDAO.findRequiredByPrimaryKey(donorType);

		Long facilityId = null;

		// org donor search has been changed to automatically include local
		// facility; individual donor search
		// has option for local facility vs. any facility
		if (dt.getLookupType() == DonorTypeValue.ORGANIZATION || "L".equals(facilityScope)) {
			facilityId = getFacilityContextId();
		}
		List<DonorSearchResult> donors = donorDAO.findByCriteria(dt, firstName, null, lastName, orgName, city, state,
				zip, email, phone, facilityId);

		return donors;
	}

	@RequestMapping("/donor/donorLinkVolunteer")
	@PreAuthorize("hasAuthority('" + Permission.DONATION_CREATE + "')")
	public @ResponseBody Donor linkVolunteer(@RequestParam(required = false) Long donorId,
			@RequestParam Long volunteerId) throws ServiceValidationException {
		return donorService.linkVolunteer(donorId, volunteerId);
	}

	@RequestMapping("/donor/donorLinkOrganization")
	@PreAuthorize("hasAuthority('" + Permission.DONATION_CREATE + "')")
	public @ResponseBody Donor linkOrganization(@RequestParam(required = false) Long donorId, @RequestParam Long orgId)
			throws ServiceValidationException {

		return donorService.linkOrganization(donorId, orgId);
	}

	// Donor Type 2 - Organization & Individual; 3 - Other Groups & Individual;
	// 5 - Other Groups; 1 - individual; 4 - organization

	private boolean isDonationSummaryEditable(DonationSummary donationSum) {
		if (!getRequiredSiteContext().getStationNumber().equalsIgnoreCase(donationSum.getFacility().getStationNumber())
				|| 2 == donationSum.getDonor().getDonorType().getId()
				|| 3 == donationSum.getDonor().getDonorType().getId()
				|| 5 == donationSum.getDonor().getDonorType().getId()
				|| (1 == donationSum.getDonor().getDonorType().getId() && donationSum.getOrganization() != null
						&& donationSum.getOrganization().isInactive())
				|| (4 == donationSum.getDonor().getDonorType().getId()
						&& donationSum.getDonor().getOrganization() != null
						&& donationSum.getDonor().getOrganization().isInactive())) {
			return false;
		}

		return dateUtil.isDonationSummaryEditable(donationSum.getDonationDate(), getFacilityTimeZone());
	}

	private boolean shouldDisableAddDonation(Donor donor) {
		if (2 == donor.getDonorType().getId() || 3 == donor.getDonorType().getId() || 5 == donor.getDonorType().getId()
				|| (4 == donor.getDonorType().getId() && donor.getOrganization() != null
						&& donor.getOrganization().isInactive())) {
			return true;
		}

		return false;
	}

	@RequestMapping("/mergeDonor")
	@PreAuthorize("hasAuthority('" + Permission.MERGE_DONOR + "')")
	public @ResponseBody Donor donorMerge(@RequestParam Long sourceDonorId, @RequestParam Long targetDonorId,
			HttpServletRequest request) throws ServiceValidationException {

		Donor sourceDonor = donorDAO.findByPrimaryKey(sourceDonorId);
		Donor targetDonor = donorDAO.findByPrimaryKey(targetDonorId);
		List<DonationSummary> targetDonations = targetDonor.getDonations();
		for (DonationSummary s : sourceDonor.getDonations()) {
			s.setDonor(targetDonor);
			targetDonations.add(s);
		}

		targetDonor.setDonations(targetDonations);

		// sourceDonor = donorService.saveOrUpdate(sourceDonor);
		targetDonor = donorService.saveOrUpdate(targetDonor);
		donorService.delete(sourceDonorId);
		userNotifier.notifyUserOnceWithMessage(request, getMessage("donor.merge.success"));
		return targetDonor;
	}

	@RequestMapping("/mergeDonorToAnonymous")
	@PreAuthorize("hasAuthority('" + Permission.MERGE_DONOR + "')")
	public @ResponseBody Boolean mergeDonorToAninymous(@RequestParam Long sourceDonorId, HttpServletRequest request)
			throws ServiceValidationException {

		Donor d = donorDAO.findByPrimaryKey(new Long(0));
		Donor sourceDonor = donorDAO.findByPrimaryKey(sourceDonorId);
		List<DonationSummary> targetDonations = d.getDonations();
		for (DonationSummary s : sourceDonor.getDonations()) {
			s.setDonor(d);
			targetDonations.add(s);
		}

		d.setDonations(targetDonations);
		d = donorService.saveOrUpdate(d);
		donorService.delete(sourceDonorId);

		userNotifier.notifyUserOnceWithMessage(request, getMessage("donor.merge.success"));
		return true;
	}

	@RequestMapping("/donorDuplicateCheck")
	@JsonView(DonorView.Search.class)
	public @ResponseBody SortedSet<DonorSearchResult> findDuplicateDonors(@RequestParam String firstName,
			@RequestParam String lastName, @RequestParam(required = false) State state,
			@RequestParam(required = false) Long excludeDonorId) {

		SortedSet<DonorSearchResult> donors = new TreeSet<>();

		if (StringUtils.isBlank(lastName) || StringUtils.isBlank(firstName))
			throw new IllegalArgumentException("Both last name and first name are required.");

		List<DonorSearchResult> donorMatches = donorDAO.findByCriteria(
				donorTypeDAO.findByLookup(DonorTypeValue.INDIVIDUAL), firstName, null, lastName, null, null, state,
				null, null, null, null);
		donors.addAll(donorMatches);

		if (excludeDonorId != null) {
			donors = donors.stream().filter(p -> !p.getDonor().getId().equals(excludeDonorId))
					.collect(Collectors.toCollection(TreeSet::new));
		}

		return donors;
	}

}
