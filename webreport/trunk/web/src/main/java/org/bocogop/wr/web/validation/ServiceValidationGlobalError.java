package org.bocogop.wr.web.validation;

import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;

import org.bocogop.shared.service.validation.ServiceValidationException;

public class ServiceValidationGlobalError extends ObjectError {
	private static final long serialVersionUID = 486961310693080512L;

	private ServiceValidationException rootException;

	public ServiceValidationGlobalError(ServiceValidationException e, BindingResult r) {
		super(r.getObjectName(), e.getCodes(), e.getArguments(), e.getDefaultMessage());
		this.rootException = e;
	}

	public String getOriginalErrorCode() {
		return rootException.getErrorCode();
	}

	public ServiceValidationException getServiceValidationException() {
		return rootException;
	}

}