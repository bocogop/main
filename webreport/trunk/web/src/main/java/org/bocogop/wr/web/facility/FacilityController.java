package org.bocogop.wr.web.facility;

import static org.bocogop.wr.model.facility.FacilityType.FacilityTypeValue.GAMES;
import static org.bocogop.wr.model.facility.FacilityType.FacilityTypeValue.TIMEKEEPING;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.CompareToBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;

import com.fasterxml.jackson.annotation.JsonView;

import org.bocogop.shared.model.Permission;
import org.bocogop.shared.model.Permission.PermissionType;
import org.bocogop.shared.model.lookup.sds.State;
import org.bocogop.shared.persistence.lookup.sds.VAFacilityDAO.QuickSearchResult;
import org.bocogop.shared.service.validation.ServiceValidationException;
import org.bocogop.shared.util.PersistenceUtil;
import org.bocogop.shared.util.SecurityUtil;
import org.bocogop.shared.util.WebUtil;
import org.bocogop.wr.model.donation.DonGenPostFund;
import org.bocogop.wr.model.donation.DonationReference;
import org.bocogop.wr.model.facility.AdministrativeUnit;
import org.bocogop.wr.model.facility.AdministrativeUnit.AdministrativeUnitView;
import org.bocogop.wr.model.facility.FacilityType.FacilityTypeValue;
import org.bocogop.wr.model.facility.Facility;
import org.bocogop.wr.model.facility.FacilityNode;
import org.bocogop.wr.model.facility.Kiosk;
import org.bocogop.wr.model.facility.Location;
import org.bocogop.wr.model.lookup.Language;
import org.bocogop.wr.model.lookup.Language.LanguageType;
import org.bocogop.wr.web.AbstractAppController;
import org.bocogop.wr.web.breadcrumbs.Breadcrumb;
import org.bocogop.wr.web.validation.ValidationException;

@Controller
@SessionAttributes(value = { AbstractAppController.DEFAULT_COMMAND_NAME })
public class FacilityController extends AbstractAppController {

	@Autowired
	private FacilityValidator facilityValidator;
	@Value("${maxQuietPrinterStatusCheckMinutes}")
	private int maxQuietPrinterStatusCheckMinutes;

	@RequestMapping("/facilityCreate.htm")
	@Breadcrumb("Create Facility")
	@PreAuthorize("hasAnyAuthority('" + Permission.FACILITY_CREATE + "')")
	public String createFacility(ModelMap model, HttpServletRequest request) {
		Facility facility = new Facility();
		FacilityCommand command = new FacilityCommand(facility);
		model.addAttribute(DEFAULT_COMMAND_NAME, command);
		createReferenceData(model);
		return "facilityCreate";
	}

	@RequestMapping("/facilityEdit.htm")
	@Breadcrumb("Edit Facilities")
	@PreAuthorize("hasAnyAuthority('" + Permission.FACILITY_EDIT_ALL + ", " + Permission.FACILITY_EDIT_CURRENT + "')")
	public String editFacility(@RequestParam(required = false) Long id, ModelMap model, HttpServletRequest request) {
		FacilityCommand command = new FacilityCommand();
		if (SecurityUtil.hasAllPermissionsAtCurrentFacility(PermissionType.FACILITY_EDIT_ALL)) {
			if (id != null) {
				Facility facility = facilityDAO.findRequiredByPrimaryKey(id);
				command = new FacilityCommand(facility);
			}
		} else if (SecurityUtil.hasAllPermissionsAtCurrentFacility(PermissionType.FACILITY_EDIT_CURRENT)) {
			command = new FacilityCommand(getRequiredFacilityContext());
		} else {
			throw new SecurityException();
		}

		if (command.getFacility() != null && command.getFacility().getStationParameters() != null
				&& command.getFacility().getStationParameters().getLanguage() == null) {
			Language lang = languageDAO.findByLookup(LanguageType.ENGLISH);
			command.getFacility().getStationParameters().setLanguage(lang);
		}

		model.addAttribute(DEFAULT_COMMAND_NAME, command);
		createReferenceData(model);
		return "facilityEdit";
	}

