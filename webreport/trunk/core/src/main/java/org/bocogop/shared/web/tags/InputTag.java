package org.bocogop.shared.web.tags;

import javax.persistence.Column;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.validation.constraints.Max;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.Length;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.ConfigurablePropertyAccessor;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.validation.AbstractPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.web.servlet.support.BindStatus;
import org.springframework.web.servlet.support.RequestContext;

public class InputTag extends org.springframework.web.servlet.tags.form.InputTag {
	private static final Logger log = LoggerFactory.getLogger(InputTag.class);
	private static final long serialVersionUID = -364728090243513709L;

	/*
	 * Not sure if the tag will try to retrieve this attribute more than once so
	 * caching it just to be on the safe side. Foregoing synchronization since
	 * re-population a second time wouldn't be a huge issue - CPB
	 */
	private boolean attemptedCacheLength = false;
	private String cachedLength = null;

	@Override
	protected String getMaxlength() {
		/* allow an explicit tag attribute to override our introspected value */
		String maxlength = super.getMaxlength();
		if (maxlength != null)
			return maxlength;

//		if (cachedLength != null)
//			return cachedLength;
//
//		if (!attemptedCacheLength)
			try {
				cachedLength = getCachedLength(getBindStatus(), getRequestContext(), pageContext);
			} catch (JspException e) {
				log.error("Encountered an error:", e);
			} finally {
				attemptedCacheLength = true;
			}
		return cachedLength;
	}

	@Override
	protected String getAlt() {
		StringBuffer altBuf = new StringBuffer((super.getAlt() != null) ? super.getAlt() : "");

		try {
			String[] errorMessages = getBindStatus().getErrorMessages();
			for (int i = 0; i < errorMessages.length; i++) {
				String errorMessage = errorMessages[i];
				altBuf.append(getDisplayString(errorMessage));
			}
		} catch (JspException e) {
			log.error("Encountered an error:", e);
		}

		return altBuf.toString();
	}

	public static String getCachedLength(BindStatus status, RequestContext rc, PageContext pageContext)
			throws JspException {
		String path = status.getPath();
		int dotPos = path.indexOf('.');

		if (dotPos != -1) {
			String beanName = path.substring(0, dotPos);
			String expression = path.substring(dotPos + 1);
			Errors errors = rc.getErrors(beanName, false);

			ConfigurablePropertyAccessor propertyAccessor = null;

			if (errors != null) {
				if (expression != null && errors instanceof AbstractPropertyBindingResult) {
					AbstractPropertyBindingResult abr = (AbstractPropertyBindingResult) errors;
					propertyAccessor = abr.getPropertyAccessor();
				}
			} else {
				Object target = null;
				if (rc.getModel() != null) {
					target = rc.getModel().get(beanName);
				} else {
					target = pageContext.getRequest().getAttribute(beanName);
				}

				if (target == null)
					throw new IllegalStateException("Neither BindingResult nor plain target object for bean name '"
							+ beanName + "' available as request attribute");

				if (expression != null && !"*".equals(expression) && !expression.endsWith("*"))
					propertyAccessor = PropertyAccessorFactory.forBeanPropertyAccess(target);
			}

			if (propertyAccessor != null) {
				TypeDescriptor typeDescriptor = propertyAccessor.getPropertyTypeDescriptor(expression);

				TypeDescriptor numberDescriptor = TypeDescriptor.valueOf(Number.class);
				TypeDescriptor charSequenceDescriptor = TypeDescriptor.valueOf(CharSequence.class);

				Long val = null;

				Size sizeAnnotation = typeDescriptor.getAnnotation(Size.class);
				if (val == null && sizeAnnotation != null) {
					val = Long.valueOf(sizeAnnotation.max());
				}

				Max maxAnnotation = typeDescriptor.getAnnotation(Max.class);
				if (val == null && maxAnnotation != null) {
					val = maxAnnotation.value();
				}

				Length len = typeDescriptor.getAnnotation(Length.class);
				if (val == null && len != null) {
					val = Long.valueOf(len.max());
				}
				
				Column col = typeDescriptor.getAnnotation(Column.class);
				if (val == null && col != null && col.length() != 255) {
					val = Long.valueOf(col.length());
				}

				if (val != null) {
					/*
					 * If our @Size or @Max annotations are associated with a
					 * numeric field, we must be doing a range check on the
					 * actual value. So use the number of characters in the
					 * maximum value allowed for this field. CPB
					 */
					if (typeDescriptor.isAssignableTo(numberDescriptor))
						return String.valueOf(String.valueOf(val).length());

					/*
					 * If our @Size or @Max annotations are associated with a
					 * CharSequence (i.e. String) field, we must be directly
					 * specifying the length of this field. So simply return the
					 * same value to use in our maxLength attribute. CPB
					 */
					if (typeDescriptor.isAssignableTo(charSequenceDescriptor))
						return String.valueOf(val);
				}
			}
		}

		return null;
	}
}