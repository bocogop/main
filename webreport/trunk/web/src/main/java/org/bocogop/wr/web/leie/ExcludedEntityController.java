package org.bocogop.wr.web.leie;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttributes;

import com.fasterxml.jackson.annotation.JsonView;

import org.bocogop.wr.model.ApplicationParameter;
import org.bocogop.wr.model.ApplicationParameter.ApplicationParameterType;
import org.bocogop.wr.model.leie.ExcludedEntity;
import org.bocogop.wr.model.leie.ExclusionType.ExclusionTypeView;
import org.bocogop.wr.persistence.dao.leie.ExcludedEntityMatch;
import org.bocogop.wr.web.AbstractAppController;
import org.bocogop.wr.web.breadcrumbs.Breadcrumb;

@Controller
@SessionAttributes(value = { AbstractAppController.DEFAULT_COMMAND_NAME })
public class ExcludedEntityController extends AbstractAppController {

	@RequestMapping("/excludedEntityList.htm")
	@Breadcrumb("List Excluded Entity Matches")
	// @PreAuthorize("hasAnyAuthority('" + Permission.ORG_CODE_NATIONAL_READ +
	// ", " + Permission.ORG_CODE_LOCAL_READ
	// + "')")
	public String listExcludedEntityMatches(ModelMap model, HttpServletRequest request) {
		List<Long> facilityIds = null;
		// TODO replace with permissions once Cindi updates CRUD - CPB
		if (!getCurrentUser().isNationalAdmin())
			facilityIds = Arrays.asList(getFacilityContextId());
		List<ExcludedEntityMatch> excludedEntities = excludedEntityDAO.findExcludedEntitiesForFacilities(facilityIds);

		model.addAttribute("excludedEntities", excludedEntities);

		ApplicationParameter ap = applicationParameterDAO
				.findByName(ApplicationParameterType.LEIE_SOURCE_DATA_CHANGED_DATE.getParamName());
		try {
			model.addAttribute("lastUpdatedDate", StringUtils.isNotBlank(ap.getParameterValue())
					? ZonedDateTime.parse(ap.getParameterValue()).toLocalDate() : null);
		} catch (DateTimeParseException ignored) {
		}

		ap = applicationParameterDAO.findByName(ApplicationParameterType.LEIE_JOB_LAST_EXECUTED_DATE.getParamName());
		try {
			model.addAttribute("lastExecutedDate", StringUtils.isNotBlank(ap.getParameterValue())
					? ZonedDateTime.parse(ap.getParameterValue()).toLocalDate() : null);
		} catch (DateTimeParseException ignored) {
		}

		return "excludedEntityList";
	}

	@RequestMapping("/excludedEntityViewAll.htm")
	@Breadcrumb("List All Excluded Entities")
	// @PreAuthorize("hasAnyAuthority('" + Permission.ORG_CODE_NATIONAL_READ +
	// ", " + Permission.ORG_CODE_LOCAL_READ
	// + "')")
	public String listAllExcludedEntities(ModelMap model, HttpServletRequest request) {
		ApplicationParameter ap = applicationParameterDAO
				.findByName(ApplicationParameterType.LEIE_SOURCE_DATA_CHANGED_DATE.getParamName());
		try {
			model.addAttribute("lastUpdatedDate", StringUtils.isNotBlank(ap.getParameterValue())
					? ZonedDateTime.parse(ap.getParameterValue()).toLocalDate() : null);
		} catch (DateTimeParseException ignored) {
		}

		return "excludedEntityListAll";
	}

	@RequestMapping("/excludedEntities/search")
	@JsonView(ExclusionTypeView.Extended.class)
	public @ResponseBody Map<String, Object> excludedEntitySearch(@RequestParam int draw, @RequestParam int start,
			@RequestParam int length, @RequestParam(name = "search[value]") String searchValue,
			@RequestParam(name = "search[regex]") boolean searchIsRegex,
			@RequestParam(name = "order[0][column]") int sortColIndex,
			@RequestParam(name = "order[0][dir]") String sortDir) {
		Map<String, Object> resultMap = new HashMap<>();

		String dir = ("asc".equals(sortDir) ? "" : " desc");
		String[] cols = {
				"o.lastName" + dir + ", o.firstName" + dir + ", o.middleName" + dir + ", o.businessName" + dir, //
				"et.ssa" + dir, //
				"et.code42Usc" + dir, //
				"o.exclusionDate" + dir, //
				"et.description" + dir, //
				"o.state" + dir + ", o.city" + dir + ", o.address" + dir, //
				"o.dob" + dir };

		List<ExcludedEntity> results = excludedEntityDAO.findByCriteria(searchValue, start, length, cols[sortColIndex]);
		resultMap.put("data", results);
		resultMap.put("draw", draw);

		int[] totalAndFiltered = excludedEntityDAO.getTotalAndFilteredNumber(searchValue);
		resultMap.put("recordsTotal", totalAndFiltered[0]);
		resultMap.put("recordsFiltered", totalAndFiltered[1]);

		return resultMap;
	}

	@RequestMapping("/runLEIE")
	public @ResponseBody boolean runLEIE() throws IOException {
		excludedEntityService.refreshDataAndUpdateVolunteers();
		return true;
	}
	
	@RequestMapping("/updateLEIE")
	public @ResponseBody boolean leieUpdateVolunteers() throws IOException {
		excludedEntityService.updateVolunteers();
		return true;
	}

}
