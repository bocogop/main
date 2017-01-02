package org.bocogop.shared.web.validation;

/**
 * Generic exception that represent a problem performing validations.
 * 
 * @author barrycon
 * 
 */
public class ValidationException extends Exception {
	private static final long serialVersionUID = -4542032582084941203L;

	public ValidationException() {
	}

	public ValidationException(String message) {
		super(message);
	}

	public ValidationException(Throwable cause) {
		super(cause);
	}

	public ValidationException(String message, Throwable cause) {
		super(message, cause);
	}

}
