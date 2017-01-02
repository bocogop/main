package org.bocogop.wr.web.conversion;

import java.io.IOException;

import org.bocogop.shared.model.IdentifiedPersistent;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class IDOnlySerializer<T extends IdentifiedPersistent> extends JsonSerializer<T> {

	@Override
	public void serialize(T value, JsonGenerator jgen, SerializerProvider provider)
			throws IOException, JsonProcessingException {
		jgen.writeStartObject();
		jgen.writeNumberField("id", value.getId());
		serializeExtraAttributes(value, jgen, provider);
		jgen.writeEndObject();
	}

	protected void serializeExtraAttributes(T value, JsonGenerator jgen, SerializerProvider provider)
			throws IOException, JsonProcessingException {
	}

}
