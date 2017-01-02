package org.bocogop.wr.web.voter.demographics;

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
import java.util.stream.Collectors;

import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
import org.bocogop.shared.model.voter.Voter.VoterView;
import org.bocogop.shared.persistence.dao.voter.demographics.VolDemoColumn;
import org.bocogop.shared.persistence.dao.voter.demographics.VolDemoSearchParams;
import org.bocogop.shared.util.OffsetCollection;
import org.bocogop.shared.util.WebUtil;
import org.bocogop.shared.web.AbstractAppController;
import org.bocogop.shared.model.AppUser;
import org.bocogop.shared.model.voter.VoterDemographics;
import org.bocogop.wr.web.breadcrumbs.Breadcrumb;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.annotation.JsonView;

@Controller
public class VolDemoController extends AbstractAppController {

	private static final String SESSION_ATTR_DEMOGRAPHICS_SEARCH_PARAMS = "voterDemographicsSearchCache";
	private static final String SESSION_ATTR_DEMOGRAPHICS_RESULTS = "voterDemographicsResults";

	@RequestMapping("/voterDemographics.htm")
	@Breadcrumb("Voter Demographics")
	// @PreAuthorize("hasAuthority('" + Permission.VOTER_EDIT + "')")
	public String voterDemographics(ModelMap model, HttpSession session) {
		session.removeAttribute(SESSION_ATTR_DEMOGRAPHICS_RESULTS);
		session.removeAttribute(SESSION_ATTR_DEMOGRAPHICS_SEARCH_PARAMS);

		Map<String, Integer> allMonths = new LinkedHashMap<>();
		for (int i = 1; i <= 12; i++)
			allMonths.put(Month.of(i).getDisplayName(TextStyle.FULL, Locale.getDefault()), i);
		model.put("allMonths", allMonths);
		model.put("curMonth", LocalDate.now().getMonthValue());
		model.put("curYear", LocalDate.now().getYear());
		model.put("allGenders", genderDAO.findAllSorted());

		WebUtil.addEnumToModel(VolDemoColumn.class, model);
		model.addAttribute("columnsByDivider", VolDemoColumn.getColumnsByDivider());
		for (VolDemoColumn c : VolDemoColumn.values()) {
			model.addAttribute("COL_INDEX_" + c.name(), c.ordinal());
		}

		return "voterDemographics";
	}

	@RequestMapping("/voter/demographics")
	@JsonView(VoterView.Demographics.class)
	public @ResponseBody Map<String, Object> voterDemographicsSearch(HttpSession session,
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

		boolean sortAscending = "asc".equals(sortDir);

		Map<String, String> restrictions = (Map<String, String>) allParams.entrySet().stream()
				.filter(p -> p.getKey().startsWith("rx")).collect(Collectors
						.toMap(p -> StringUtils.uncapitalize(p.getKey().substring(2)), p -> p.getValue().get(0)));

		VolDemoSearchParams newSearchParams = new VolDemoSearchParams(null /* TODO BOCOGOP */, filters, searchValue, sortColIndex,
				sortAscending, restrictions, displayCols);

		VolDemoSearchParams lastSearchParams = (VolDemoSearchParams) session
				.getAttribute(SESSION_ATTR_DEMOGRAPHICS_SEARCH_PARAMS);
		if (lastSearchParams == null)
			lastSearchParams = new VolDemoSearchParams();

		@SuppressWarnings("unchecked")
		OffsetCollection<VoterDemographics> lastResults = (OffsetCollection<VoterDemographics>) session
				.getAttribute(SESSION_ATTR_DEMOGRAPHICS_RESULTS);
		List<VoterDemographics> results = null;
		boolean cacheHit = false;

		if (lastResults != null && lastSearchParams.matchesPagingCriteria(newSearchParams)) {
			results = lastResults.getPage(start, length);
			cacheHit = (results != null);
		}

		if (!cacheHit) {
			int maxTotalEntries = 1000;

			int newFrom = Math.max(start - maxTotalEntries / 2, 0);
			int newLength = Math.max(maxTotalEntries / 2 + length, maxTotalEntries);
			results = voterDemographicsDAO.findDemographics(newSearchParams, newFrom, newLength);
			boolean lastPage = results.size() < newLength;
			OffsetCollection<VoterDemographics> c = new OffsetCollection<>(results, newFrom, lastPage);
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
			totalAndFiltered = voterDemographicsDAO.findDemographicsTotalAndFilteredNumber(newSearchParams);
			newSearchParams.mostRecentCounts = totalAndFiltered;
		}
		session.setAttribute(SESSION_ATTR_DEMOGRAPHICS_SEARCH_PARAMS, newSearchParams);

		resultMap.put("recordsTotal", totalAndFiltered[0]);
		resultMap.put("recordsFiltered", totalAndFiltered[1]);
		return resultMap;
	}

	@RequestMapping("/voter/emailRecipientList")
	public @ResponseBody boolean emailRecipientList(@RequestParam(name = "emails[]") String[] emails) {
		AppUser appUser = getCurrentUser();
		String userEmail = appUser.getEmail();
		if (StringUtils.isBlank(userEmail))
			throw new RuntimeException("Sorry, your user does not have an email stored.");
		StringBuilder emailBody = new StringBuilder();
		for (String e : emails)
			emailBody.append(e).append("\n");

		emailService.sendEmail("Voter Demographics email recipient list", emailBody.toString(),
				new String[] { userEmail }, null);
		return true;
	}

}
