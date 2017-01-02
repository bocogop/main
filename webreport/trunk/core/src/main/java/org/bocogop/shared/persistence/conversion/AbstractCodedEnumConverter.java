package org.bocogop.shared.persistence.conversion;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import org.apache.commons.lang3.StringUtils;
import org.bocogop.shared.persistence.userType.CodedEnum;
import org.bocogop.shared.util.TypeUtil;

@Converter
public abstract class AbstractCodedEnumConverter<T extends Enum<?> & CodedEnum>
		implements AttributeConverter<T, String> {

	private Class<T> type;

	@SuppressWarnings("unchecked")
	protected AbstractCodedEnumConverter() {
		type = (Class<T>) TypeUtil.getFirstTypeParameterClass(this);
	}

	@Override
	public String convertToDatabaseColumn(T val) {
		CodedEnum x = (CodedEnum) val;
		return x == null ? null : x.getCode();
	}

	@Override
	public T convertToEntityAttribute(String code) {
		if (StringUtils.isBlank(code))
			return null;

		// Piece of crap Fortify can't handle this apparently.
		// return Arrays.stream(type.getEnumConstants()).filter(x ->
		// x.getCode().equals(code.trim())).findAny()
		// .orElse(null);

		for (T c : type.getEnumConstants()) {
			CodedEnum ce = (CodedEnum) c;
			String code2 = ce.getCode();
			if (code2.equals(code.trim()))
				return c;
		}
		return null;
	}

}
