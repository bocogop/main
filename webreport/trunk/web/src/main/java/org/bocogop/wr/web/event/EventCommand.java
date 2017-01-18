package org.bocogop.wr.web.event;

import org.bocogop.shared.model.Event;

public class EventCommand {

	// -------------------------------- Fields

	private Event event;

	// -------------------------------- Constructors

	public EventCommand() {
	}

	public EventCommand(Event event) {
		this.event = event;
	}

	// -------------------------------- Business Methods

	// -------------------------------- Accessor Methods

	public Event getEvent() {
		return event;
	}

	public void setEvent(Event event) {
		this.event = event;
	}

}
