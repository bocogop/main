package org.bocogop.wr.persistence.queryCustomization;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.bocogop.wr.persistence.queryCustomization.fieldTypes.ModelAssociationFieldType;
import org.bocogop.wr.util.cache.CacheUtil;

public final class ModelAssociationFieldInfoRegistry {

	private static Map<ModelAssociationFieldType, ModelAssociationFieldInfo> infoMap = Collections
			.synchronizedMap(new HashMap<ModelAssociationFieldType, ModelAssociationFieldInfo>());

	private ModelAssociationFieldInfoRegistry() {
	}

	public static synchronized ModelAssociationFieldInfo getForField(ModelAssociationFieldType type) {
		ModelAssociationFieldInfo info = infoMap.get(type);
		if (info == null) {
			info = populateInfo(type);
			infoMap.put(type, info);
		}
		return info;
	}

	private static ModelAssociationFieldInfo populateInfo(ModelAssociationFieldType type) {
		ModelAssociationFieldInfo info = new ModelAssociationFieldInfo();
		Field field = FieldUtils.getDeclaredField(type.getModelClass(), type.getFieldName(), true);

		Class<?> propertyType = null;

		if (field != null) {
			/*
			 * Don't think we want to remove "join fetch" for fields if the
			 * field itself is read-only..? I think the IDs would be cached but
			 * not the data in each child - CPB
			 */
			// Cache fieldCache = field
			// .getAnnotation(org.hibernate.annotations.Cache.class);
			propertyType = field.getType();
		} else {
			Method getter = MethodUtils.getAccessibleMethod(type.getModelClass(),
					"get" + StringUtils.capitalize(type.getFieldName()));
			if (getter != null) {
				propertyType = getter.getReturnType();
			} else {
				throw new IllegalArgumentException("No field or getter named '" + type.getFieldName()
						+ "' exists in the class " + type.getModelClass().getName());
			}
		}

		if (propertyType != null) {
			info.propertyType = propertyType;
			info.readOnly = CacheUtil.isReadOnly(propertyType);
		}
		return info;
	}

	public static class ModelAssociationFieldInfo {

		public Class<?> propertyType;
		private boolean readOnly;
		private boolean alreadyCached;

		public boolean isReadOnly() {
			return readOnly;
		}

		public void setCached() {
			if (!readOnly)
				throw new UnsupportedOperationException("Can't flag a non-read-only field as having been cached");
			alreadyCached = true;
		}

		public boolean isAlreadyCached() {
			return alreadyCached;
		}

		public Class<?> getPropertyType() {
			return propertyType;
		}

	}

}
