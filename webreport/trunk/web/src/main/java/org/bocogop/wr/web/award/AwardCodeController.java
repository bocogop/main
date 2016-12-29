package org.bocogop.wr.web.award;

import java.util.SortedSet;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.annotation.JsonView;

import org.bocogop.shared.model.Permission;
import org.bocogop.shared.service.validation.ServiceValidationException;
import org.bocogop.wr.model.award.Award;
import org.bocogop.wr.model.award.AwardType;
import org.bocogop.wr.model.award.Award.AwardCodeView;
import org.bocogop.wr.web.AbstractAppController;
import org.bocogop.wr.web.breadcrumbs.Breadcrumb;

@Controller
public class AwardCodeController extends AbstractAppController {

	@RequestMapping("/manageAwardCodes.htm")
	@Breadcrumb("Manage Award Code")
	@PreAuthorize("hasAuthority('" + Permission.AWARD_CODE_CREATE + "')") 
	public String listAwardCodes(ModelMap model) {
		createReferenceData(model);
		//setFormAsReadOnlyUnlessUserHasPermissions(model, PermissionType.STAFF_TITLE_CREATE);		
		return "manageAwardCodes";
	}

	@RequestMapping("/awardCodes")
	@JsonView(AwardCodeView.Basic.class)
	public @ResponseBody SortedSet<Award> getAwardCodes() {
		return awardCodeDAO.findAllSorted();
	}
	@RequestMapping("/awardCode/delete")
	public @ResponseBody boolean deleteAwardCode(@RequestParam long awardCodeId) throws ServiceValidationException {
		awardCodeService.delete(awardCodeId);
		return true;
	}
	
	@RequestMapping(value = "/awardCode/saveOrUpdate", method = RequestMethod.POST)
	// @PreAuthorize("hasAuthority('" + Permission.VOLUNTEER_VIEW + "')")
	public @ResponseBody boolean awardCodeCreateOrUpdate(@RequestParam(required = false) Long awardCodeId, 
			@RequestParam String code, 			
			@RequestParam String name, 			
			@RequestParam Integer requiredHours, 	
			@RequestParam Integer awardHours, 
			@RequestParam AwardType type,
			@RequestParam boolean isActive) throws ServiceValidationException {
			
		Award award;
		
		award = new Award();			
		
		award.setCode(code.trim());
		award.setName(name.trim());
		award.setHoursRequired(requiredHours);
		award.setType(type);
		award.setAwardHours(awardHours);
		award.setInactive(!isActive);

		award = awardCodeService.saveOrUpdate(awardCodeId, award);
		return true;
	}

	private void createReferenceData(ModelMap model) {
		model.put("allAwardCodeTypes", AwardType.values());
	}

}
