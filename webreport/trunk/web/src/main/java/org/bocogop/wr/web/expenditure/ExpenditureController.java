package org.bocogop.wr.web.expenditure;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttributes;

import com.fasterxml.jackson.annotation.JsonView;

import org.bocogop.shared.model.Permission;
import org.bocogop.shared.model.Permission.PermissionType;
import org.bocogop.shared.service.validation.ServiceValidationException;
import org.bocogop.shared.util.CollectionUtil;
import org.bocogop.shared.util.SecurityUtil;
import org.bocogop.shared.util.CollectionUtil.SynchronizeCollectionsOps;
import org.bocogop.shared.util.WebUtil;
import org.bocogop.wr.model.donation.DonationSummary;
import org.bocogop.wr.model.expenditure.Expenditure;
import org.bocogop.wr.model.expenditure.Expenditure.ExpenditureView;
import org.bocogop.wr.model.expenditure.ExpenditureDonationAssociation;
import org.bocogop.wr.model.expenditure.UnitType;
import org.bocogop.wr.util.DateUtil;
import org.bocogop.wr.web.AbstractAppController;
import org.bocogop.wr.web.breadcrumbs.Breadcrumb;

@Controller
@SessionAttributes(value = { AbstractAppController.DEFAULT_COMMAND_NAME })
public class ExpenditureController extends AbstractAppController {

	@Value("${expenditureList.maxResults}")
	private int maxResults;
	@Value("${expenditureList.defaultStartDaysInPast}")
	private int defaultStartDaysInPast;

	@RequestMapping("/expenditure")
	@JsonView(ExpenditureView.Search.class)
	public @ResponseBody Expenditure getExpenditure(@RequestParam long id) {
		return expenditureDAO.findByPrimaryKey(id);
	}

	@RequestMapping("/expenditure/saveOrUpdate")
	public @ResponseBody Boolean saveOrUpdate(@RequestParam Long id, @RequestParam String transactionId,
			@RequestParam String purchaseOrder, @RequestParam long donGenPostFundId,
			@RequestParam String originatorUserName, @RequestParam String vendor,
			@RequestParam @DateTimeFormat(pattern = DateUtil.TWO_DIGIT_DATE_ONLY) LocalDate requestDate,
			@RequestParam BigDecimal amount, @RequestParam String description, @RequestParam String comments,
			@RequestParam Integer quantity, @RequestParam UnitType unit, @RequestParam BigDecimal unitPrice,
			@RequestParam(required = false, name = "donationSummaryIds[]") Long[] donationSummaryIds)
			throws ServiceValidationException {
		Expenditure e = null;
		if (id != null) {
			e = expenditureDAO.findRequiredByPrimaryKey(id);
		} else {
			e = new Expenditure();
		}

		e.setTransactionId(transactionId);
		e.setPurchaseOrderNumber(purchaseOrder);
		e.setDonGenPostFund(donGenPostFundDAO.findRequiredByPrimaryKey(donGenPostFundId));
		e.setOriginator(appUserService.createOrRetrieveUser(originatorUserName, null));
		e.setRequestDate(requestDate);
		e.setAmount(amount);
		e.setDescription(description);
		e.setVendor(vendor);
		e.setComments(comments);
		e.setFacility(getFacilityContext());
		e.setQuantity(quantity);
		e.setUnit(unit);
		e.setUnitPrice(unitPrice);

		if (donationSummaryIds == null)
			donationSummaryIds = new Long[0];

		final Expenditure eFinal = e;
		Collection<DonationSummary> donationSummaries = donationSummaryDAO
				.findByPrimaryKeys(Arrays.asList(donationSummaryIds)).values();
		CollectionUtil.synchronizeCollections(e.getDonationAssociations(), donationSummaries,
				new SynchronizeCollectionsOps<ExpenditureDonationAssociation, DonationSummary>() {
					@Override
					public ExpenditureDonationAssociation convert(DonationSummary u) {
						return new ExpenditureDonationAssociation(eFinal, u);
					}
				});

		e = expenditureService.saveOrUpdate(e);

		return true;
	}

	@RequestMapping("/expenditure/delete")
	public @ResponseBody boolean delete(@RequestParam long id) throws ServiceValidationException {
		expenditureService.delete(id);
		return true;
	}

	@RequestMapping(path = "/expenditureList.htm", method = RequestMethod.GET)
	@Breadcrumb("List Expenditures")
	@PreAuthorize("hasAuthority('" + Permission.EXPENDITURE_CREATE + "')")
	public String listExpenditures(ModelMap model,
			@RequestParam(required = false) @DateTimeFormat(pattern = DateUtil.TWO_DIGIT_DATE_ONLY) LocalDate startDate,
			@RequestParam(required = false) @DateTimeFormat(pattern = DateUtil.TWO_DIGIT_DATE_ONLY) LocalDate endDate,
			@RequestParam(required = false) Long donGenPostFundId) {
		// defaults
		if (startDate == null)
			startDate = getTodayAtFacility().minusDays(defaultStartDaysInPast);
		model.put("startDate", startDate.format(DateUtil.TWO_DIGIT_DATE_ONLY_FORMAT));
		model.put("endDate", endDate == null ? null : endDate.format(DateUtil.TWO_DIGIT_DATE_ONLY_FORMAT));
		model.put("donGenPostFundId", donGenPostFundId);
		model.put("earliestEditDate", dateUtil.getEarliestAcceptableDateEntryAsOfNow(getFacilityTimeZone()));
		
		addReferenceDataForExpenditureList(model);

		return "expenditureList";
	}

	private void addReferenceDataForExpenditureList(ModelMap model) {
		model.put("maxResults", maxResults);
		model.put("canDelete", SecurityUtil.hasAllPermissionsAtCurrentFacility(PermissionType.EXPENDITURE_DELETE));
		populateReferenceDataForPopup(model);
		appendCommonReportParams(model);
	}

	@RequestMapping("/expenditure/list")
	@JsonView(ExpenditureView.Search.class)
	public @ResponseBody List<Expenditure> runSearch(
			@RequestParam @DateTimeFormat(pattern = DateUtil.TWO_DIGIT_DATE_ONLY) LocalDate startDate,
			@RequestParam @DateTimeFormat(pattern = DateUtil.TWO_DIGIT_DATE_ONLY) LocalDate endDate,
			@RequestParam Long donGenPostFundId) {
		if (donGenPostFundId != null && donGenPostFundId == -1)
			donGenPostFundId = null;

		List<Expenditure> results = expenditureDAO.findByCriteria(getFacilityContextId(), donGenPostFundId, startDate,
				endDate);
		return results;
	}

	public void populateReferenceDataForPopup(ModelMap model) {
		model.put("allGPFs", donGenPostFundDAO.findByFacility(getFacilityContextId()));
		WebUtil.addEnumToModel(UnitType.class, model);
		model.put("allDonorTypes", donorTypeDAO.findAll());
	}

}
