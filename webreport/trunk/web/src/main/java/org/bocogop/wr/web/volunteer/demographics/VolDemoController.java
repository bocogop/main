package org.bocogop.wr.web.volunteer.demographics;

import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.SortedSet;
import java.util.stream.Collectors;

import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.annotation.JsonView;

import org.bocogop.shared.model.AppUser;
import org.bocogop.shared.util.WebUtil;
import org.bocogop.wr.model.volunteer.Volunteer.VolunteerView;
import org.bocogop.wr.model.facility.Facility;
import org.bocogop.wr.model.facility.Facility.FacilityView;
import org.bocogop.wr.model.volunteer.VolunteerDemographics;
import org.bocogop.wr.persistence.dao.volunteer.demographics.VolDemoColumn;
import org.bocogop.wr.persistence.dao.volunteer.demographics.VolDemoSearchParams;
import org.bocogop.wr.util.OffsetCollection;
import org.bocogop.wr.web.AbstractAppController;
import org.bocogop.wr.web.breadcrumbs.Breadcrumb;

@Controller
public class VolDemoController extends AbstractAppController {

	private static final String SESSION_ATTR_DEMOGRAPHICS_SEARCH_PARAMS = "volunteerDemographicsSearchCache";
	private static final String SESSION_ATTR_DEMOGRAPHICS_RESULTS = "volunteerDemographicsResults";

	@RequestMapping("/volunteerDemographics.htm")
	@Breadcrumb("Volunteer Demographics")
	// @PreAuthorize("hasAuthority('" + Permission.VOLUNTEER_CREATE + "')")
	public String volunteerDemographics(ModelMap model, HttpSession session) {
		session.removeAttribute(SESSION_ATTR_DEMOGRAPHICS_RESULTS);
		session.removeAttribute(SESSION_ATTR_DEMOGRAPHICS_SEARCH_PARAMS);

		Map<String, Integer> allMonths = new LinkedHashMap<>();
		for (int i = 1; i <= 12; i++)
			allMonths.put(Month.of(i).getDisplayName(TextStyle.FULL, Locale.getDefault()), i);
		model.put("allMonths", allMonths);
		model.put("curMonth", LocalDate.now().getMonthValue());
		model.put("curYear", LocalDate.now().getYear());
		model.put("allGenders", genderDAO.findAllSorted());
		model.put("allStates", stateDAO.findAllSorted());
		appendCommonReportParams(model);

		WebUtil.addEnumToModel(VolDemoColumn.class, model);
		model.addAttribute("columnsByDivider", VolDemoColumn.getColumnsByDivider());
		for (VolDemoColumn c : VolDemoColumn.values()) {
			model.addAttribute("COL_INDEX_" + c.name(), c.ordinal());
		}

		return "volunteerDemographics";
	}

