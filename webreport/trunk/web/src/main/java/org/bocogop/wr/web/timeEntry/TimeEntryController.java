package org.bocogop.wr.web.timeEntry;

import static org.bocogop.wr.persistence.queryCustomization.fieldTypes.WorkEntryAssociationFieldType.ORGANIZATION;
import static org.bocogop.wr.persistence.queryCustomization.fieldTypes.WorkEntryAssociationFieldType.VOLUNTEER_ASSIGNMENT;
import static java.util.stream.Collectors.toMap;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.annotation.JsonView;

import org.bocogop.shared.model.AppUser;
import org.bocogop.shared.model.Permission;
import org.bocogop.shared.model.Permission.PermissionType;
import org.bocogop.shared.service.validation.ServiceValidationException;
import org.bocogop.wr.model.organization.AbstractBasicOrganization;
import org.bocogop.wr.model.time.AdjustedHoursEntry;
import org.bocogop.wr.model.time.WorkEntry;
import org.bocogop.wr.model.time.WorkEntry.WorkEntryView;
import org.bocogop.wr.model.volunteer.Volunteer;
import org.bocogop.wr.model.volunteer.VolunteerAssignment;
import org.bocogop.wr.persistence.queryCustomization.QueryCustomization;
import org.bocogop.wr.util.DateUtil;
import org.bocogop.wr.util.context.SessionUtil;
import org.bocogop.wr.web.AbstractAppController;
import org.bocogop.wr.web.breadcrumbs.Breadcrumb;

@Controller
public class TimeEntryController extends AbstractAppController {

	@RequestMapping("/timeEntry.htm")
	@Breadcrumb("Time Entry")
	@PreAuthorize("hasAnyAuthority('" + Permission.TIME_READ + ", " + Permission.TIME_CREATE + "')")
	public String timeEntry(ModelMap model, @RequestParam(required = false) Long volunteerId) {
		if (volunteerId != null) {
			Volunteer v = volunteerDAO.findRequiredByPrimaryKey(volunteerId);
			if (v.getStatus().isVolunteerInactiveOrTerminated())
				return "redirect:/volunteerEdit.htm?id=" + volunteerId;
		}
		LocalDate d = dateUtil.getEarliestAcceptableDateEntryAsOfNow(getFacilityTimeZone());
		model.put("assumePriorYearAfterMMDD",
				dateUtil.getPreviousFiscalYearEndDatePlusGracePeriod(getFacilityTimeZone())
						.format(DateUtil.TWO_DIGIT_MONTH_AND_DAY_ONLY_FORMAT));
		model.put("iso8601EarliestAcceptableDateEntry", d.toString());
		model.put("volIdRequested", volunteerId);
		setFormAsReadOnlyUnlessUserHasPermissions(model, PermissionType.TIME_CREATE);
		return "timeEntry";
	}

	@RequestMapping("/timeEntry/adjustedHours")
	@PreAuthorize("hasAnyAuthority('" + Permission.TIME_READ + ", " + Permission.TIME_CREATE + "')")
	public @ResponseBody Map<String, Object> getAdjustedHoursByVolunteer(@RequestParam long volunteerId) {
		Map<String, Object> results = new HashMap<>();
		SortedSet<AdjustedHoursEntry> entries = new TreeSet<>(
				adjustedHoursEntryDAO.findByCriteria(null, volunteerId, null));
		results.put("adjustedHoursEntries", entries);

		Set<String> usernames = entries.stream().map(p -> p.getCreatedBy()).collect(Collectors.toSet());
		Collection<AppUser> users = appUserDAO.findByCriteria(usernames, null, false, null, false, null, false);
		Map<String, String> usersByUsernameMap = users.stream()
				.collect(toMap(AppUser::getUsername, AppUser::getDisplayName, (a, b) -> a));

		Map<Long, String> usersByHoursIdMap = new HashMap<>();
		for (AdjustedHoursEntry entry : entries) {
			String createdBy = entry.getCreatedBy();
			String userName = usersByUsernameMap.get(createdBy);
			if (userName == null)
				userName = createdBy;
			usersByHoursIdMap.put(entry.getId(), userName);
		}
		results.put("usernameMap", usersByHoursIdMap);

		return results;
	}

	@RequestMapping("/timeEntry/timeReportByVolunteer")
	@PreAuthorize("hasAnyAuthority('" + Permission.TIME_READ + ", " + Permission.TIME_CREATE + "')")
	@JsonView(WorkEntryView.TimeReportByVolunteer.class)
	public @ResponseBody Collection<WorkEntry> getTimeReportByVolunteer(@RequestParam long volunteerId,
			@DateTimeFormat(pattern = DateUtil.TWO_DIGIT_DATE_ONLY) @RequestParam(required = false) LocalDate date) {
		SortedSet<WorkEntry> r = new TreeSet<>(Comparator.reverseOrder());
		List<WorkEntry> workEntries = workEntryDAO.findByCriteria(volunteerId, null, getFacilityContextId(), null, null,
				getTodayAtFacility(),
				new QueryCustomization() //
						.prefetchField(ORGANIZATION, "org") //
						.prefetchField(VOLUNTEER_ASSIGNMENT, "va") //
						.addExtraJoin("left join fetch va.benefitingService") //
						.addExtraJoin("left join fetch va.benefitingServiceRole"));
		r.addAll(workEntries);
		return r;
	}

