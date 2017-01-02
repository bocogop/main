package org.bocogop.shared.util.context;

import org.bocogop.shared.model.Event;

public class BasicContextManager implements ContextManager {

	Event eventContext;

	public BasicContextManager() {}
	
	public BasicContextManager(Event eventContext) {
		this.eventContext = eventContext;
	}

	public Event getEventContext() {
		return eventContext;
	}

	public void setEventContext(Event eventContext) {
		this.eventContext = eventContext;
	}

}
