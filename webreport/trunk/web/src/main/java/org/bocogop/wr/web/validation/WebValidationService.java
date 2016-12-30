package org.bocogop.wr.web.validation;

import java.util.Locale;
import java.util.Map;

import org.bocogop.shared.service.validation.ServiceValidationException;
import org.bocogop.shared.util.SecurityUtil;
import org.bocogop.wr.web.ajax.AjaxRequestHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;

@Service
public class WebValidationService {

	@Autowired
	private MessageSource messageSource;

	/**
	 * Flags an error in the specified Errors object based on the nature of the
	 * specified ServiceValidationException "e".
	 * <ul>
	 * <li>If e was thrown due to a specific service-level parameter being
	 * invalid, and thus has the "parameterName" field populated, then a field
	 * with the same name will be rejected in the errors object.
	 * <li>If e was thrown because of a global business-level validation
	 * failure, and thus the "parameterName" field is not set, then this method
	 * will bind a ServiceValidationGlobalError to the specified BindingResult.
	 * </ul>
	 * <p>
	 * Note that this behavior requires the target of the BindingResult object
	 * (i.e. the command class in the web layer) to name its fields the same as
	 * what is thrown by the service layer method, so that the field-level
	 * errors can be bound to the correct places. CPB
	 */
	public void handle(ServiceValidationException e, BindingResult result) {
		e.setTimeZoneOverride(SecurityUtil.getCurrentUser().getTimeZone());

		if (e.getParameterName() != null && result.getFieldType(e.getParameterName()) != null) {
			result.addError(new ServiceValidationFieldError(e, result));
		} else {
			result.addError(new ServiceValidationGlobalError(e, result));
		}
	}

	public void handle(Throwable e, BindingResult result, String defaultMessage) {
		result.addError(new ObjectError(result.getObjectName(), defaultMessage));
	}

	/**
	 * 
	 * @param retMap
	 */
	public void populateAjaxMap(MessageSourceResolvable e, Map<String, Object> retMap) {
		if (e instanceof ServiceValidationException) {
			ServiceValidationException f = (ServiceValidationException) e;
			f.setTimeZoneOverride(SecurityUtil.getCurrentUser().getTimeZone());

			retMap.put("serviceValidationError", true);
		}

		retMap.put(AjaxRequestHandler.AJAX_STATUS_MESSAGE_KEY, messageSource.getMessage(e, Locale.getDefault()));
	}

}