	private void createReferenceData(ModelMap model) {
		model.put("allTypes", facilityTypeDAO.findByLookups(TIMEKEEPING, GAMES).values());
		model.put("allVISNs", administrativeUnitDAO.findAllSorted());
		model.put("allFacilities", facilityDAO.findAllSorted());
		model.put("allLanguages", languageDAO.findAllSorted());

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

		WebUtil.addEnumToModel(FacilityTypeValue.class, model);
	}

	@RequestMapping("/facility/kiosk")
	// @PreAuthorize("hasAuthority('" + Permission.VOLUNTEER_VIEW + "')")
	public @ResponseBody Map<String, Object> getKiosksForFacility(@RequestParam long facilityId) {
		Facility f = facilityDAO.findRequiredByPrimaryKey(facilityId);

		Map<String, Object> results = new HashMap<>();

		SortedSet<Kiosk> r = new TreeSet<>(f.getKiosks());
		results.put("kiosks", r);

		Map<Long, Boolean> statuses = r.stream()
				.collect(Collectors.toMap(k -> k.getId(), k -> k.isPrinterOnline(maxQuietPrinterStatusCheckMinutes)));
		results.put("kioskStatusMap", statuses);

		Map<Long, Integer> printRequestCountMap = r.stream()
				.collect(Collectors.toMap(k -> k.getId(), k -> k.getPrintRequests().size()));
		results.put("kioskPrintRequestCountMap", printRequestCountMap);

		return results;
	}

	@RequestMapping("/facility/kiosk/delete")
	// @PreAuthorize("hasAuthority('" + Permission.VOLUNTEER_VIEW + "')")
	public @ResponseBody boolean kioskDelete(@RequestParam long kioskId,
			@ModelAttribute(DEFAULT_COMMAND_NAME) FacilityCommand command) throws ServiceValidationException {
		kioskService.delete(kioskId);
		/*
		 * Without this, Hibernate gets confused since the Facility in the
		 * command has a reference to a Kiosk that was already deleted - CPB
		 */
		command.refreshFacility(facilityDAO.findRequiredByPrimaryKey(command.getFacility().getId()));

		return true;
	}

