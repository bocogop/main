package org.bocogop.wr.web.donation;

import static org.bocogop.wr.model.donation.DonorType.DonorTypeValue.INDIVIDUAL;
import static org.bocogop.wr.model.donation.DonorType.DonorTypeValue.ORGANIZATION;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import org.bocogop.shared.service.validation.ServiceValidationException;
import org.bocogop.shared.util.WebUtil;
import org.bocogop.wr.model.donation.DonGenPostFund;
import org.bocogop.wr.model.donation.DonationDetail;
import org.bocogop.wr.model.donation.DonationLog;
import org.bocogop.wr.model.donation.DonationSummary;
import org.bocogop.wr.model.donation.DonationSummary.DonationSummaryView;
import org.bocogop.wr.model.donation.DonationType.DonationTypeValue;
import org.bocogop.wr.model.donation.Donor;
import org.bocogop.wr.model.donation.DonorType;
import org.bocogop.wr.model.facility.Facility;
import org.bocogop.wr.model.organization.AbstractBasicOrganization;
import org.bocogop.wr.util.DateUtil;
import org.bocogop.wr.web.AbstractAppController;
import org.bocogop.wr.web.breadcrumbs.Breadcrumb;
import org.bocogop.wr.web.validation.ValidationException;

@Controller
@SessionAttributes(value = { AbstractAppController.DEFAULT_COMMAND_NAME, "donationListCommand" })
public class DonationController extends AbstractAppController {

	public static final String DONATION_DEFAULTS = "donationLogId";

	@Autowired
	private DonationValidator donationValidator;

	@Value("${donationSummaryList.maxResults}")
	private int maxResults;
	@Value("${donationSummaryList.normal.defaultStartDaysInPast}")
	private int normalDefaultStartDaysInPast;
	@Value("${donationSummaryList.thankyou.defaultStartDaysInPast}")
	private int thankYouDefaultStartDaysInPast;

	@RequestMapping("/donationEdit.htm")
	@Breadcrumb("Edit Donation")
	@PreAuthorize("hasAuthority('" + Permission.DONATION_READ + "')")
	public String editDonation(@RequestParam long id, @RequestParam(required = false) String fromPage,
			@RequestParam(required = false) Long printMemo, @RequestParam(required = false) Long printReceipt,
			@RequestParam(required = false) Long printThankYou, @RequestParam(required = false) String printFormat,
			ModelMap model, HttpServletRequest request) {
		model.addAttribute("printMemo", printMemo);
		model.addAttribute("printReceipt", printReceipt);
		model.addAttribute("printThankYou", printThankYou);
		model.addAttribute("printFormat", printFormat);

		DonationSummary donation = donationSummaryDAO.findRequiredByPrimaryKey(id);

		DonationCommand command = new DonationCommand(donation);
		command.setCurrentFiscalYearStartDate(dateUtil.getCurrentFiscalYearStartDate(getFacilityTimeZone()));
		command.setCurrentFiscalYearEndDate(dateUtil.getCurrentFiscalYearEndDate(getFacilityTimeZone()));
		command.setFromPage(fromPage);

		// Take care the case if donation type = item/activity
		if (donation.getDonationType().getDonationType().equalsIgnoreCase("Item")
				|| donation.getDonationType().getDonationType().equalsIgnoreCase("Activity"))
			command.setDonationDetail4(getDonationDetailForItemActivity(donation));

		setFormAsReadOnlyUnlessUserHasPermissions(model, PermissionType.DONATION_CREATE);

		model.addAttribute(DEFAULT_COMMAND_NAME, command);
		createReferenceData(model, command, false);
		// model.addAttribute("newDonationSummary", false);
		return "editDonation";
	}

