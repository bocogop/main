package org.bocogop.wr.web.conversion;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.bocogop.shared.model.core.IdentifiedPersistent;
import org.bocogop.shared.model.core.Persistent;
import org.bocogop.shared.persistence.AppDAO;
import org.bocogop.shared.util.TypeUtil;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.GenericConverter;
import org.springframework.format.support.FormattingConversionService;

public abstract class AbstractStringToPersistentConverter<T extends Persistent>
		implements GenericConverter, InitializingBean {

	@Autowired
	protected FormattingConversionService conversionService;

	private Class<T> type;
	private AppDAO<T> dao;

	@SuppressWarnings("unchecked")
	protected AbstractStringToPersistentConverter(AppDAO<T> dao) {
		type = (Class<T>) TypeUtil.getFirstTypeParameterClass(this);
		this.dao = dao;
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

			T lookup = dao.findByPrimaryKey(idL);
			if (lookup == null)
				throw new IllegalArgumentException("No " + type.getName() + " could be retrieved with the ID " + id);
			return lookup;
		} else if (type.isAssignableFrom(sourceType.getObjectType())) {
			IdentifiedPersistent l = (IdentifiedPersistent) source;
			return l != null && l.getId() != null ? String.valueOf(l.getId()) : null;
		}

		throw new IllegalArgumentException("Unexpected type of source parameter '" + source + "' - the type "
				+ sourceType.getObjectType().getName() + " is not supported.");
	}

	@Override
	public final void afterPropertiesSet() throws Exception {
		conversionService.addConverter(this);
	}

}