	@RequestMapping("/facilitySubmit.htm")
	@PreAuthorize("hasAnyAuthority('" + Permission.FACILITY_CREATE + ", " + Permission.FACILITY_EDIT_CURRENT + ", "
			+ Permission.FACILITY_EDIT_ALL + "')")
	public String submitFacility(@ModelAttribute(DEFAULT_COMMAND_NAME) FacilityCommand command, BindingResult result,
			SessionStatus status, ModelMap model, HttpServletRequest request) throws ValidationException {
		Facility facility = command.getFacility();
		boolean isEdit = facility.isPersistent();

		Facility parent = command.getParentId() == null ? null
				: facilityDAO.findRequiredByPrimaryKey(command.getParentId());
		facility.setParent(parent);

		/* Validation step (JSR303, other custom logic in the validator) */
		facilityValidator.validate(command, result, false, "facility");
		if (facility.getStationParameters() != null && facility.getStationParameters().getAlternateLanguage() != null
				&& (facility.getStationParameters().getRequiresAlternateLanguage() == null
						|| facility.getStationParameters().getRequiresAlternateLanguage() != true)) {
			facility.getStationParameters().setRequiresAlternateLanguage(true);
		} else if (facility.getStationParameters() != null
				&& (facility.getStationParameters().getRequiresAlternateLanguage() == null
						|| facility.getStationParameters().getRequiresAlternateLanguage() != false))
			facility.getStationParameters().setRequiresAlternateLanguage(false);

		boolean hasErrors = result.hasErrors();

		if (!hasErrors) {
			try {
				facility = facilityService.saveOrUpdate(facility);
				if (facility.getId().equals(getFacilityContextId()))
					setFacilityContext(facility);
				userNotifier.notifyUserOnceWithMessage(request,
						getMessage(isEdit ? "facility.update.success" : "facility.create.success"));
			} catch (ServiceValidationException e) {
				webValidationService.handle(e, result);
				hasErrors = true;
			}
		}

		if (hasErrors) {
			createReferenceData(model);
			return isEdit ? "facilityEdit" : "facilityCreate";
		} else {
			status.setComplete();
			return "redirect:/facilityEdit.htm?id=" + facility.getId();
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@RequestMapping("/findFacilitiesForHierarchyDisplay")
	// @PreAuthorize("hasAuthority('" + Permission.VOLUNTEER_VIEW + "')")
	@JsonView(AdministrativeUnitView.ShowHierarchy.class)
	public @ResponseBody SortedSet<? extends FacilityNode<?>> findFacilitiesForHierarchyDisplay() {
		SortedSet facilities = administrativeUnitDAO.findAllSorted();

		SortedSet<? extends FacilityNode<?>> rootFacilities = facilityDAO.findRootFacilities();
		for (Object f : facilities)
			rootFacilities.removeAll(((AdministrativeUnit) f).getFacilityChildren());

		facilities.add(new FacilityNode() {
			@Override
			public SortedSet getFacilityChildren() {
				return rootFacilities;
			}

			@Override
			public Long getId() {
				return -1L;
			}

			@Override
			public String getDisplayName() {
				return "(No VISN Assigned)";
			}

			@Override
			public int compareTo(Object o) {
				if (equals(o))
					return 0;
				return 1;
			}

			@Override
			public boolean isActive() {
				return true;
			}
		});

		return facilities;
	}

	@RequestMapping("/unlinkSDSFacilityFromFacility")
	// @PreAuthorize("hasAuthority('" + Permission.VOLUNTEER_VIEW + "')")
	public @ResponseBody boolean unlinkSDSFacilityFromFacility(@RequestParam long facilityId) {
		facilityService.unlinkSDSFacilityFromFacility(facilityId);
		return true;
	}

	@RequestMapping("/linkSDSFacilityToFacility")
	// @PreAuthorize("hasAuthority('" + Permission.VOLUNTEER_VIEW + "')")
	public @ResponseBody boolean linkSDSFacilityToFacility(@RequestParam long facilityId,
			@RequestParam long vaFacilityId) {
		facilityService.linkSDSFacilityToFacility(facilityId, vaFacilityId);
		return true;
	}

	@RequestMapping("/facility/location")
	// @PreAuthorize("hasAuthority('" + Permission.VOLUNTEER_VIEW + "')")
	public @ResponseBody Map<String, Object> getLocationsForFacility(@RequestParam long facilityId,
			@RequestParam(required = false) Boolean activeStatus,
			@RequestParam(required = false) Boolean includeCounts) {
		Map<String, Object> results = new HashMap<>();

		SortedSet<Location> r = facilityDAO.findByCriteria(null, facilityId, Location.class, activeStatus);
		results.put("locations", r);
		if (includeCounts != null && includeCounts) {
			Map<Long, Integer[]> counts = locationDAO
					.countVolunteersForLocations(PersistenceUtil.translateObjectsToIds(r));
			results.put("countsMap", counts);
		}
		return results;
	}

	@RequestMapping("/facility/location/saveOrUpdate")
	// @PreAuthorize("hasAuthority('" + Permission.VOLUNTEER_VIEW + "')")
	public @ResponseBody Location locationSaveOrUpdate(@RequestParam long facilityId,
			@RequestParam(required = false) Long locationId, @RequestParam String name,
			@RequestParam String addressLine1, @RequestParam String addressLine2, @RequestParam String city,
			@RequestParam State state, @RequestParam String zip, @RequestParam String contactName,
			@RequestParam String contactRole, @RequestParam String contactPhone, @RequestParam String contactEmail,
			@ModelAttribute(DEFAULT_COMMAND_NAME) FacilityCommand command) throws ServiceValidationException {
		Location location = new Location();
		if (locationId != null) {
			location = locationDAO.findRequiredByPrimaryKey(locationId);
		} else {
			location.setParent(facilityDAO.findRequiredByPrimaryKey(facilityId));
			location.setActive(true);
		}
		location.setName(name);
		location.setAddressLine1(addressLine1);
		location.setAddressLine2(addressLine2);
		location.setCity(city);
		location.setState(state);
		location.setZip(zip);
		location.setContactName(contactName);
		location.setContactRole(contactRole);
		location.setContactEmail(contactEmail);
		location.setContactPhone(contactPhone);
		location = locationService.saveOrUpdate(location);

		/*
		 * Without this, Hibernate gets confused since the Facility in the
		 * command has a reference to a Location that was already deleted - CPB
		 */
		command.refreshFacility(facilityDAO.findRequiredByPrimaryKey(command.getFacility().getId()));

		return location;
	}

	@RequestMapping("/facility/kiosk/saveOrUpdate")
	// @PreAuthorize("hasAuthority('" + Permission.VOLUNTEER_VIEW + "')")
	public @ResponseBody Kiosk kioskSaveOrUpdate(@RequestParam long facilityId,
			@RequestParam(required = false) Long kioskId, @RequestParam String location,
			@RequestParam boolean registered, @ModelAttribute(DEFAULT_COMMAND_NAME) FacilityCommand command)
			throws ServiceValidationException {
		Kiosk kiosk = new Kiosk();
		if (kioskId != null) {
			kiosk = kioskDAO.findRequiredByPrimaryKey(kioskId);
		} else {
			kiosk.setFacility(facilityDAO.findRequiredByPrimaryKey(facilityId));
		}
		kiosk.setLocation(location);
		kiosk.setRegistered(registered);
		kiosk = kioskService.saveOrUpdate(kiosk);

		/*
		 * Without this, Hibernate gets confused since the Facility in the
		 * command has a reference to a Location that was already deleted - CPB
		 */
		command.refreshFacility(facilityDAO.findRequiredByPrimaryKey(command.getFacility().getId()));

		return kiosk;
	}

	@RequestMapping("/facility/location/delete")
	// @PreAuthorize("hasAuthority('" + Permission.VOLUNTEER_VIEW + "')")
	public @ResponseBody boolean locationDelete(@RequestParam long locationId,
			@ModelAttribute(DEFAULT_COMMAND_NAME) FacilityCommand command) throws ServiceValidationException {
		locationService.delete(locationId);
		/*
		 * Without this, Hibernate gets confused since the Facility in the
		 * command has a reference to a Location that was already deleted - CPB
		 */
		command.refreshFacility(facilityDAO.findRequiredByPrimaryKey(command.getFacility().getId()));

		return true;
	}

	@RequestMapping("/facility/location/inactivate")
	// @PreAuthorize("hasAuthority('" + Permission.VOLUNTEER_VIEW + "')")
	public @ResponseBody boolean locationInactivate(@RequestParam long locationId) throws ServiceValidationException {
		locationService.inactivate(locationId);
		return true;
	}

	@RequestMapping("/facility/location/reactivate")
	// @PreAuthorize("hasAuthority('" + Permission.VOLUNTEER_VIEW + "')")
	public @ResponseBody boolean locationReactivate(@RequestParam long locationId) throws ServiceValidationException {
		locationService.reactivate(locationId);
		return true;
	}

	@RequestMapping("/facility/quickSearch")
	public @ResponseBody Map<String, Object> findAppUserByNameOrUsername(@RequestParam int draw,
			@RequestParam int start, @RequestParam int length, @RequestParam(name = "search[value]") String searchValue,
			@RequestParam(name = "search[regex]") boolean searchIsRegex) {
		Map<String, Object> resultMap = new HashMap<>();

		SortedSet<QuickSearchResult> results = null;
		if (StringUtils.isNotBlank(searchValue)) {
			List<QuickSearchResult> vaFacilities = facilityDAO.findUnlinkedMatchingVAFacilities(searchValue, length);
			results = new TreeSet<>(vaFacilities);
		} else {
			results = new TreeSet<>();
		}

		resultMap.put("data", results);
		resultMap.put("draw", draw);
		return resultMap;
	}

	@RequestMapping("/donGenPostFundList")
	public @ResponseBody List<DonGenPostFund> getDonGenPostFundList(@RequestParam long facilityId) {
		if (facilityId == 0 || facilityId == -1)
			return new ArrayList<DonGenPostFund>();
		List<DonGenPostFund> donGenPostFundList = donGenPostFundDAO.findByFacility(facilityId);
		return donGenPostFundList;
	}

	@RequestMapping("/donReferenceList")
	public @ResponseBody List<DonationReference> getDonReferenceList(@RequestParam long facilityId) {
		if (facilityId == 0 || facilityId == -1)
			return new ArrayList<DonationReference>();
		List<DonationReference> donReferenceList = donationReferenceDAO.findDonReferenceByFacilityId(facilityId);

		return donReferenceList;
	}

	@RequestMapping("/facility/donGenPostFund/saveOrUpdate")
	public @ResponseBody DonGenPostFund donGenPostFundSaveOrUpdate(@RequestParam long facilityId,
			@RequestParam(required = false) Long genPostFundId, String genPostFund, @RequestParam boolean active)
			throws ServiceValidationException {
		Facility facility = facilityDAO.findRequiredByPrimaryKey(facilityId);
		DonGenPostFund dgPostFund = new DonGenPostFund();
		if (genPostFundId != null)
			dgPostFund = donGenPostFundDAO.findByPrimaryKey(genPostFundId);
		dgPostFund.setGeneralPostFund(genPostFund);
		dgPostFund.setFacility(facility);
		dgPostFund.setInactive(!active);
		return donGenPostFundService.saveOrUpdate(dgPostFund);
	}

	@RequestMapping("/facility/donReference/saveOrUpdate")
	public @ResponseBody DonationReference donReferenceSaveOrUpdate(@RequestParam long facilityId, Long donReferenceId,
			String donRef, @RequestParam boolean active) throws ServiceValidationException {
		Facility facility = facilityDAO.findRequiredByPrimaryKey(facilityId);
		DonationReference donReference = new DonationReference();
		if (donReferenceId != null)
			donReference = donationReferenceDAO.findByPrimaryKey(donReferenceId);
		donReference.setDonationReference(donRef);
		donReference.setFacility(facility);
		donReference.setInactive(!active);
		return donationReferenceService.saveOrUpdate(donReference);
	}

	@RequestMapping("/facility/donGenPostFund/delete")
	public @ResponseBody boolean donGenPostFundDelete(@RequestParam long donGenPostFundId)
			throws ServiceValidationException {
		try {
			donGenPostFundService.delete(donGenPostFundId);
		} catch (Exception exp) {
			throw new ServiceValidationException("donGenPostFund.error.deleteReferenced");
		}
		return true;
	}

	@RequestMapping("/facility/donGenPostFund/inactivate")
	public @ResponseBody boolean donGenPostFundInactivate(@RequestParam long donGenPostFundId)
			throws ServiceValidationException {
		donGenPostFundService.inactivate(donGenPostFundId);
		return true;
	}

	@RequestMapping("/facility/donGenPostFund/reactivate")
	public @ResponseBody boolean donGenPostFundReactivate(@RequestParam long donGenPostFundId)
			throws ServiceValidationException {
		donGenPostFundService.reactivate(donGenPostFundId);
		return true;
	}

	@RequestMapping("/facility/donReference/delete")
	public @ResponseBody boolean donReferenceDelete(@RequestParam long donReferenceId)
			throws ServiceValidationException {
		try {
			donationReferenceService.delete(donReferenceId);
		} catch (Exception exp) {
			throw new ServiceValidationException("donReferece.error.deleteReferenced");
		}
		return true;
	}

	@RequestMapping("/facility/donReference/inactivate")
	public @ResponseBody boolean donReferenceInactivate(@RequestParam long donReferenceId)
			throws ServiceValidationException {
		donationReferenceService.inactivate(donReferenceId);
		return true;
	}

	@RequestMapping("/facility/donReference/reactivate")
	public @ResponseBody boolean donReferenceReactivate(@RequestParam long donReferenceId)
			throws ServiceValidationException {
		donationReferenceService.reactivate(donReferenceId);
		return true;
	}

}
