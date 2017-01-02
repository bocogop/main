package org.bocogop.kiosk.util;

import javax.servlet.http.HttpSession;

import org.bocogop.shared.model.Event;
import org.bocogop.shared.persistence.dao.EventDAO;
import org.bocogop.shared.util.context.ContextManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class SessionUtil extends org.bocogop.shared.util.context.SessionUtil {

	private static String KEY_PREFIX = SessionUtil.class.getName();
	private static final String HTTP_SESSION_CONTEXT_EVENT_KEY = KEY_PREFIX + ".EVENT";

	@Autowired
	private EventDAO eventDAO;

	// ---------------------------------------- Static helper methods

	public static Event getEventContext() {
		ContextManager cp = contextManagerThreadOverride.get();
		if (cp != null)
			return cp.getEventContext();

		HttpSession s = getHttpSession();
		return s == null ? null : (Event) s.getAttribute(HTTP_SESSION_CONTEXT_EVENT_KEY);
	}

	// --------------------------------- Instance methods

	@Transactional
	public void setEventContext(Event f) {
		ContextManager cp = contextManagerThreadOverride.get();
		if (cp != null) {
			Event attachedEvent = f == null ? null : eventDAO.findRequiredByPrimaryKey(f.getId());
			cp.setEventContext(attachedEvent);
		} else {
			setEventContext(f, getHttpSession());
		}
	}

	/**
	 * Sets the specified Event & VAEvent in the specified HttpSession and
	 * performs additional initialization steps for this new event context. This
	 * method should only be called by framework code outside where Spring binds
	 * the ServletContext to the thread, but where an HttpSession is still
	 * available (so the assumption is we aren't running with a custom
	 * ContextManager). Otherwise, the setEventContext(Event) method
	 * should be used. CPB
	 */
	@Transactional
	public void setEventContext(Event f, HttpSession session) {
		if (session == null)
			throw new IllegalArgumentException("No HttpSession was found.");

		Event attachedEvent = f == null ? null : eventDAO.findRequiredByPrimaryKey(f.getId());
		session.setAttribute(HTTP_SESSION_CONTEXT_EVENT_KEY, attachedEvent);
	}

}
