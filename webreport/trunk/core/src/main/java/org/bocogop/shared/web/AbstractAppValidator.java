package org.bocogop.shared.web;

import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.ValidationException;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.validation.Errors;

public abstract class AbstractAppValidator<T> {
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(AbstractAppValidator.class);

	/**
	 * Validates the specified object - CPB
	 * 
	 * @param mainObject
	 *            The object to validate
	 * @param errors
	 *            The errors which we will use to reject invalid values in the
	 *            specified mainObject
	 * @param jsr303ValidateMainObject
	 *            TODO
	 * @param jsr303ObjectNestedPaths
	 *            Dot-separated paths of objects (relative to the specified
	 *            mainObject) for which we want to perform additional JSR303
	 *            (annotations-based) validations
	 * @throws ValidationException
	 *             If there is an error retrieving the nested jsr303 objects
	 */
	public final void validate(T mainObject, Errors errors, boolean jsr303ValidateMainObject,
			String... jsr303ObjectNestedPaths) throws ValidationException {
		// make sure we are using javax.validation not spring
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		Validator validator = factory.getValidator();

		if (jsr303ValidateMainObject) {
			performJsr303Validation(mainObject, errors, validator);
		}

		for (int i = 0; jsr303ObjectNestedPaths != null && i < jsr303ObjectNestedPaths.length; i++) {
			String nestedPath = jsr303ObjectNestedPaths[i];
			if (StringUtils.isEmpty(nestedPath))
				continue;

			errors.pushNestedPath(nestedPath);

			try {
				Object jsr303Object = PropertyUtils.getNestedProperty(mainObject, nestedPath);
				performJsr303Validation(jsr303Object, errors, validator);
			} catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
				throw new ValidationException(e);
			} finally {
				errors.popNestedPath();
			}
		}

		doExtraValidations(mainObject, errors);
	}

	private void performJsr303Validation(Object jsr303Object, Errors errors, Validator validator) {
		Set<String> fieldSet = new HashSet<>();
		for (Method method : jsr303Object.getClass().getDeclaredMethods()) {
			if (method.getName().startsWith("get")) {
				if (isAnnotationPresent(method)) {
					PropertyDescriptor descriptor = BeanUtils.findPropertyForMethod(method);
					if (descriptor != null)
						fieldSet.add(descriptor.getName());
				}
			}
		}
		for (Field field : jsr303Object.getClass().getDeclaredFields()) {
			if (isAnnotationPresent(field)) {
				fieldSet.add(field.getName());
			}
		}
		for (String fieldName : fieldSet) {
			processField(jsr303Object, errors, fieldName, validator);
		}
	}

	/**
	 * Override this method to perform any custom additional validations on the
	 * main object
	 * 
	 * @param object
	 * @param errors
	 */
	protected void doExtraValidations(T object, Errors errors) {
	}

	private boolean isAnnotationPresent(AnnotatedElement element) {
		Annotation[] annotations = element.getAnnotations();
		if (annotations == null)
			return false;

		for (Annotation annotation : annotations) {
			String annotationCanonicalName = annotation.annotationType().getCanonicalName();
			if (annotationCanonicalName.startsWith("javax.validation"))
				return true;
			if (annotationCanonicalName.startsWith("org.hibernate.validator.constraints"))
				return true;
			if (annotationCanonicalName.startsWith("org.bocogop.wr.model.validation.constraints"))
				return true;
		}

		return false;
	}

	@SuppressWarnings("unchecked")
	private void processField(Object clazz, Errors errors, String fieldName, Validator validator) {
		// use field level validation because of the problem with BEA and class
		// level validation
		PropertyDescriptor descriptor = BeanUtils.getPropertyDescriptor(clazz.getClass(), fieldName);
		Method readMethod = descriptor.getReadMethod();
		if (readMethod != null) {
			Object checkValue = null;
			try {
				checkValue = readMethod.invoke(clazz, (Object[]) null);
			} catch (Exception e) {
				// do nothing value will be null
			}
			Set<ConstraintViolation<T>> violations = validator.validateValue((Class<T>) clazz.getClass(), fieldName,
					checkValue);
			for (ConstraintViolation<T> violation : violations) {
				String propertyPath = violation.getPropertyPath().toString();
				String message = violation.getMessage(); // Add these JSR 303
															// violation
															// messages to
				errors.rejectValue(fieldName, propertyPath, message);
			}
		}
	}

}
