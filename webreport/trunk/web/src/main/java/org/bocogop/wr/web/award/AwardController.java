package org.bocogop.wr.web.award;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;

import org.bocogop.shared.model.Permission;
import org.bocogop.shared.model.Permission.PermissionType;
import org.bocogop.wr.model.award.AwardResult;
import org.bocogop.wr.model.volunteer.Volunteer;
import org.bocogop.wr.util.DateUtil;
import org.bocogop.wr.web.AbstractAppController;
import org.bocogop.wr.web.breadcrumbs.Breadcrumb;

@Controller
@SessionAttributes(value = { AwardController.COMMAND_NAME })
public class AwardController extends AbstractAppController {

	public static final String COMMAND_NAME = "awardCommand";

	@ModelAttribute(COMMAND_NAME)
	public AwardListCommand populateForm() {
		return null;
	}

	@RequestMapping(path = "/award.htm", method = RequestMethod.GET)
	@Breadcrumb("Awards")
	@PreAuthorize("hasAuthority('" + Permission.VOLUNTEER_AWARD_READ + "')")
	public String listVolunteerAwards(@ModelAttribute(COMMAND_NAME) AwardListCommand command, ModelMap model) {
		long facilityId = getFacilityContextId();

		if (command == null || command.getFacilityId() != facilityId) {
			command = new AwardListCommand(facilityId);
		}
		model.addAttribute(COMMAND_NAME, command);
		addReferenceData(model, command);
		return "awardList";
	}

	@RequestMapping(path = "/awardSearchSubmit.htm", method = RequestMethod.POST)
	@PreAuthorize("hasAuthority('" + Permission.VOLUNTEER_AWARD_READ + "')")
	public String submitAwardListSearch(@ModelAttribute(COMMAND_NAME) AwardListCommand command, ModelMap model) {
		List<AwardResult> a = runSearch(command);
		if (command.getAwardsProcessed() == 0)
			command.setEligibleAwardResults(a);
		if (command.getAwardsProcessed() == 1)
			command.setProcessedAwardResults(a);

		Map<Long, Volunteer> volunteersMap = volunteerDAO
				.findByPrimaryKeys(a.stream().map(p -> p.getVolunteerId()).collect(Collectors.toList()));
		command.setVolunteersMap(volunteersMap);

		model.addAttribute(COMMAND_NAME, command);

		addReferenceData(model, command);
		return "awardList";
	}

	private void addReferenceData(ModelMap model, AwardListCommand command) {
		model.addAttribute("todayDate", getTodayAtFacility().format(DateUtil.TWO_DIGIT_DATE_ONLY_FORMAT));
		setFormAsReadOnlyUnlessUserHasPermissions(model, PermissionType.VOLUNTEER_AWARD_CREATE);
		appendCommonReportParams(model);
	}

	public List<AwardResult> runSearch(AwardListCommand command) {
		List<AwardResult> results = null;

		if (command.getAwardsProcessed() != null && command.getAwardsProcessed() == 1) {
			results = awardDAO.findProcessedAwards(getFacilityContextId(), command.isIncludeAdult(),
					command.isIncludeYouth(), command.isIncludeOther(), command.isIncludeActive(),
					command.isIncludeSeparated(), command.getStartDate(), command.getEndDate());
			command.setProcessedSearched(true);
		} else {
			results = awardDAO.findPotentialAwards(getFacilityContextId(), command.isIncludeAdult(),
					command.isIncludeYouth(), command.isIncludeActive(), command.isIncludeSeparated());
			command.setEligibleSearched(true);
		}

		return results;
	}

	@RequestMapping(path = "/awardPostSubmit.htm", method = RequestMethod.POST)
	@PreAuthorize("hasAuthority('" + Permission.VOLUNTEER_AWARD_CREATE + "')")
	public String postAwards(@ModelAttribute(COMMAND_NAME) AwardListCommand command,
			@RequestParam("awardVolunteerIds") long[] volunteerIds,
			@DateTimeFormat(pattern = DateUtil.TWO_DIGIT_DATE_ONLY) @RequestParam LocalDate awardDate,
			HttpServletRequest request) throws ServletRequestBindingException {
		Map<Long, Long> volIdToAwardMap = new HashMap<>();

		for (long volunteerId : volunteerIds) {
			long awardId = ServletRequestUtils.getRequiredLongParameter(request, "awardForVolId" + volunteerId);
			volIdToAwardMap.put(volunteerId, awardId);
		}
		try {
			awardService.saveMultipleVolunteers(volIdToAwardMap, awardDate);
			if (command != null) {
				if (1 == command.getAwardsProcessed()) {
					// force re-search of awards processed to include the ones
					// we just added
					command.setFacilityId(-1);
				} else {
					// remove just the awards from our cached list that we just
					// added
					for (Iterator<AwardResult> it = command.getEligibleAwardResults().iterator(); it.hasNext();) {
						AwardResult ar = it.next();
						if (volIdToAwardMap.containsKey(ar.getVolunteerId()))
							it.remove();
					}
				}
			}
		} catch (Exception e) {
			// force complete refresh of awards next time thru
			if (command != null)
				command.setFacilityId(-1);
			throw e;
		}

		return "redirect:/award.htm";
	}

}