	@RequestMapping("/volunteer/demographics")
	@JsonView(VolunteerView.Demographics.class)
	public @ResponseBody Map<String, Object> volunteerDemographicsSearch(HttpSession session,
			@RequestParam MultiValueMap<String, String> allParams, @RequestParam int draw, @RequestParam int start,
			@RequestParam int length, @RequestParam(name = "search[value]") String searchValue,
			@RequestParam(name = "search[regex]") boolean searchIsRegex,
			@RequestParam(name = "order[0][column]") int sortColIndex,
			@RequestParam(name = "order[0][dir]") String sortDir,
			// params
			@RequestParam boolean isNational, //
			@RequestParam(name = "displayColumnIndexes[]") int[] displayColumnIndexes) {
		Map<String, Object> resultMap = new HashMap<>();

		EnumSet<VolDemoColumn> displayCols = VolDemoColumn.getWithIndexes(displayColumnIndexes);

		Map<VolDemoColumn, String> filters = new HashMap<>();
		VolDemoColumn[] volDemoCols = VolDemoColumn.values();

		for (int i = 0; allParams.containsKey("columns[" + i + "][search][value]"); i++) {
			List<String> l = allParams.get("columns[" + i + "][search][value]");
			if (l.isEmpty() || StringUtils.isBlank(l.get(0)))
				continue;
			/*
			 * subtract 1 to skip the checkbox column and match VolDemoColumn
			 * ordinal values - CPB
			 */
			filters.put(volDemoCols[i - 1], l.get(0));
		}

		Long facilityId = isNational && getCurrentUser().isNationalAdmin() ? null : getFacilityContextId();
		boolean sortAscending = "asc".equals(sortDir);

		Map<String, String> restrictions = (Map<String, String>) allParams.entrySet().stream()
				.filter(p -> p.getKey().startsWith("rx")).collect(Collectors
						.toMap(p -> StringUtils.uncapitalize(p.getKey().substring(2)), p -> p.getValue().get(0)));

		VolDemoSearchParams newSearchParams = new VolDemoSearchParams(getFacilityContextId(), facilityId, filters,
				searchValue, sortColIndex, sortAscending, restrictions, displayCols);

		VolDemoSearchParams lastSearchParams = (VolDemoSearchParams) session
				.getAttribute(SESSION_ATTR_DEMOGRAPHICS_SEARCH_PARAMS);
		if (lastSearchParams == null)
			lastSearchParams = new VolDemoSearchParams();

		@SuppressWarnings("unchecked")
		OffsetCollection<VolunteerDemographics> lastResults = (OffsetCollection<VolunteerDemographics>) session
				.getAttribute(SESSION_ATTR_DEMOGRAPHICS_RESULTS);
		List<VolunteerDemographics> results = null;
		boolean cacheHit = false;

		if (lastResults != null && lastSearchParams.matchesPagingCriteria(newSearchParams)) {
			results = lastResults.getPage(start, length);
			cacheHit = (results != null);
		}

		if (!cacheHit) {
			int maxTotalEntries = 1000;

			int newFrom = Math.max(start - maxTotalEntries / 2, 0);
			int newLength = Math.max(maxTotalEntries / 2 + length, maxTotalEntries);
			results = volunteerDemographicsDAO.findDemographics(newSearchParams, newFrom, newLength);
			boolean lastPage = results.size() < newLength;
			OffsetCollection<VolunteerDemographics> c = new OffsetCollection<>(results, newFrom, lastPage);
			session.setAttribute(SESSION_ATTR_DEMOGRAPHICS_RESULTS, c);
			results = c.getPage(start, length);
		}

		if (results == null)
			results = new ArrayList<>();

		resultMap.put("data", results);
		resultMap.put("draw", draw);

		/*
		 * If we didn't change our filter criteria, cache the counts for
		 * efficiency - CPB
		 */
		int[] totalAndFiltered = null;
		if (lastSearchParams.mostRecentCounts != null && lastSearchParams.matchesCountsCriteria(newSearchParams)) {
			totalAndFiltered = lastSearchParams.mostRecentCounts;
			newSearchParams.mostRecentCounts = lastSearchParams.mostRecentCounts;
		} else {
			totalAndFiltered = volunteerDemographicsDAO.findDemographicsTotalAndFilteredNumber(newSearchParams);
			newSearchParams.mostRecentCounts = totalAndFiltered;
		}
		session.setAttribute(SESSION_ATTR_DEMOGRAPHICS_SEARCH_PARAMS, newSearchParams);

		resultMap.put("recordsTotal", totalAndFiltered[0]);
		resultMap.put("recordsFiltered", totalAndFiltered[1]);
		return resultMap;
	}

	@RequestMapping("/volunteer/emailRecipientList")
	public @ResponseBody boolean emailRecipientList(@RequestParam(name = "emails[]") String[] emails) {
		AppUser appUser = getCurrentUser();
		String userEmail = appUser.getEmail();
		if (StringUtils.isBlank(userEmail))
			throw new RuntimeException("Sorry, your user does not have an email stored.");
		StringBuilder emailBody = new StringBuilder();
		for (String e : emails)
			emailBody.append(e).append("\n");

		emailService.sendEmail("Volunteer Demographics email recipient list", emailBody.toString(),
				new String[] { userEmail }, null);
		return true;
	}

}
