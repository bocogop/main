package org.bocogop.shared.service;

import org.bocogop.shared.model.Event;
import org.bocogop.shared.service.validation.ServiceValidationException;

public interface EventService {

	Event saveOrUpdate(Event event) throws ServiceValidationException;

	void delete(long eventId);

}