	@RequestMapping("/donationCreate.htm")
	@Breadcrumb("Create Donation")
	@PreAuthorize("hasAuthority('" + Permission.DONATION_CREATE + "')")
	public String createDonation(@RequestParam long donorId, @RequestParam(required = false) String fromPage,
			ModelMap model, HttpServletRequest request) {
		DonationSummary donation = new DonationSummary();
		donation.setDonationDate(getTodayAtFacility());

		Facility facility = getRequiredFacilityContext();
		donation.setFacility(facility);

		Donor donor = donorDAO.findRequiredByPrimaryKey(donorId);
		donation.setDonor(donor);

		donation.setSalutation(donor.getIndividualSalutation());

		DonationCommand command = new DonationCommand(donation);

		Long donationLogId = (Long) request.getSession().getAttribute(DONATION_DEFAULTS);
		DonationLog donationLog = null;
		if (donationLogId != null)
			donationLog = donationLogDAO.findByPrimaryKey(donationLogId);

		if (donationLog != null) {
			// pre-populate donation screen for add e-donation
			donation.setDonationDate(donationLog.getTransactionDateOnly());
			donation.setEpayTrackingID(donationLog.getTrackingId());
			donation.setDonationType(donationTypeDAO.findByLookup(DonationTypeValue.EDONATION));
			donation.setAdditionalComments(donationLog.getAdditionalInfo());

			DonationDetail donationDetail = new DonationDetail();
			donationDetail.setDonGenPostFund(
					donGenPostFundDAO.findByProgramCode(donationLog.getProgramField(), facility.getId()));
			donationDetail.setDonationValue(
					donationLog.getDonationAmount() != null ? donationLog.getDonationAmount() : new BigDecimal(0));
			donationDetail.setDonationSummary(donation);

			command.setDonationDetail1(donationDetail);
		}

		/*
		 * DonationReference donReference = donationReferenceDAO
		 * .findByInstitutionAndRef(getSiteContext().getStationNumber(),
		 * "none");
		 * 
		 * if (donation.getDonReference() == null)
		 * donation.setDonReference(donReference);
		 */

		command.setCurrentFiscalYearStartDate(dateUtil.getCurrentFiscalYearStartDate(getFacilityTimeZone()));
		command.setCurrentFiscalYearEndDate(dateUtil.getCurrentFiscalYearEndDate(getFacilityTimeZone()));
		command.setFromPage(fromPage);

		model.addAttribute(DEFAULT_COMMAND_NAME, command);
		createReferenceData(model, command, false);
		return "createDonation";
	}

	@RequestMapping("/donationDetails")
	@PreAuthorize("hasAuthority('" + Permission.DONATION_READ + "')")
	public @ResponseBody List<DonationDetail> getDonationDetailsList(@RequestParam long donationSummaryId) {
		if (donationSummaryId == 0)
			return new ArrayList<DonationDetail>();
		List<DonationDetail> donationDetailList = donationDetailDAO.findByDonationSummaryId(donationSummaryId, false);
		return donationDetailList;
	}

	@RequestMapping("/donationDelete.htm")
	@PreAuthorize("hasAuthority('" + Permission.DONATION_CREATE + "')")
	public String deleteDonationSummary(@RequestParam long donationSummaryId, @RequestParam long donorId)
			throws ServiceValidationException {
		donationService.deleteDonationSummary(donationSummaryId);

		return "redirect:/donorEdit.htm?id=" + donorId;
	}

	@RequestMapping("/deleteDonationDetail")
	@PreAuthorize("hasAuthority('" + Permission.DONATION_CREATE + "')")
	public @ResponseBody boolean deleteDonationDetail(@RequestParam long donationDetailId)
			throws ServiceValidationException {
		donationService.deleteDonationDetail(donationDetailId);
		return true;
	}

