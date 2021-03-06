package org.bocogop.shared.model.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;
import javax.validation.constraints.Pattern;

/* The main @Email validator only checks for something@something, this uses Constraint Composition to add on the requirement that it's something@something.something. CPB */
// @Email(message = "Please provide a valid email address")
@Pattern(regexp = ".+@.+\\..+", message = "Please provide a valid email address")
@Target({ ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {})
@Documented
public @interface ExtendedEmailValidator {
	String message() default "Please provide a valid email address";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};
}