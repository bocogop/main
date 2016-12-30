package org.bocogop.shared.util.context;

import java.util.concurrent.Callable;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.bocogop.shared.util.ServletUtil;

/**
 * A utility for managing objects that represent session state. The session is a
 * more broad concept than a HttpSession since background jobs and unit tests
 * can operate in "headless" mode. This class defaults to using the HttpSession
 * associated with the Spring-managed threadbound ServletContext as its default
 * means of storing and retrieving data, but that context provider can be
 * overridden temporarily or permanently for the current thread via the
 * setContextProviderOverride() method. CPB
 */
public class SessionUtil {

	private static String KEY_PREFIX = SessionUtil.class.getName();
	public static final String HTTP_SESSION_CONTEXT_SITE_KEY = KEY_PREFIX + ".SITE";

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
	public static <T> T runWithOverride(Callable<T> r, ContextManager cm) throws Exception {
		setContextManagerOverride(cm);
		try {
			T result = r.call();
			return result;
		} finally {
			removeContextManagerOverride();
		}
	}

	// ---------------------------------------- Static helper methods

	/**
	 * Returns the HttpSession of the current thread-bound ServletRequest (or
	 * throws an Exception if running outside of a ServletContext).
	 */
	public static HttpSession getHttpSession() {
		HttpServletRequest r = ServletUtil.getThreadBoundServletRequest();
		return r == null ? null : r.getSession(true);
	}

}
