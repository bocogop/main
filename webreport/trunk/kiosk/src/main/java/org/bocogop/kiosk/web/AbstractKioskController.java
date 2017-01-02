package org.bocogop.kiosk.web;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.bocogop.kiosk.util.SessionUtil;
import org.bocogop.shared.model.Event;
import org.bocogop.shared.model.voter.Voter;
import org.bocogop.shared.util.SecurityUtil;
import org.bocogop.shared.web.AbstractCommonAppController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.util.WebUtils;

public abstract class AbstractKioskController extends AbstractCommonAppController {

	@Autowired
	protected SessionUtil sessionUtil;

	public Voter getCurrentUser() {
		return SecurityUtil.getCurrentUserAs(Voter.class);
	}

	public Long getEventId(HttpServletRequest request) {
		Cookie eventIdCookie = WebUtils.getCookie(request, "eventId");
		Long eventId = eventIdCookie == null ? null : new Long(eventIdCookie.getValue());
		return eventId;
	}

	protected void setEventContext(Event f) {
		sessionUtil.setEventContext(f);
	}

	protected Long getEventContextId() {
		Event f = SessionUtil.getEventContext();
		return f == null ? null : f.getId();
	}

	protected Event getEventContext() {
		Event f = SessionUtil.getEventContext();
		if (f == null)
			return null;

		// reattach
		f = eventDAO.findRequiredByPrimaryKey(f.getId());
		return f;
	}

	protected Event getRequiredEventContext() {
		Event f = getEventContext();
		if (f == null)
			throw new IllegalArgumentException("Required event context was not found");
		return f;
	}

}
