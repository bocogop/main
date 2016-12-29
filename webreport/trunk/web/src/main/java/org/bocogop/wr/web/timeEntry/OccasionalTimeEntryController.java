package org.bocogop.wr.web.timeEntry;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.annotation.JsonView;

import org.bocogop.shared.model.Permission.PermissionType;
import org.bocogop.shared.service.validation.ServiceValidationException;
import org.bocogop.wr.model.benefitingService.BenefitingServiceRole;
import org.bocogop.wr.model.organization.AbstractBasicOrganization;
import org.bocogop.wr.model.time.OccasionalWorkEntry;
import org.bocogop.wr.model.time.OccasionalWorkEntry.OccasionalWorkEntryView;
import org.bocogop.wr.persistence.queryCustomization.QueryCustomization;
import org.bocogop.wr.persistence.queryCustomization.fieldTypes.OccasionalWorkEntryAssociationFieldType;
import org.bocogop.wr.util.DateUtil;
import org.bocogop.wr.web.AbstractAppController;
import org.bocogop.wr.web.breadcrumbs.Breadcrumb;

@Controller
public class OccasionalTimeEntryController extends AbstractAppController {

	@RequestMapping("/occasionalTimeEntry.htm")
	@Breadcrumb("Occasional Time Entry")
	public String timeEntry(ModelMap model,
			@DateTimeFormat(pattern = DateUtil.TWO_DIGIT_DATE_ONLY) @RequestParam(required = false) LocalDate date) {
		LocalDate d = dateUtil.getEarliestAcceptableDateEntryAsOfNow(getFacilityTimeZone());
		model.put("assumePriorYearAfterMMDD",
				dateUtil.getPreviousFiscalYearEndDatePlusGracePeriod(getFacilityTimeZone())
						.format(DateUtil.TWO_DIGIT_MONTH_AND_DAY_ONLY_FORMAT));
		model.put("iso8601EarliestAcceptableDateEntry", d.toString());
		model.put("dateRequested", date);
		setFormAsReadOnlyUnlessUserHasPermissions(model, PermissionType.TIME_CREATE);
		return "occasionalTimeEntry";
	}

	@RequestMapping("/occasionalTimeEntry/timeReportByDate")
	// @PreAuthorize("hasAuthority('" + Permission.VOLUNTEER_VIEW + "')")
	@JsonView(OccasionalWorkEntryView.TimeReport.class)
	public @ResponseBody SortedSet<OccasionalWorkEntry> getTimeReportByDateAtWorkingFacility(
			@DateTimeFormat(pattern = DateUtil.TWO_DIGIT_DATE_ONLY) @RequestParam(required = false) LocalDate date) {

		SortedSet<OccasionalWorkEntry> r = new TreeSet<>(Comparator.reverseOrder());
		List<OccasionalWorkEntry> workEntries = occasionalWorkEntryDAO.findByCriteria(
				dateUtil.getCurrentFiscalYearStartDate(getFacilityTimeZone()), getTodayAtFacility(), null, null, null,
				getFacilityContextId(),
				new QueryCustomization() //
						.prefetchField(OccasionalWorkEntryAssociationFieldType.ORGANIZATION, "org") //
						.prefetchField(OccasionalWorkEntryAssociationFieldType.FACILITY, "f") //
						.prefetchField(OccasionalWorkEntryAssociationFieldType.BENEFITING_SERVICE, "bs") //
						.prefetchField(OccasionalWorkEntryAssociationFieldType.BENEFITING_SERVICE_ROLE, "bsr"))//
		;

		r.addAll(workEntries);

		return r;
	}

