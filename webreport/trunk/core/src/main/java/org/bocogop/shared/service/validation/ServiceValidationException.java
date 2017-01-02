package org.bocogop.shared.service.validation;

import java.io.Serializable;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.bocogop.shared.util.DateUtil;
import org.springframework.context.MessageSourceResolvable;

/**
 * An exception which represents a validation error that occurred in the service
 * layer of the application. This exception also functions as a
 * MessageSourceResolvable, which used by the MessageSource to retrieve the
 * appropriate error message for the user.
 * 
 * @author vhaisdbarryc
 * 	
 */
public class ServiceValidationException extends Exception implements MessageSourceResolvable {
	private static final long serialVersionUID = -3571621035959917290L;

	// ----------------------------------------------- Static Fields

	public static final String RULE_SUFFIX = ".rule";
	public static final String RECOMMENDATION_SUFFIX = ".recommendation";

	// ----------------------------------------------- Fields

	private String errorCode;
	private Serializable[] arguments;

	/*
	 * Below are optional additional fields which may help customize the message
	 * shown to the user:
	 */

	/* The parameter of the service method having the problem */
	private String parameterName;

	/*
	 * Allows the creator to override the TimeZones of all DateTimes stored in
	 * the arguments array, so that when they are used to fill in placeholders
	 * as a MessageSourceResolvable, they are specific to the user's appropriate
	 * TimeZone.
	 */
	private ZoneId timeZoneOverride = null;

	// ----------------------------------------------- Constructors

	public ServiceValidationException(String errorCode) {
		this(null, errorCode, null);
	}

	public ServiceValidationException(String errorCode, Serializable... arguments) {
		this(null, errorCode, arguments);
	}

	public ServiceValidationException(String parameterName, String errorCode, Serializable[] arguments) {
		if (errorCode == null)
			throw new IllegalArgumentException("The errorCode parameter must be non-null");

		this.parameterName = parameterName;
		this.errorCode = errorCode;
		this.arguments = arguments;
	}

	// ----------------------------------------------- Business Methods

	public Serializable[] getArguments() {
		if (arguments == null)
			return arguments;

		Serializable[] newArgs = new Serializable[arguments.length];
		for (int i = 0; i < arguments.length; i++) {
			if (arguments[i] instanceof ZonedDateTime) {
				ZonedDateTime dt = (ZonedDateTime) arguments[i];
				if (timeZoneOverride != null) {
					newArgs[i] = DateUtil.localFormat(timeZoneOverride, dt);
				} else {
					newArgs[i] = DateUtil.localFormat(dt.getZone(), dt);
				}
			} else if (arguments[i] instanceof ZonedDateTime[]) {
				ZonedDateTime[] dt = (ZonedDateTime[]) arguments[i];
				if (dt.length != 2)
					throw new IllegalArgumentException("A " + ZonedDateTime.class.getSimpleName()
							+ " range represented by an array was used as an" + " argument but the length != 2");
				if (timeZoneOverride != null) {
					newArgs[i] = DateUtil.getDateRangeDescription(timeZoneOverride, dt[0], dt[1]);
				} else {
					newArgs[i] = DateUtil.getDateRangeDescription(dt[0].getZone(), dt[0], dt[1]);
				}
			} else {
				newArgs[i] = arguments[i];
			}
		}

		return newArgs;
	}

	@Override
	public String[] getCodes() {
		return new String[] { errorCode };
	}

	@Override
	public String getDefaultMessage() {
		return "Message not found for any codes {" + StringUtils.join(getCodes(), ",") + "}";
	}

	public String getRuleMessageKey() {
		return errorCode + RULE_SUFFIX;
	}

	public String getRecommendationMessageKey() {
		return errorCode + RECOMMENDATION_SUFFIX;
	}

	/**
	 * Initializes the cause of this exception to the specified Throwable by
	 * calling initCause(). This method follows the contract of
	 * Exception.initCause(), which means it can only be called once.
	 */
	public ServiceValidationException withCause(Throwable e) {
		initCause(e);
		return this;
	}

	// ----------------------------------------------- Common Methods

	@Override
	public boolean equals(Object o) {
		if (o instanceof ServiceValidationException == false)
			return false;

		ServiceValidationException e = (ServiceValidationException) o;
		return new EqualsBuilder().append(errorCode, e.errorCode).append(arguments, e.arguments)
				.append(parameterName, e.parameterName).append(timeZoneOverride, e.timeZoneOverride).isEquals();
	}

	// ----------------------------------------------- Accessor Methods

	public String getParameterName() {
		return parameterName;
	}

	public String getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(String errorCode) {
		if (errorCode == null)
			throw new IllegalArgumentException("The errorCode parameter must be non-null");
		this.errorCode = errorCode;
	}

	public ZoneId getTimeZoneOverride() {
		return timeZoneOverride;
	}

	public void setTimeZoneOverride(ZoneId dtz) {
		this.timeZoneOverride = dtz;
	}

}
