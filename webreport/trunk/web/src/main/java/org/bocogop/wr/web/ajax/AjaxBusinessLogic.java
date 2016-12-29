package org.bocogop.wr.web.ajax;

import java.util.Map;

public interface AjaxBusinessLogic {

	/**
	 * Implementors are expected to perform some business logic and return a
	 * success message. If no particular message is needed, null may be
	 * returned.
	 * <p>
	 * The specified map is provided to allow implementors to return extra data
	 * elements besides the success message, if desired. Two keywords are
	 * reserved and will be overwritten, if used:
	 * AjaxRequestHandler.AJAX_STATUS_MESSAGE_KEY and
	 * AjaxRequestHandler.AJAX_ERROR_STATUS_KEY.
	 * <p>
	 * Implementors need to throw an Exception to signify failure of the
	 * business logic. If the Exception thrown is an instance of a
	 * MessageSourceResolvable (such as a ServiceValidationException), it will
	 * be used to resolve the error message; otherwise, the Exception's message
	 * field will be used.
	 * <p>
	 * CPB
	 */
	String doLogic(Map<String, Object> dataMap) throws Exception;

}
