package org.bocogop.wr.web.staffTitle;

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
import org.bocogop.shared.model.Permission.PermissionType;
import org.bocogop.shared.service.validation.ServiceValidationException;
import org.bocogop.wr.model.facility.StaffTitle;
import org.bocogop.wr.model.facility.StaffTitle.StaffTitleView;
import org.bocogop.wr.web.AbstractAppController;
import org.bocogop.wr.web.breadcrumbs.Breadcrumb;

@Controller
public class StaffTitleController extends AbstractAppController {

	@RequestMapping("/manageStaffTitle.htm")
	@Breadcrumb("Manage StaffTitle")
	@PreAuthorize("hasAuthority('" + Permission.STAFF_TITLE_CREATE + "')") 
	public String listStaffTitles(ModelMap model) {
		createReferenceData(model);
		setFormAsReadOnlyUnlessUserHasPermissions(model, PermissionType.STAFF_TITLE_CREATE);		
		return "manageStaffTitle";
	}

	@RequestMapping("/staffTitles")
	@JsonView(StaffTitleView.Basic.class)
	public @ResponseBody SortedSet<StaffTitle> getStaffTitles() {
		SortedSet<StaffTitle> results = staffTitleDAO.findAllSorted();
		return results;
	}
	@RequestMapping("/staffTitle/delete")
	public @ResponseBody boolean deleteStaffTitle(@RequestParam long staffTitleId) throws ServiceValidationException {
		staffTitleService.delete(staffTitleId);
		return true;
	}
	
	@RequestMapping(value = "/staffTitle/saveOrUpdate", method = RequestMethod.POST)
	// @PreAuthorize("hasAuthority('" + Permission.VOLUNTEER_VIEW + "')")
	public @ResponseBody boolean staffTitleCreateOrUpdate(@RequestParam(required = false) Long staffTitleId, 
			@RequestParam String name, 			
			@RequestParam(required = false) String description,
			@RequestParam boolean isChief,
			@RequestParam boolean isChiefSupervisor,
			@RequestParam boolean isActive) throws ServiceValidationException {
		
		StaffTitle st;
		if (staffTitleId != null) {
			st = staffTitleDAO.findRequiredByPrimaryKey(staffTitleId);
		} else {
			st= new StaffTitle();			
		}

		st.setName(name);
		st.setDescription(description);
		st.setChief(isChief);
		st.setChiefSupervisor(isChiefSupervisor);
		st.setInactive(!isActive);

		st = staffTitleService.saveOrUpdate(st);
		return true;
	}


	private void createReferenceData(ModelMap model) {
		//model.put("allRequirementDateTypes", requirementDateTypeDAO.findAllSorted());
	}

}
