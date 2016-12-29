package org.bocogop.wr.util.cache;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Arrays;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.util.StringUtils;

public class AppKey implements Serializable {
	private static final long serialVersionUID = -4039821726470031824L;

	public static final AppKey EMPTY = new AppKey();

	private final Object target;
	private final Class<?> targetClass;
	private final Method method;
	private final Object[] params;
	private final int hashCode;

	private AppKey() {
		target = null;
		targetClass = null;
		method = null;
		params = null;
		hashCode = getHashCode();
	}

	private int getHashCode() {
		return new HashCodeBuilder().append(target).append(method).appendSuper(Arrays.deepHashCode(this.params))
				.toHashCode();
	}

	/**
	 * Create a new {@link AppKey} instance.
	 * 
	 * @param elements
	 *            the elements of the key
	 */
	public AppKey(Object target, Method method, Object... elements) {
		if (elements == null)
			throw new IllegalArgumentException("Missing required 'elements' parameter");

		this.target = target;
		if (target != null) {
			this.targetClass = AopProxyUtils.ultimateTargetClass(target);
		} else {
			this.targetClass = null;
		}

		this.method = method;
		this.params = new Object[elements.length];
		System.arraycopy(elements, 0, this.params, 0, elements.length);
		this.hashCode = getHashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj instanceof AppKey == false)
			return false;

		AppKey appKey = (AppKey) obj;
		return new EqualsBuilder().append(targetClass, appKey.targetClass).append(method, appKey.method)
				.appendSuper(Arrays.deepEquals(params, appKey.params)).isEquals();
	}

	@Override
	public final int hashCode() {
		return hashCode;
	}

	public final Object getTarget() {
		return target;
	}

	public final Object getTargetClass() {
		return targetClass;
	}

	public final Method getMethod() {
		return method;
	}

	public final Object[] getParams() {
		return params;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + " [target=" + targetClass + ", method=" + method + ", params="
				+ StringUtils.arrayToCommaDelimitedString(this.params) + "]";
	}

}
