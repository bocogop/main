package org.bocogop.wr.web.donation;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.annotation.JsonView;

import org.bocogop.shared.model.Permission;
import org.bocogop.wr.model.donation.DonationLog;
import org.bocogop.wr.model.donation.DonationLog.DonationLogSummaryView;
import org.bocogop.wr.web.AbstractAppController;
import org.bocogop.wr.web.breadcrumbs.Breadcrumb;

@Controller
public class DonationLogController extends AbstractAppController {

	@RequestMapping("/manageDonationLog.htm")
	@Breadcrumb("E-Donations Received")
	@PreAuthorize("hasAuthority('" + Permission.EDONATION_MANAGE + "')")
	public String listDonationLogs(ModelMap model) {
		return "donationLogList";
	}

	@RequestMapping("/donationLogList")
	@JsonView(DonationLogSummaryView.Basic.class)
	public @ResponseBody List<DonationLog> getDonationLogList() {
		List<DonationLog> results = donationLogDAO
				.findDonationLogByStationNumber(getFacilityContext().getStationNumber(), true);
		return results;
	}

	@RequestMapping("/donationLogIdSaveToSession")
	@PreAuthorize("hasAuthority('" + Permission.EDONATION_MANAGE + "')")
	public @ResponseBody boolean saveDonationLogIdToSession(@RequestParam long donationLogId, 
			ModelMap model, HttpServletRequest request) {
		request.getSession().setAttribute(DonationController.DONATION_DEFAULTS, donationLogId);
		return true;
	}
}
