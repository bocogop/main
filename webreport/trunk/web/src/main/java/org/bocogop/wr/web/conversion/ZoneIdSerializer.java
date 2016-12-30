package org.bocogop.wr.web.conversion;

import java.io.IOException;
import java.time.ZoneId;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class ZoneIdSerializer extends JsonSerializer<ZoneId> {

	@Override
	public void serialize(ZoneId value, JsonGenerator jgen, SerializerProvider provider)
			throws IOException, JsonProcessingException {
		jgen.writeStartObject();
		jgen.writeStringField("id", value.getId());
		jgen.writeEndObject();
	}

}