	@RequestMapping("/donationSummarySubmit.htm")
	@PreAuthorize("hasAuthority('" + Permission.DONATION_CREATE + "')")
	public String submitDonationSummary(@ModelAttribute(DEFAULT_COMMAND_NAME) DonationCommand command,
			BindingResult result, SessionStatus status, ModelMap model, HttpServletRequest request)
			throws ValidationException {
		DonationSummary donationSummary = command.getDonationSummary();

		// Clean hidden fields
		donationSummary = cleanHiddenFields(donationSummary, command);
		if (command.getOrganizationId() != null) {
			AbstractBasicOrganization org = organizationDAO.findRequiredByPrimaryKey(command.getOrganizationId());
			donationSummary.setOrganization(org);
		} else {
			donationSummary.setOrganization(null);
		}

		/* Validation step (JSR303, other custom logic in the validator) */

		donationValidator.validate(command, result, false, "donationSummary");
		boolean hasErrors = result.hasErrors();

		if (!hasErrors) {
			try {
				donationSummary = donationService.saveOrUpdateDonationSummary(donationSummary, true,
						command.getDonationDetail1(), command.getDonationDetail2(), command.getDonationDetail3(),
						command.getDonationDetail4());
				userNotifier.notifyUserOnceWithMessage(request, getMessage("donationSummary.update.success"));
			} catch (ServiceValidationException e) {
				webValidationService.handle(e, result);
				hasErrors = true;
			}
		}

		if (hasErrors) {
			createReferenceData(model, command, hasErrors);
			return "editDonation";
		} else {
			status.setComplete();
			String path = null;

			if ("donor".equalsIgnoreCase(command.getFromPage())) {
				path = "redirect:/donorEdit.htm?id=" + donationSummary.getDonor().getId();
			} else if (request.getSession().getAttribute(DONATION_DEFAULTS) != null) {
				// if session contains donationLogId, then we are in the process
				// of adding an edonation
				// upon completion of adding the edonation, we can clear the
				// session and return to the
				// edonation list page
				request.getSession().removeAttribute(DONATION_DEFAULTS);
				path = "redirect:/manageDonationLog.htm";
			} else {
				path = "redirect:/donationEdit.htm?id=" + donationSummary.getId();
			}

			if (command.isPrintMemo())
				path += "&printMemo=" + donationSummary.getId();
			if (command.isPrintReceipt())
				path += "&printReceipt=" + donationSummary.getId();
			if (command.isPrintThankYou())
				path += "&printThankYou=" + donationSummary.getId();
			if (StringUtils.isNotBlank(command.getPrintFormat()))
				path += "&printFormat=" + command.getPrintFormat();

			return path;
		}
	}

	@RequestMapping("/donationDetailCreateOrUpdate")
	@PreAuthorize("hasAuthority('" + Permission.DONATION_CREATE + "')")
	public @ResponseBody DonationDetail createOrUpdateDonationDetail(@RequestParam Long donationSummaryId,
			@RequestParam(required = false) Long donationDetailId, @RequestParam DonGenPostFund generalPostFund,
			@RequestParam BigDecimal donationValue) throws ServiceValidationException {

		DonationDetail donationDetail = null;

		if (donationDetailId == null) {
			donationDetail = new DonationDetail();
			DonationSummary donationSummary = donationSummaryDAO.findRequiredByPrimaryKey(donationSummaryId);
			donationDetail.setDonationSummary(donationSummary);
		} else {
			donationDetail = donationDetailDAO.findRequiredByPrimaryKey(donationDetailId.longValue());
		}

		donationDetail.setDonGenPostFund(generalPostFund);
		donationDetail.setDonationValue(donationValue);
		donationDetail = donationService.saveOrUpdateDonationDetail(donationDetail);

		return donationDetail;
	}

	@RequestMapping("/donation/search")
	@PreAuthorize("hasAuthority('" + Permission.DONATION_READ + "')")
	@JsonView(DonationSummaryView.Search.class)
	public @ResponseBody SortedSet<DonationSummary> searchForDonations(
			@RequestParam(required = false) @DateTimeFormat(pattern = DateUtil.TWO_DIGIT_DATE_ONLY) LocalDate beginDate,
			@RequestParam(required = false) @DateTimeFormat(pattern = DateUtil.TWO_DIGIT_DATE_ONLY) LocalDate endDate,
			@RequestParam(required = false) String donorName, @RequestParam(required = false) Long donorTypeId,
			@RequestParam(required = false) Long donationId) {
		SortedSet<DonationSummary> results = new TreeSet<>();
		if (donationId != null) {
			DonationSummary ds = donationSummaryDAO.findByPrimaryKey(donationId);
			if (ds != null && ds.getFacility().getFacility().getId().equals(getFacilityContextId()))
				results.add(ds);
		} else {
			Collection<DonorType> donorTypes = null;
			if (donorTypeId != null)
				donorTypes = Arrays.asList(donorTypeDAO.findRequiredByPrimaryKey(donorTypeId));

			results = donationSummaryDAO.findByCriteria(getFacilityContextId(), donorName, null, beginDate, endDate,
					donorTypes, true, true);
		}
		return results;
	}

