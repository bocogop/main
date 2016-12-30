package org.bocogop.wr.web.conversion;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.bocogop.wr.model.lookup.LookupType;
import org.bocogop.wr.util.TypeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.GenericConverter;
import org.springframework.format.support.FormattingConversionService;

public abstract class AbstractStringToLookupConverter<T extends LookupType> implements GenericConverter {

	@Autowired
	protected FormattingConversionService conversionService;

	private Class<T> type;

	@SuppressWarnings("unchecked")
	protected AbstractStringToLookupConverter() {
		type = (Class<T>) TypeUtil.getFirstTypeParameterClass(this);
	}

	@Override
	public Set<ConvertiblePair> getConvertibleTypes() {
		Set<ConvertiblePair> pairs = new HashSet<>();
		pairs.add(new ConvertiblePair(String.class, type));
		pairs.add(new ConvertiblePair(type, String.class));
		return pairs;
	}

	@Override
	public Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
		if (String.class.equals(sourceType.getObjectType())) {
			String id = (String) source;
			if (StringUtils.isEmpty(id))
				return null;
			long idL = Long.parseLong(id);
			if (idL < 1)
				return null;

			T[] vals = type.getEnumConstants();
			for (T val : vals)
				if (val.getId() == idL)
					return val;

			return null;
		} else if (type.isAssignableFrom(sourceType.getObjectType())) {
			LookupType l = (LookupType) source;
			return l != null ? String.valueOf(l.getId()) : null;
		}

		throw new IllegalArgumentException("Unexpected type of source parameter '" + source + "' - the type "
				+ sourceType.getObjectType().getName() + " is not supported.");
	}

}
