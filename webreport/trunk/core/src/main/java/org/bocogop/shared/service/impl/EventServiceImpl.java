package org.bocogop.shared.service.impl;

import org.bocogop.shared.model.Event;
import org.bocogop.shared.model.Permission.PermissionType;
import org.bocogop.shared.service.EventService;
import org.bocogop.shared.service.validation.ServiceValidationException;
import org.bocogop.shared.util.SecurityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class EventServiceImpl extends AbstractServiceImpl implements EventService {
	private static final Logger log = LoggerFactory.getLogger(EventServiceImpl.class);

	@Override
	public Event saveOrUpdate(Event event) throws ServiceValidationException {
		/* Business-level validations */
		if (SecurityUtil.hasAllPermissionsAtCurrentPrecinct(PermissionType.EVENT_EDIT)) {
			event = eventDAO.saveOrUpdate(event);
		}

		return event;
	}

	@Override
	public void delete(long eventId) {
		eventDAO.delete(eventId);
	}

}