	@RequestMapping("/changeDonor")
	@PreAuthorize("hasAuthority('" + Permission.DONATION_CREATE + "')")
	public @ResponseBody DonationSummary changeDonor(@RequestParam Long donationSummaryId, @RequestParam Long donorId,
			HttpServletRequest request) throws ServiceValidationException {

		DonationSummary donationSummary = donationSummaryDAO.findRequiredByPrimaryKey(donationSummaryId);
		Donor donor = donorDAO.findRequiredByPrimaryKey(donorId);
		if (4 == donor.getDonorType().getId() && donor.getOrganization() != null
				&& donor.getOrganization().isInactive()) {
			userNotifier.notifyUserOnceWithMessage(request, getMessage("changeDonor.organization.inactive"));
			return donationSummary;
		}
		donationSummary.setDonor(donor);
		donationSummary = donationService.saveOrUpdateDonationSummary(donationSummary, false, null, null, null, null);
		userNotifier.notifyUserOnceWithMessage(request, getMessage("changeDonor.update.success"));
		return donationSummary;
	}

	private void createReferenceData(ModelMap model, DonationCommand donationCommand, boolean hasErrors) {
		model.put("allDonationTypes", donationTypeDAO.findAllSorted());
		// to avoid the lazy loading error by getting facility from facilityDAO
		Facility facility = donationCommand.getDonationSummary().getFacility();

		model.put("allDonationReferences", facility.getDonationReferences());
		model.put("allDonGenPostFunds", facility.getDonGenPostfundsByStatus(true));
		model.put("allCreditCardTypes", stdCreditCardTypeDAO.findAllSorted());
		WebUtil.addClassConstantsToModel(DonGenPostFund.class, model);
		DonationSummary donationSum = donationCommand.getDonationSummary();

		if (!formIsReadOnly(model) && !hasErrors && donationSum != null && donationSum.getDonationDate() != null) {
			boolean editable = dateUtil.isDonationSummaryEditable(donationSum.getDonationDate(), getFacilityTimeZone());

			if (editable && isNotEditablePerMoreRules(donationSum)) {
				editable = false;
			}
			if (!editable)
				setFormAsReadOnly(model, !editable);
		}

		appendCommonReportParams(model);
	}

	// Donor Type 2 - Organization & Individual; 3 - Other Groups & Individual;
	// 5 - Other Groups; 1 - individual; 4 - organization

	private boolean isNotEditablePerMoreRules(DonationSummary donationSum) {
		if (!getRequiredSiteContext().getStationNumber().equalsIgnoreCase(donationSum.getFacility().getStationNumber())
				|| 2 == donationSum.getDonor().getDonorType().getId()
				|| 3 == donationSum.getDonor().getDonorType().getId()
				|| 5 == donationSum.getDonor().getDonorType().getId()
				|| (1 == donationSum.getDonor().getDonorType().getId() && donationSum.getOrganization() != null
						&& donationSum.getOrganization().isInactive())
				|| (4 == donationSum.getDonor().getDonorType().getId()
						&& donationSum.getDonor().getOrganization() != null
						&& donationSum.getDonor().getOrganization().isInactive())) {
			return true;
		}
		return false;
	}

