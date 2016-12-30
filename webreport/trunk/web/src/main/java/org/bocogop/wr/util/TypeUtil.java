package org.bocogop.wr.util;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class TypeUtil {

	public static Class<?> getFirstTypeParameterClass(Object o) {
		return getTypeParameterClass(o, 0);
	}

	public static Class<?> getTypeParameterClass(Object o, int index) {
		Type thisType = o.getClass().getGenericSuperclass();
		final Type rawType;

		if (thisType instanceof ParameterizedType) {
			rawType = ((ParameterizedType) thisType).getActualTypeArguments()[index];
		} else if (thisType instanceof Class) {
			rawType = ((ParameterizedType) ((Class<?>) thisType).getGenericSuperclass())
					.getActualTypeArguments()[index];
		} else {
			throw new IllegalArgumentException("Problem handling type construction for " + o.getClass());
		}

		Class<?> type;
		if (rawType instanceof Class) {
			type = (Class<?>) rawType;
		} else if (rawType instanceof ParameterizedType) {
			type = (Class<?>) ((ParameterizedType) rawType).getRawType();
		} else {
			throw new IllegalArgumentException("Problem determining the class of the generic for " + o.getClass());
		}
		return type;
	}

}
