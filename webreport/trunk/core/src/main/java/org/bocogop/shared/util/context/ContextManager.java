package org.bocogop.shared.util.context;

import org.bocogop.shared.model.Event;

/**
 * Represents the contract for any class responsible for providing session-bound
 * values.
 * 
 */
public interface ContextManager {

	Event getEventContext();

	void setEventContext(Event e);
	
}