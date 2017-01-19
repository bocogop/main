package org.bocogop.wr.web.event;

import static org.bocogop.shared.util.SecurityUtil.hasAllPermissions;

import java.util.Collection;
import java.util.SortedSet;

import javax.servlet.http.HttpServletRequest;

import org.bocogop.shared.model.Event;
import org.bocogop.shared.model.Event.EventView;
import org.bocogop.shared.model.Participation;
import org.bocogop.shared.model.Participation.ParticipationView;
import org.bocogop.shared.model.Permission;
import org.bocogop.shared.model.Permission.PermissionType;
import org.bocogop.shared.service.validation.ServiceValidationException;
import org.bocogop.shared.web.AbstractAppController;
import org.bocogop.shared.web.validation.ValidationException;
import org.bocogop.wr.web.breadcrumbs.Breadcrumb;
import org.springframework.beans.factory.annotation.Autowired;
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

@Controller
@SessionAttributes(value = { AbstractAppController.DEFAULT_COMMAND_NAME })
public class EventController extends AbstractAppController {

	@Autowired
	private EventValidator eventValidator;

	@RequestMapping("/eventList.htm")
	@Breadcrumb("Event List")
	public String eventList(ModelMap model) {
		setFormAsReadOnlyUnlessUserHasPermissions(model, PermissionType.EVENT_EDIT);
		return "eventList";
	}

	@RequestMapping("/event")
	@JsonView(EventView.List.class)
	public @ResponseBody Collection<Event> getEvents() {
		SortedSet<Event> events = eventDAO.findAllSorted();
		// breadcrumbsInterceptor.updateCurrentBreadcrumbParameters(session,
		// params);
		return events;
	}

	@RequestMapping("/event/delete")
	public @ResponseBody boolean eventDelete(@RequestParam long id) {
		eventService.delete(id);
		return true;
	}

	@RequestMapping("/event/participation")
	@JsonView(ParticipationView.VotersForEvent.class)
	public @ResponseBody Collection<Participation> getParticipationsForEvent(@RequestParam long eventId) {
		return participationDAO.findByCriteria(null, eventId);
	}

	@RequestMapping("/event/participant/add")
	public @ResponseBody boolean addParticipantToEvent(@RequestParam long eventId, @RequestParam long voterId)
			throws ServiceValidationException {
		Participation p = new Participation();
		p.setVoter(voterDAO.findRequiredByPrimaryKey(voterId));
		p.setEvent(eventDAO.findRequiredByPrimaryKey(eventId));
		p = participationService.saveOrUpdate(p);
		return true;
	}

	@RequestMapping("/participation/delete")
	public @ResponseBody boolean deleteParticipation(@RequestParam long participationId)
			throws ServiceValidationException {
		participationService.delete(participationId);
		return true;
	}

	// ------------------------------------------------------- Event form
	// display and submit methods

	@RequestMapping("/eventAdd.htm")
	// Don't want a Breadcrumb here since we want to force them to search first
	// every time - CPB
	// @Breadcrumb("Create Event")
	@PreAuthorize("hasAuthority('" + Permission.EVENT_EDIT + "')")
	public String eventCreate(ModelMap model, HttpServletRequest request) {
		Event event = new Event();

		EventCommand command = new EventCommand(event);
		model.addAttribute(DEFAULT_COMMAND_NAME, command);
		createReferenceData(command, model);
		return "eventAdd";
	}

	@RequestMapping("/eventEdit.htm")
	@Breadcrumb("Edit Event")
	public String eventEdit(@RequestParam long id, ModelMap model, HttpServletRequest request) {
		Event event = eventDAO.findRequiredByPrimaryKey(id);
		EventCommand command = new EventCommand(event);
		model.addAttribute(DEFAULT_COMMAND_NAME, command);
		createReferenceData(command, model);
		return "eventEdit";
	}

	private void createReferenceData(EventCommand command, ModelMap model) {
		if (!hasAllPermissions(PermissionType.EVENT_EDIT))
			setFormAsReadOnly(model, true);
	}

	@RequestMapping("/eventSubmit.htm")
	@PreAuthorize("hasAuthority('" + Permission.EVENT_EDIT + "')")
	public String eventSubmit(@ModelAttribute(DEFAULT_COMMAND_NAME) EventCommand command, BindingResult result,
			SessionStatus status, ModelMap model, HttpServletRequest request) throws ValidationException {
		Event event = command.getEvent();

		boolean isEdit = event.isPersistent();

		eventValidator.validate(command, result, false, "event");
		boolean hasErrors = result.hasErrors();

		if (!hasErrors) {
			try {
				event = eventService.saveOrUpdate(event);
				userNotifier.notifyUserOnceWithMessage(request,
						getMessage(isEdit ? "event.update.success" : "event.create.success"));
			} catch (ServiceValidationException e) {
				webValidationService.handle(e, result);
				hasErrors = true;
			}
		}

		if (hasErrors) {
			createReferenceData(command, model);
			return isEdit ? "eventEdit" : "eventCreate";
		} else {
			status.setComplete();

			String toPage = "/eventEdit.htm?id=" + event.getId();
			return "redirect:" + toPage;
		}
	}

}
