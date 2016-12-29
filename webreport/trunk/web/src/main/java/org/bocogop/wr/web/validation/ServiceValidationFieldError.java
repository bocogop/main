package org.bocogop.wr.web.validation;

import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import org.bocogop.shared.service.validation.ServiceValidationException;

public class ServiceValidationFieldError extends FieldError {
	private static final long serialVersionUID = 486961310693080512L;

	private ServiceValidationException rootException;

	public ServiceValidationFieldError(ServiceValidationException e, BindingResult r) {
		super(r.getObjectName(), e.getParameterName(), r.getRawFieldValue(e.getParameterName()), false, e.getCodes(),
				e.getArguments(), e.getDefaultMessage());
		this.rootException = e;
	}

	public String getOriginalErrorCode() {
		return rootException.getErrorCode();
	}

}