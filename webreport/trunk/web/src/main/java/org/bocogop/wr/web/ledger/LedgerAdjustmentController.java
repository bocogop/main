package org.bocogop.wr.web.ledger;

import java.math.BigDecimal;
import java.time.LocalDate;
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

import com.fasterxml.jackson.annotation.JsonView;

import org.bocogop.shared.model.Permission;
import org.bocogop.shared.model.Permission.PermissionType;
import org.bocogop.shared.service.validation.ServiceValidationException;
import org.bocogop.shared.util.SecurityUtil;
import org.bocogop.wr.model.expenditure.LedgerAdjustment;
import org.bocogop.wr.model.expenditure.LedgerAdjustment.LedgerAdjustmentView;
import org.bocogop.wr.util.DateUtil;
import org.bocogop.wr.web.AbstractAppController;
import org.bocogop.wr.web.breadcrumbs.Breadcrumb;

@Controller
public class LedgerAdjustmentController extends AbstractAppController {

	@Value("${ledgerAdjustmentList.maxResults}")
	private int maxResults;
	@Value("${ledgerAdjustmentList.defaultStartDaysInPast}")
	private int defaultStartDaysInPast;

	@RequestMapping("/ledgerAdjustment/delete")
	public @ResponseBody boolean delete(@RequestParam long id) throws ServiceValidationException {
		ledgerAdjustmentService.delete(id);
		return true;
	}

	@RequestMapping("/ledgerAdjustment/saveOrUpdate")
	public @ResponseBody boolean saveOrUpdate(@RequestParam Long id, @RequestParam long donGenPostFundId,
			@RequestParam @DateTimeFormat(pattern = DateUtil.TWO_DIGIT_DATE_ONLY) LocalDate requestDate,
			@RequestParam BigDecimal amount, @RequestParam String justification) throws ServiceValidationException {
		LedgerAdjustment la = null;
		if (id != null) {
			la = ledgerAdjustmentDAO.findRequiredByPrimaryKey(id);
		} else {
			la = new LedgerAdjustment();
		}

		la.setDonGenPostFund(donGenPostFundDAO.findRequiredByPrimaryKey(donGenPostFundId));
		la.setRequestDate(requestDate);
		la.setAmount(amount);
		la.setJustification(justification);
		la.setFacility(getFacilityContext());

		la = ledgerAdjustmentService.saveOrUpdate(la);

		return true;
	}

	@RequestMapping(path = "/ledgerAdjustmentList.htm", method = RequestMethod.GET)
	@Breadcrumb("List Ledger Adjustments")
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

		addReferenceDataForLedgerAdjustmentList(model);
		return "ledgerAdjustmentList";
	}

	private void addReferenceDataForLedgerAdjustmentList(ModelMap model) {
		model.put("maxResults", maxResults);
		model.put("canDelete", SecurityUtil.hasAllPermissionsAtCurrentFacility(PermissionType.EXPENDITURE_DELETE));
		populateReferenceDataForPopup(model);
		appendCommonReportParams(model);
	}

	@RequestMapping("/ledgerAdjustment/list")
	@JsonView(LedgerAdjustmentView.Search.class)
	public @ResponseBody List<LedgerAdjustment> runSearch(
			@RequestParam @DateTimeFormat(pattern = DateUtil.TWO_DIGIT_DATE_ONLY) LocalDate startDate,
			@RequestParam @DateTimeFormat(pattern = DateUtil.TWO_DIGIT_DATE_ONLY) LocalDate endDate,
			@RequestParam Long donGenPostFundId) {
		if (donGenPostFundId != null && donGenPostFundId == -1)
			donGenPostFundId = null;

		List<LedgerAdjustment> results = ledgerAdjustmentDAO.findByCriteria(getFacilityContextId(), donGenPostFundId,
				startDate, endDate);
		return results;
	}

	public void populateReferenceDataForPopup(ModelMap model) {
		model.put("allGPFs", donGenPostFundDAO.findByFacility(getFacilityContextId()));
	}
}