	@RequestMapping("/occasionalTimeEntry/post")
	// @PreAuthorize("hasAuthority('" + Permission.VOLUNTEER_VIEW + "')")
	public @ResponseBody boolean postTime(@RequestParam int numEntries, HttpServletRequest request)
			throws ServletRequestBindingException, ServiceValidationException {
		Set<Long> organizationIds = new HashSet<>();
		Set<Long> benefitingServiceRoleIds = new HashSet<>();
		for (int i = 0; i < numEntries; i++) {
			organizationIds.add(ServletRequestUtils.getRequiredLongParameter(request, "organizationId" + i));
			benefitingServiceRoleIds
					.add(ServletRequestUtils.getRequiredLongParameter(request, "benefitingServiceRoleId" + i));
		}
		Map<Long, AbstractBasicOrganization> organizationsMap = organizationDAO
				.findRequiredByPrimaryKeys(organizationIds);
		Map<Long, BenefitingServiceRole> benefitingServiceRoleMap = benefitingServiceRoleDAO
				.findRequiredByPrimaryKeys(benefitingServiceRoleIds);

		List<OccasionalWorkEntry> worksheet = new ArrayList<>();
		for (int i = 0; i < numEntries; i++) {
			long organizationId = ServletRequestUtils.getRequiredLongParameter(request, "organizationId" + i);
			long benefitingServiceRoleId = ServletRequestUtils.getRequiredLongParameter(request,
					"benefitingServiceRoleId" + i);
			int numberInGroup = ServletRequestUtils.getRequiredIntParameter(request, "numberInGroup" + i);
			LocalDate date = LocalDate.parse(ServletRequestUtils.getRequiredStringParameter(request, "date" + i),
					DateUtil.TWO_DIGIT_DATE_ONLY_FORMAT);
			BigDecimal hours = new BigDecimal(ServletRequestUtils.getRequiredStringParameter(request, "hours" + i));
			String comments = ServletRequestUtils.getStringParameter(request, "comments" + i);

			AbstractBasicOrganization organization = organizationsMap.get(organizationId);
			BenefitingServiceRole benefitingServiceRole = benefitingServiceRoleMap.get(benefitingServiceRoleId);
			worksheet.add(new OccasionalWorkEntry(organization, benefitingServiceRole, date, numberInGroup,
					hours.doubleValue(), comments));
		}

		occasionalWorkEntryService.saveOrUpdateMultiple(worksheet);

		return true;
	}

	@RequestMapping("/occasionalTimeEntry/delete")
	// @PreAuthorize("hasAuthority('" + Permission.VOLUNTEER_VIEW + "')")
	public @ResponseBody boolean deleteTime(@RequestParam long occasionalWorkEntryId)
			throws ServletRequestBindingException {
		occasionalWorkEntryService.delete(occasionalWorkEntryId);
		return true;
	}

	@RequestMapping("/occasionalTimeEntry/update")
	// @PreAuthorize("hasAuthority('" + Permission.VOLUNTEER_VIEW + "')")
	public @ResponseBody boolean updateTimeEntry(@RequestParam long id, @RequestParam long organizationId,
			@RequestParam long benefitingServiceRoleId, @RequestParam int numberInGroup,
			@DateTimeFormat(pattern = DateUtil.TWO_DIGIT_DATE_ONLY) @RequestParam LocalDate date,
			@RequestParam double hours, @RequestParam String comments) throws ServiceValidationException {
		OccasionalWorkEntry w = occasionalWorkEntryDAO.findRequiredByPrimaryKey(id);
		w.setDateWorked(date);
		w.setHoursWorked(hours);
		w.setNumberInGroup(numberInGroup);
		w.setComments(comments);

		AbstractBasicOrganization o = organizationDAO.findRequiredByPrimaryKey(organizationId);
		w.setOrganization(o);

		BenefitingServiceRole bsr = benefitingServiceRoleDAO.findRequiredByPrimaryKey(benefitingServiceRoleId);
		w.setBenefitingServiceRole(bsr);
		w.setBenefitingService(bsr.getBenefitingService());

		w = occasionalWorkEntryService.saveOrUpdate(w, false);

		return true;
	}

}