	private DonationSummary cleanHiddenFields(DonationSummary donationSummary, DonationCommand command) {
		// if the donation type = cash
		if (donationSummary.getDonationType().getDonationType().equalsIgnoreCase("Cash")) {
			donationSummary.setCheckNumber(null);
			donationSummary.setCheckDate(null);
			donationSummary.setStdCreditCardType(null);
			donationSummary.setCreditCardTransactionId(null);
			donationSummary.setEpayTrackingID(null);
			command.setDonationDetail4(null);
		}
		// if the donation type = Credit Card
		if (donationSummary.getDonationType().getDonationType().equalsIgnoreCase("Credit Card")) {
			donationSummary.setCheckNumber(null);
			donationSummary.setCheckDate(null);
			donationSummary.setEpayTrackingID(null);
			command.setDonationDetail4(null);
		}
		// if the donation type = Check
		if (donationSummary.getDonationType().getDonationType().equalsIgnoreCase("Check")) {
			donationSummary.setStdCreditCardType(null);
			donationSummary.setCreditCardTransactionId(null);
			donationSummary.setEpayTrackingID(null);
			command.setDonationDetail4(null);
		}
		// if the donation type = E-Donation
		if (donationSummary.getDonationType().getDonationType().equalsIgnoreCase("E-Donation")) {
			donationSummary.setCheckNumber(null);
			donationSummary.setCheckDate(null);
			donationSummary.setStdCreditCardType(null);
			donationSummary.setCreditCardTransactionId(null);
			command.setDonationDetail4(null);
		}
		// if the donation type = Item
		if (donationSummary.getDonationType().getDonationType().equalsIgnoreCase("Item")) {
			donationSummary.setCheckNumber(null);
			donationSummary.setCheckDate(null);
			donationSummary.setStdCreditCardType(null);
			donationSummary.setCreditCardTransactionId(null);
			donationSummary.setEpayTrackingID(null);
			donationSummary.setInMemoryOf(null);
			donationSummary.setFamilyContact(null);
			donationSummary.setFamilyContactAddress(null);
			donationSummary.setFamilyContactCity(null);
			donationSummary.setFamilyContactState(null);
			donationSummary.setFamilyContactZip(null);
			donationSummary.setCc1(null);
			donationSummary.setCc2(null);
			donationSummary.setCc3(null);
			donationSummary.setCc4(null);
			donationSummary.setCc5(null);
			command.setDonationDetail1(null);
			command.setDonationDetail2(null);
			command.setDonationDetail3(null);
		}
		// if the donation type = Activity
		if (donationSummary.getDonationType().getDonationType().equalsIgnoreCase("Activity")) {
			donationSummary.setCheckNumber(null);
			donationSummary.setCheckDate(null);
			donationSummary.setStdCreditCardType(null);
			donationSummary.setCreditCardTransactionId(null);
			donationSummary.setEpayTrackingID(null);
			donationSummary.setInMemoryOf(null);
			donationSummary.setFamilyContact(null);
			donationSummary.setFamilyContactAddress(null);
			donationSummary.setFamilyContactCity(null);
			donationSummary.setFamilyContactState(null);
			donationSummary.setFamilyContactZip(null);
			donationSummary.setCc1(null);
			donationSummary.setCc2(null);
			donationSummary.setCc3(null);
			donationSummary.setCc4(null);
			donationSummary.setCc5(null);
			command.setDonationDetail1(null);
			command.setDonationDetail2(null);
			command.setDonationDetail3(null);
		}
		return donationSummary;
	}

	DonationDetail getDonationDetailForItemActivity(DonationSummary donation) {
		List<DonationDetail> donationDetailList = donationDetailDAO.findByDonationSummaryId(donation.getId(), true);
		if (!donationDetailList.isEmpty()) {
			DonationDetail donDetail = donationDetailList.get(0);
			if ((donDetail.getDonGenPostFund() == null
					|| donDetail.getDonGenPostFund().getGeneralPostFund().equalsIgnoreCase("None"))
					&& donDetail.getDonationValue() != null)
				return donationDetailList.get(0);
		}
		return null;
	}

	/*
	 * Necessary to populate form in session once before we activate the
	 * listDonations() GET method below - otherwise it complains that the
	 * session attribute is null. I wish there was a "required = false"
	 * attribute to this annotation. CPB
	 */
	@ModelAttribute("donationListCommand")
	public DonationListCommand populateForm() {
		return new DonationListCommand();
	}