	@RequestMapping("/timeEntry/timeReportByDate")
	@PreAuthorize("hasAnyAuthority('" + Permission.TIME_READ + ", " + Permission.TIME_CREATE + "')")
	@JsonView(WorkEntryView.TimeReportByDate.class)
	public @ResponseBody Collection<WorkEntry> getTimeReportByDateAtWorkingFacility(
			@DateTimeFormat(pattern = DateUtil.TWO_DIGIT_DATE_ONLY) @RequestParam LocalDate date) {
		List<WorkEntry> workEntries = workEntryDAO.findByCriteria(null, null, getFacilityContextId(), null, date, null,
				new QueryCustomization() //
						.prefetchField(ORGANIZATION, "org") //
						.prefetchField(VOLUNTEER_ASSIGNMENT, "va") //
						.addExtraJoin("left join fetch va.volunteer") //
						.addExtraJoin("left join fetch va.benefitingService") //
						.addExtraJoin("left join fetch va.benefitingServiceRole") //
						.setOrderBy("o.createdDate desc"));
		return workEntries;
	}

	@RequestMapping("/timeEntry/post")
	@PreAuthorize("hasAnyAuthority('" + Permission.TIME_CREATE + "')")
	public @ResponseBody boolean postTime(@RequestParam int numEntries, HttpServletRequest request)
			throws ServletRequestBindingException, ServiceValidationException {
		Set<Long> assignmentIds = new HashSet<>();
		Set<Long> organizationIds = new HashSet<>();
		for (int i = 0; i < numEntries; i++) {
			assignmentIds.add(ServletRequestUtils.getRequiredLongParameter(request, "assignmentId" + i));
			organizationIds.add(ServletRequestUtils.getRequiredLongParameter(request, "organizationId" + i));
		}
		Map<Long, VolunteerAssignment> assignmentMap = volunteerAssignmentDAO.findRequiredByPrimaryKeys(assignmentIds);
		Map<Long, AbstractBasicOrganization> organizationsMap = organizationDAO
				.findRequiredByPrimaryKeys(organizationIds);

		List<WorkEntry> worksheet = new ArrayList<>();
		for (int i = 0; i < numEntries; i++) {
			long assignmentId = ServletRequestUtils.getRequiredLongParameter(request, "assignmentId" + i);
			long organizationId = ServletRequestUtils.getRequiredLongParameter(request, "organizationId" + i);
			LocalDate date = LocalDate.parse(ServletRequestUtils.getRequiredStringParameter(request, "date" + i),
					DateUtil.TWO_DIGIT_DATE_ONLY_FORMAT);
			BigDecimal hours = new BigDecimal(ServletRequestUtils.getRequiredStringParameter(request, "hours" + i));

			VolunteerAssignment va = assignmentMap.get(assignmentId);
			AbstractBasicOrganization o = organizationsMap.get(organizationId);
			worksheet.add(new WorkEntry(va, o, date, hours.doubleValue()));
		}

		workEntryService.saveMultipleNew(worksheet, false);

		return true;
	}

	@RequestMapping("/timeEntry/delete")
	@PreAuthorize("hasAnyAuthority('" + Permission.TIME_CREATE + "')")
	public @ResponseBody boolean deleteTime(@RequestParam long workEntryId) throws ServletRequestBindingException {
		workEntryService.delete(workEntryId);
		return true;
	}

	@RequestMapping("/adjustedHours/new")
	@PreAuthorize("hasAnyAuthority('" + Permission.TIME_CREATE + "')")
	public @ResponseBody boolean adjustedHoursNew(@RequestParam long volunteerId,
			@DateTimeFormat(pattern = DateUtil.TWO_DIGIT_DATE_ONLY) @RequestParam LocalDate date,
			@RequestParam double hours, @RequestParam String comments) throws ServiceValidationException {
		AdjustedHoursEntry e = new AdjustedHoursEntry();
		e.setDate(date);
		e.setHours(hours);
		e.setDescription(comments);
		e.setFacility(SessionUtil.getFacilityContext());
		e.setVolunteer(volunteerDAO.findRequiredByPrimaryKey(volunteerId));
		e = adjustedHoursEntryService.saveOrUpdate(e);

		return true;
	}

	@RequestMapping("/timeEntry/update")
	@PreAuthorize("hasAnyAuthority('" + Permission.TIME_CREATE + "')")
	public @ResponseBody boolean updateTimeEntry(@RequestParam long id,
			@DateTimeFormat(pattern = DateUtil.TWO_DIGIT_DATE_ONLY) @RequestParam LocalDate date,
			@RequestParam double hours, @RequestParam long assignmentId, @RequestParam long organizationId)
			throws ServiceValidationException {
		WorkEntry w = workEntryDAO.findRequiredByPrimaryKey(id);
		w.setDateWorked(date);
		w.setHoursWorked(hours);

		VolunteerAssignment va = volunteerAssignmentDAO.findRequiredByPrimaryKey(assignmentId);
		w.setVolunteerAssignment(va);

		AbstractBasicOrganization o = organizationDAO.findRequiredByPrimaryKey(organizationId);
		w.setOrganization(o);
		w = workEntryService.saveOrUpdate(w, false);

		return true;
	}

}
