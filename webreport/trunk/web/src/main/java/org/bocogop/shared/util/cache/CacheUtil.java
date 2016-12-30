package org.bocogop.shared.util.cache;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

public class CacheUtil {

	/*
	 * Vendor-specific implementation. If this reflection is slow, we could
	 * create a registry pattern and map class -> meta info, a'la
	 * ModelAssociationFieldInfoRegistry - CPB
	 */
	public static boolean isReadOnly(Class<?> propertyType) {
		Cache fieldAnnotation = propertyType.getAnnotation(org.hibernate.annotations.Cache.class);
		boolean classIsReadOnly = (fieldAnnotation != null
				&& fieldAnnotation.usage() == CacheConcurrencyStrategy.READ_ONLY);
		return classIsReadOnly;
	}

}