	@RequestMapping(path = "/donationList.htm", method = RequestMethod.GET)
	@Breadcrumb("List Donations")
	@PreAuthorize("hasAuthority('" + Permission.DONATION_READ + "')")
	public String listDonations(@ModelAttribute("donationListCommand") DonationListCommand command, ModelMap model,
			@RequestParam(defaultValue = "normal") String listDonationsMode,
			@RequestParam(required = false) @DateTimeFormat(pattern = DateUtil.TWO_DIGIT_DATE_ONLY) LocalDate startDate,
			@RequestParam(required = false) @DateTimeFormat(pattern = DateUtil.TWO_DIGIT_DATE_ONLY) LocalDate endDate,
			@RequestParam(required = false) String donorName, @RequestParam(required = false) Long donationId,
			@RequestParam(name = "donorTypes[]", required = false) List<Long> donorTypeIds,
			@RequestParam(name = "acknowledgementStatus[]", required = false) List<Boolean> acknowledgementStatuses) {
		long facilityId = getFacilityContextId();

		List<DonorType> donorTypes = new ArrayList<>(donorTypeDAO.findByPrimaryKeys(donorTypeIds).values());

		if ("thankyou".equals(listDonationsMode)) {
			startDate = getTodayAtFacility().minusDays(thankYouDefaultStartDaysInPast);
			donorTypes = new ArrayList<>(donorTypeDAO.findByLookups(INDIVIDUAL, ORGANIZATION).values());
			command = new DonationListCommand(facilityId, listDonationsMode, startDate, endDate, donorName, donationId,
					donorTypes, null);
			runSearch(command);
		} else if ("timeperiodsearch".equals(listDonationsMode)) {
			command = new DonationListCommand(facilityId, listDonationsMode, startDate, endDate, null, null,
					donorTypeDAO.findAll(), null);
			runSearch(command);
		} else if (command == null || (command.getMode() != null && !command.getMode().equals(listDonationsMode))
				|| command.getFacilityId() != facilityId) {
			Boolean acknowledgementStatus = null;
			if (acknowledgementStatuses != null && acknowledgementStatuses.size() == 1)
				acknowledgementStatus = acknowledgementStatuses.get(0);

			if (startDate == null)
				startDate = getTodayAtFacility().minusDays(normalDefaultStartDaysInPast);
			if (CollectionUtils.isEmpty(donorTypes))
				donorTypes = new ArrayList<>(donorTypeDAO.findByLookups(INDIVIDUAL, ORGANIZATION).values());

			command = new DonationListCommand(facilityId, listDonationsMode, startDate, endDate, donorName, donationId,
					donorTypes, acknowledgementStatus);
		} else {
			runSearch(command);
		}

		model.addAttribute("donationListCommand", command);
		addReferenceDataForDonationList(command, model);

		return "donationSummaryList";
	}

	public void runSearch(DonationListCommand command) {
		SortedSet<DonationSummary> results = donationSummaryDAO.findByCriteria(getFacilityContextId(),
				command.getDonorName(), command.getDonationId(), command.getStartDate(), command.getEndDate(),
				command.getDonorTypes(), command.isIncludeAcknowledged(), command.isIncludeUnacknowledged());
		command.setSearched(true);
		command.setDonations(results);
	}

	@RequestMapping(path = "/donationList.htm", method = RequestMethod.POST)
	@Breadcrumb("List Donations")
	@PreAuthorize("hasAuthority('" + Permission.DONATION_READ + "')")
	public String submitDonationListSearch(@ModelAttribute("donationListCommand") DonationListCommand command,
			BindingResult result, SessionStatus status, ModelMap model) {
		model.put("maxResultsExceeded", false);
		if (result.hasErrors()) {
			command.setSearched(false);
			command.getDonations().clear();
			addReferenceDataForDonationList(command, model);
			return "donationSummaryList";
		}

		runSearch(command);

		addReferenceDataForDonationList(command, model);
		return "donationSummaryList";
	}

	private void addReferenceDataForDonationList(DonationListCommand command, ModelMap model) {
		List<DonorType> all = donorTypeDAO.findAll();
		model.put("allCurrentDonorTypes",
				all.stream().filter(p -> !p.getLookupType().isLegacy()).collect(Collectors.toList()));
		model.put("allLegacyDonorTypes",
				all.stream().filter(p -> p.getLookupType().isLegacy()).collect(Collectors.toList()));
		if (command.isSearched())
			model.put("maxResultsExceeded", command.getDonations().size() == maxResults);
		appendCommonReportParams(model);
	}

}
