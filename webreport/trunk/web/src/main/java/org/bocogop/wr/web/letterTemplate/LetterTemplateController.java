package org.bocogop.wr.web.letterTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;

import org.bocogop.shared.util.WebUtil;
import org.bocogop.wr.model.facility.Facility;
import org.bocogop.wr.model.facility.Facility.FacilityValue;
import org.bocogop.wr.model.letterTemplate.LetterTemplate;
import org.bocogop.wr.model.letterTemplate.LetterType;
import org.bocogop.wr.web.AbstractAppController;
import org.bocogop.wr.web.breadcrumbs.Breadcrumb;

@Controller
@SessionAttributes(value = { AbstractAppController.DEFAULT_COMMAND_NAME })
public class LetterTemplateController extends AbstractAppController {

	@RequestMapping("/letterTemplateEdit.htm")
	@Breadcrumb("Edit Letter Templates")
	// @PreAuthorize("hasAuthority('" + Permission.DONATION_CREATE + "')")
	public String get(ModelMap model, @RequestParam(required = false) LetterType letterType) {
		Map<LetterType, LetterTemplate> defaultLetterMap = letterTemplateDAO
				.findByFacilityId(FacilityValue.CENTRAL_OFFICE.getId());
		Map<String, LetterTemplate> stringDefaultLetterMap = translateToStringKey(defaultLetterMap);
		model.addAttribute("defaultLetterMap", stringDefaultLetterMap);

		Map<LetterType, LetterTemplate> letterMap = letterTemplateDAO.findByFacilityId(getFacilityContextId());
		Map<String, LetterTemplate> stringLetterMap = translateToStringKey(letterMap);
		model.addAttribute("letterMap", stringLetterMap);

		model.addAttribute("selectedLetterType", letterType);
		createReferenceData(model);
		return "letterTemplateEdit";
	}

	private Map<String, LetterTemplate> translateToStringKey(Map<LetterType, LetterTemplate> map) {
		Map<String, LetterTemplate> results = new HashMap<>();
		for (Entry<LetterType, LetterTemplate> entry : map.entrySet()) {
			LetterType key = entry.getKey();
			results.put(key.getCode(), entry.getValue());
		}
		return results;

		// Apparently Fortify can't handle this. Piece of crap. CPB
		// return map.entrySet().stream()
		// .collect(Collectors.toMap(p -> p.getKey().getCode(),
		// Map.Entry::getValue, (a, b) -> a, HashMap::new));
	}

	private void createReferenceData(ModelMap model) {
		WebUtil.addEnumToModel(LetterType.class, model);
	}

	@RequestMapping(value = "/letterTemplateSubmit.htm", method = RequestMethod.POST)
	// @PreAuthorize("hasAuthority('" + Permission.DONATION_CREATE + "')")
	public String post(@RequestParam LetterType type, @RequestParam Map<String, String> allParams,
			HttpServletRequest request) {
		Facility facility = getRequiredFacilityContext();
		long facilityId = facility.getId();
		Map<LetterType, LetterTemplate> letterMap = letterTemplateDAO.findByFacilityId(facilityId);

		boolean override = Boolean.valueOf(allParams.get("override_" + type.getCode()));
		boolean showHeader = Boolean.valueOf(allParams.get("showHeader_" + type.getCode()));
		boolean showFooter = Boolean.valueOf(allParams.get("showFooter_" + type.getCode()));
		String body = allParams.get("body_" + type.getCode());

		LetterTemplate template = letterMap.get(type);

		if (override || facility.isCentralOffice()) {
			if (template == null) {
				template = new LetterTemplate(facility, type);
			}
			template.setBody(body);
			template.setShowHeader(showHeader);
			template.setShowFooter(showFooter);
			template = letterTemplateService.saveOrUpdate(template);
		} else {
			if (template != null) {
				letterTemplateService.delete(template.getId());
			}
		}

		userNotifier.notifyUserOnceWithMessage(request, getMessage("letterTemplate.update.success"));

		return "redirect:/letterTemplateEdit.htm?letterType=" + type.getCode();
	}

}
