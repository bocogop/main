package org.bocogop.wr.web.precinct;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.bocogop.shared.model.precinct.Precinct;
import org.bocogop.shared.service.validation.ServiceValidationException;
import org.bocogop.shared.web.AbstractAppController;
import org.bocogop.shared.web.validation.ValidationException;
import org.bocogop.wr.web.breadcrumbs.Breadcrumb;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;

@Controller
@SessionAttributes(value = { AbstractAppController.DEFAULT_COMMAND_NAME })
public class PrecinctController extends AbstractAppController {

	@Autowired
	private PrecinctValidator precinctValidator;

	@RequestMapping("/precinctCreate.htm")
	@Breadcrumb("Create Precinct")
	// @PreAuthorize("hasAnyAuthority('" + Permission.PRECINCT_CREATE + "')")
	public String createPrecinct(ModelMap model, HttpServletRequest request) {
		Precinct precinct = new Precinct();
		PrecinctCommand command = new PrecinctCommand(precinct);
		model.addAttribute(DEFAULT_COMMAND_NAME, command);
		createReferenceData(model);
		return "precinctCreate";
	}

	@RequestMapping("/precinctEdit.htm")
	@Breadcrumb("Edit Precincts")
	// @PreAuthorize("hasAnyAuthority('" + Permission.PRECINCT_EDIT_ALL + ", " +
	// Permission.PRECINCT_EDIT_CURRENT + "')")
	public String editPrecinct(@RequestParam(required = false) Long id, ModelMap model, HttpServletRequest request) {
		PrecinctCommand command = new PrecinctCommand();
		// if
		// (SecurityUtil.hasAllPermissionsAtCurrentPrecinct(PermissionType.PRECINCT_EDIT_ALL))
		// {
		// if (id != null) {
		// Precinct precinct = precinctDAO.findRequiredByPrimaryKey(id);
		// command = new PrecinctCommand(precinct);
		// }
		// } else if
		// (SecurityUtil.hasAllPermissionsAtCurrentPrecinct(PermissionType.PRECINCT_EDIT_CURRENT))
		// {
		// command = new PrecinctCommand(getRequiredPrecinctContext());
		// } else {
		// throw new SecurityException();
		// }

		model.addAttribute(DEFAULT_COMMAND_NAME, command);
		createReferenceData(model);
		return "precinctEdit";
	}

	private void createReferenceData(ModelMap model) {
		model.put("allPrecincts", precinctDAO.findAllSorted());

		Set<String> prioritizedIds = new HashSet<>(
				Arrays.asList("US/Eastern", "US/Central", "US/Mountain", "US/Pacific"));

		final Instant now = Instant.now();
		SortedMap<ZoneId, ZoneOffset> list = new TreeMap<>(new Comparator<ZoneId>() {
			@Override
			public int compare(ZoneId o1, ZoneId o2) {
				if (o1.equals(o2))
					return 0;
				return new CompareToBuilder()
						.append(prioritizedIds.contains(o1.getId()) ? 0 : 1,
								prioritizedIds.contains(o2.getId()) ? 0 : 1)
						.append(o1.getRules().getOffset(now), o2.getRules().getOffset(now))
						.append(o1.getId(), o2.getId()).toComparison() > 0 ? 1 : -1;
			}
		});

		for (String s : ZoneId.getAvailableZoneIds()) {
			ZoneId z = ZoneId.of(s);
			ZoneOffset o = z.getRules().getOffset(now);
			list.put(z, o);
		}

		model.put("allTimeZones", list);
		model.put("prioritizedTimeZoneIds", prioritizedIds);
	}

	@RequestMapping("/precinctSubmit.htm")
	// @PreAuthorize("hasAnyAuthority('" + Permission.PRECINCT_CREATE + ", " +
	// Permission.PRECINCT_EDIT_CURRENT + ", "
	// + Permission.PRECINCT_EDIT_ALL + "')")
	public String submitPrecinct(@ModelAttribute(DEFAULT_COMMAND_NAME) PrecinctCommand command, BindingResult result,
			SessionStatus status, ModelMap model, HttpServletRequest request) throws ValidationException {
		Precinct precinct = command.getPrecinct();
		boolean isEdit = precinct.isPersistent();

		/* Validation step (JSR303, other custom logic in the validator) */
		precinctValidator.validate(command, result, false, "precinct");

		boolean hasErrors = result.hasErrors();

		if (!hasErrors) {
			try {
				precinct = precinctService.saveOrUpdate(precinct);
				userNotifier.notifyUserOnceWithMessage(request,
						getMessage(isEdit ? "precinct.update.success" : "precinct.create.success"));
			} catch (ServiceValidationException e) {
				webValidationService.handle(e, result);
				hasErrors = true;
			}
		}

		if (hasErrors) {
			createReferenceData(model);
			return isEdit ? "precinctEdit" : "precinctCreate";
		} else {
			status.setComplete();
			return "redirect:/precinctEdit.htm?id=" + precinct.getId();
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@RequestMapping("/findPrecinctsForHierarchyDisplay")
	// @PreAuthorize("hasAuthority('" + Permission.VOTER_VIEW + "')")
	public @ResponseBody SortedSet<Precinct> findPrecinctsForHierarchyDisplay() {
		SortedSet precincts = new TreeSet<>(); // administrativeUnitDAO.findAllSorted();
		// TODO BOCOGOP

		//
		// SortedSet<? extends PrecinctNode<?>> rootPrecincts =
		// precinctDAO.findRootPrecincts();
		// for (Object f : precincts)
		// rootPrecincts.removeAll(((AdministrativeUnit)
		// f).getPrecinctChildren());
		//
		// precincts.add(new PrecinctNode() {
		// @Override
		// public SortedSet getPrecinctChildren() {
		// return rootPrecincts;
		// }
		//
		// @Override
		// public Long getId() {
		// return -1L;
		// }
		//
		// @Override
		// public String getDisplayName() {
		// return "(No VISN Assigned)";
		// }
		//
		// @Override
		// public int compareTo(Object o) {
		// if (equals(o))
		// return 0;
		// return 1;
		// }
		//
		// @Override
		// public boolean isActive() {
		// return true;
		// }
		// });

		return precincts;
	}

}