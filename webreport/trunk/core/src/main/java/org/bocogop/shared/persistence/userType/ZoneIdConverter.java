package org.bocogop.shared.persistence.userType;

import java.time.ZoneId;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import org.apache.commons.lang3.StringUtils;

@Converter(autoApply = true)
public class ZoneIdConverter implements AttributeConverter<ZoneId, String> {

	@Override
	public String convertToDatabaseColumn(ZoneId zoneId) {
		return zoneId == null ? null : zoneId.getId();
	}

	@Override
	public ZoneId convertToEntityAttribute(String value) {
		return StringUtils.isNotBlank(value) ? ZoneId.of(value) : null;
	}
}