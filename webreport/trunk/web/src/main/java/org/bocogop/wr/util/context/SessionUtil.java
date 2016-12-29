package org.bocogop.wr.util.context;

import java.util.concurrent.Callable;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import org.bocogop.shared.model.lookup.sds.VAFacility;
import org.bocogop.shared.persistence.lookup.sds.VAFacilityDAO;
import org.bocogop.wr.model.facility.Facility;
import org.bocogop.wr.model.facility.Facility.FacilityValue;
import org.bocogop.wr.persistence.dao.facility.FacilityDAO;

@Component
public class SessionUtil extends org.bocogop.shared.util.context.SessionUtil {

	private static String KEY_PREFIX = SessionUtil.class.getName();
	private static final String HTTP_SESSION_CONTEXT_FACILITY_KEY = KEY_PREFIX + ".FACILITY";
	private static final String HTTP_SESSION_CONTEXT_FACILITY_NAME_KEY = KEY_PREFIX + ".FACILITY_NAME";
	private static final String HTTP_SESSION_CONTEXT_FACILITY_NUM_MEALS_KEY = KEY_PREFIX + ".FACILITY_NUM_MEALS";
	private static final String HTTP_SESSION_CONTEXT_FACILITY_IS_CO_KEY = KEY_PREFIX + ".FACILITY_IS_CENTRAL_OFFICE";

	protected static ThreadLocal<ContextManager> contextManagerThreadOverride = new ThreadLocal<>();

	/**
	 * For the current thread only, this sets the ContextManager which will be
	 * used to store and retrieve values. This ContextManager will continue to
	 * be used in this thread until the removeContextProviderOverride() method
	 * is called. To remove the risk of forgetting to restore the default
	 * provider, consider using the runWithOverride() method.
	 * 
	 * @param c
	 */
	public static void setContextManagerOverride(ContextManager c) {
		contextManagerThreadOverride.set(c);
	}

	/**
	 * Removes the ContextManager bound to this thread (if any).
	 */
	public static void removeContextManagerOverride() {
		contextManagerThreadOverride.remove();
	}

	/**
	 * This method safely sets the current thread's ContextManager, then runs
	 * the specified Callable, and finally restores the ContextManager to the
	 * default. The restoration is guaranteed regardless of an Exception being
	 * thrown.
	 */
	public static <T> T runWithContext(Callable<T> r, ContextManager cm) throws Exception {
		setContextManagerOverride(cm);
		try {
			T result = r.call();
			return result;
		} finally {
			removeContextManagerOverride();
		}
	}

	@Autowired
	private VAFacilityDAO vaFacilityDAO;
	@Autowired
	private FacilityDAO facilityDAO;

	// ---------------------------------------- Static helper methods

	public static Facility getFacilityContext() {
		ContextManager cp = contextManagerThreadOverride.get();
		if (cp != null)
			return cp.getFacilityContext();

		HttpSession s = getHttpSession();
		return s == null ? null : (Facility) s.getAttribute(HTTP_SESSION_CONTEXT_FACILITY_KEY);
	}

	public static String getFacilityContextName() {
		ContextManager cp = contextManagerThreadOverride.get();
		if (cp != null)
			return cp.getFacilityContextName();

		HttpSession s = getHttpSession();
		return s == null ? null : (String) s.getAttribute(HTTP_SESSION_CONTEXT_FACILITY_NAME_KEY);
	}

	public static Integer getFacilityContextNumMeals() {
		ContextManager cp = contextManagerThreadOverride.get();
		if (cp != null)
			return cp.getFacilityContextNumMeals();

		HttpSession s = getHttpSession();
		return s == null ? null : (Integer) s.getAttribute(HTTP_SESSION_CONTEXT_FACILITY_NUM_MEALS_KEY);
	}

	public static Boolean isFacilityContextCentralOffice() {
		ContextManager cp = contextManagerThreadOverride.get();
		if (cp != null)
			return cp.getFacilityContextCentralOffice();

		HttpSession s = getHttpSession();
		return s == null ? null : (Boolean) s.getAttribute(HTTP_SESSION_CONTEXT_FACILITY_IS_CO_KEY);
	}

	// --------------------------------- Instance methods

	@Transactional
	public void setFacilityContext(VAFacility vaFacility, Facility f) {
		ContextManager cp = contextManagerThreadOverride.get();
		if (cp != null) {
			VAFacility attachedVaFacility = vaFacility == null ? null
					: vaFacilityDAO.findRequiredByPrimaryKey(vaFacility.getId());
			Facility attachedFacility = f == null ? null : facilityDAO.findRequiredByPrimaryKey(f.getId());

			org.bocogop.shared.util.context.SessionUtil.setSiteContext(attachedVaFacility);
			cp.setFacilityContext(attachedFacility);
			cp.setFacilityContextName(attachedFacility == null ? null : attachedFacility.getDisplayName());
			cp.setFacilityContextNumMeals(attachedFacility == null ? null
					: attachedFacility.getStationParameters() == null ? null
							: attachedFacility.getStationParameters().getNumberOfMeals());
			cp.setFacilityContextCentralOffice(
					attachedFacility == null ? null : FacilityValue.CENTRAL_OFFICE.getId() == attachedFacility.getId());
		} else {
			setFacilityContext(vaFacility, f, getHttpSession());
		}
	}

	/**
	 * Sets the specified Facility & VAFacility in the specified HttpSession and
	 * performs additional initialization steps for this new facility context.
	 * This method should only be called by framework code outside where Spring
	 * binds the ServletContext to the thread, but where an HttpSession is still
	 * available (so the assumption is we aren't running with a custom
	 * ContextManager). Otherwise, the setFacilityContext(VAFacility, Facility)
	 * method should be used. CPB
	 */
	@Transactional
	public void setFacilityContext(VAFacility vaFacility, Facility f, HttpSession session) {
		if (session == null)
			throw new IllegalArgumentException("No HttpSession was found.");
		
		VAFacility attachedVaFacility = vaFacility == null ? null
				: vaFacilityDAO.findRequiredByPrimaryKey(vaFacility.getId());
		Facility attachedFacility = f == null ? null : facilityDAO.findRequiredByPrimaryKey(f.getId());

		org.bocogop.shared.util.context.SessionUtil.setHttpSessionSiteContext(attachedVaFacility, session);
		session.setAttribute(HTTP_SESSION_CONTEXT_FACILITY_KEY, attachedFacility);
		session.setAttribute(HTTP_SESSION_CONTEXT_FACILITY_NAME_KEY,
				attachedFacility == null ? null : attachedFacility.getDisplayName());
		session.setAttribute(HTTP_SESSION_CONTEXT_FACILITY_NUM_MEALS_KEY,
				attachedFacility == null ? null
						: attachedFacility.getStationParameters() == null ? null
								: attachedFacility.getStationParameters().getNumberOfMeals());
		session.setAttribute(HTTP_SESSION_CONTEXT_FACILITY_IS_CO_KEY,
				attachedFacility == null ? null : FacilityValue.CENTRAL_OFFICE.getId() == attachedFacility.getId());
	}

}
