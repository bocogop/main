package org.bocogop.wr.web.conversion;

import java.text.ParseException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAccessor;
import java.util.Locale;

import org.springframework.format.Parser;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.datetime.standard.TemporalAccessorParser;
import org.springframework.stereotype.Component;

import org.bocogop.shared.util.SecurityUtil;

@Component
public class Jsr310DateTimeFormatAnnotationFormatterFactory
		extends org.springframework.format.datetime.standard.Jsr310DateTimeFormatAnnotationFormatterFactory {

	@Override
	public Parser<?> getParser(DateTimeFormat annotation, Class<?> fieldType) {
		final TemporalAccessorParser parser = (TemporalAccessorParser) super.getParser(annotation, fieldType);

		Parser<TemporalAccessor> trimmingParser = new Parser<TemporalAccessor>() {
			@Override
			public TemporalAccessor parse(String text, Locale locale) throws ParseException {
				ZoneId userTimeZone = SecurityUtil.getCurrentUser().getTimeZone();
				TemporalAccessor x = parser.parse(text == null ? null : text.trim(), locale);

				if (x instanceof ZonedDateTime && userTimeZone != null) {
					x = ((ZonedDateTime) x).withZoneSameLocal(userTimeZone);
				}
				return x;
			}
		};

		return trimmingParser;
	}

}
