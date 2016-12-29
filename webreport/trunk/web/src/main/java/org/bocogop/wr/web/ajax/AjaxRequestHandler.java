package org.bocogop.wr.web.ajax;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.DateDeserializers.DateDeserializer;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.DateSerializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.fasterxml.jackson.datatype.hibernate5.Hibernate5Module;
import com.fasterxml.jackson.datatype.hibernate5.Hibernate5Module.Feature;

import org.bocogop.wr.config.CommonWebConfig;
import org.bocogop.wr.util.DateUtil;
import org.bocogop.wr.util.cache.CacheNames;
import org.bocogop.wr.web.validation.WebValidationService;

@Component
public class AjaxRequestHandler {

	public static final String AJAX_RESULT_MODEL_KEY = "ajaxResult";
	public static final String AJAX_STATUS_MESSAGE_KEY = "statusMessage";
	public static final String AJAX_STATUS_DETAILS_KEY = "statusDetails";
	public static final String AJAX_ERROR_STATUS_KEY = "hasError";

	@Autowired
	protected MessageSource messageSource;
	@Resource(name = "ajaxRequestHandler")
	private AjaxRequestHandler selfProxy;
	@Autowired
	protected WebValidationService webValidationService;

	/**
	 * Utility method which returns true iff the HttpServletRequest was an AJAX
	 * request
	 * 
	 * @param r
	 * @return
	 */
	public static boolean isAjax(HttpServletRequest r) {
		/*
		 * Could use this approach but the line down is probably better since
		 * it's our naming convention and has no client dependencies - CPB
		 */
		// return "XMLHttpRequest".equals(r.getHeader("X-Requested-With"));
		return r.getServletPath().equals(CommonWebConfig.AJAX_CONTEXT_PATH_PREFIX);
	}

	@Cacheable(value = CacheNames.AJAX_VIEWS, key = "#skipAjaxResultJSONWrapper")
	public MappingJackson2JsonView createView(boolean skipAjaxResultJSONWrapper) {
		ObjectMapper om = new CustomJsonObjectMapper();
		MappingJackson2JsonView v = new MappingJackson2JsonView(om);
		v.setExtractValueFromSingleKeyModel(skipAjaxResultJSONWrapper);
		v.setBeanName(AJAX_RESULT_MODEL_KEY);
		// v.setContentType("text/html;charset=UTF-8");
		return v;
	}

	public ModelAndView getExceptionModelAndView(Throwable ex, HttpServletRequest request) {
		if (!isAjax(request))
			throw new IllegalArgumentException(
					"The specified " + HttpServletRequest.class.getSimpleName() + " was not an AJAX request");

		MappingJackson2JsonView jsonView = createView(false);

		Map<String, Object> model = new HashMap<>();
		populateResponseForAjaxException(model, ex);

		ModelAndView mav = new ModelAndView();
		mav.setView(jsonView);
		mav.addObject(AJAX_RESULT_MODEL_KEY, model);
		return mav;
	}

	public Map<String, Object> doBusinessLogic(AjaxBusinessLogic request) {
		Map<String, Object> retMap = new HashMap<>();

		try {
			String message = request.doLogic(retMap);
			if (message != null)
				retMap.put(AJAX_STATUS_MESSAGE_KEY, message);
			retMap.put(AJAX_ERROR_STATUS_KEY, false);
		} catch (Exception e) {
			populateResponseForAjaxException(retMap, e);
		}

		return retMap;
	}

	private void populateResponseForAjaxException(Map<String, Object> retMap, Throwable e) {
		if (e instanceof MessageSourceResolvable) {
			webValidationService.populateAjaxMap((MessageSourceResolvable) e, retMap);
		} else {
			retMap.put(AJAX_STATUS_MESSAGE_KEY, e.getMessage());
			retMap.put(AJAX_STATUS_DETAILS_KEY, ExceptionUtils.getFullStackTrace(e));
		}
		retMap.put(AJAX_ERROR_STATUS_KEY, true);
	}

	public static class CustomJsonObjectMapper extends ObjectMapper {
		private static final long serialVersionUID = 3889851622287778724L;

		/*
		 * Could have instead used
		 * https://github.com/FasterXML/jackson-datatype-jsr310 but this allows
		 * us to standardize on our DateUtil formats and introduces one less
		 * library - CPB
		 */
		public CustomJsonObjectMapper() {
			Hibernate5Module hibernateModule = new Hibernate5Module();
			hibernateModule.enable(Feature.FORCE_LAZY_LOADING);
			hibernateModule.disable(Feature.USE_TRANSIENT_ANNOTATION);
			registerModule(hibernateModule);

			SimpleModule module = new SimpleModule("JSONModule", new Version(2, 0, 0, null, null, null));
			module.addSerializer(Date.class, new DateSerializer(true, null));
			module.addDeserializer(Date.class, new DateDeserializer());

			module.addSerializer(ZonedDateTime.class, new StdSerializer<ZonedDateTime>(ZonedDateTime.class) {
				private static final long serialVersionUID = -3290548677012921823L;

				@Override
				public void serialize(ZonedDateTime value, JsonGenerator jgen, SerializerProvider provider)
						throws IOException, JsonGenerationException {
					jgen.writeNumber(value.toInstant().toEpochMilli());
				}
			});
			module.addDeserializer(ZonedDateTime.class, new StdDeserializer<ZonedDateTime>(ZonedDateTime.class) {
				private static final long serialVersionUID = -3290548677012921823L;

				@Override
				public ZonedDateTime deserialize(JsonParser jp, DeserializationContext ctxt)
						throws IOException, JsonProcessingException {
					return ZonedDateTime.ofInstant(Instant.ofEpochMilli(jp.getNumberValue().longValue()), DateUtil.UTC);
				}
			});

			module.addSerializer(LocalDate.class, new StdSerializer<LocalDate>(LocalDate.class) {
				private static final long serialVersionUID = -805229080847366922L;

				@Override
				public void serialize(LocalDate value, JsonGenerator jgen, SerializerProvider provider)
						throws IOException, JsonGenerationException {
					jgen.writeString(value.format(DateUtil.TWO_DIGIT_DATE_ONLY_FORMAT));
				}
			});
			module.addDeserializer(LocalDate.class, new StdDeserializer<LocalDate>(LocalDate.class) {
				private static final long serialVersionUID = -3290548677012921823L;

				@Override
				public LocalDate deserialize(JsonParser jp, DeserializationContext ctxt)
						throws IOException, JsonProcessingException {
					return LocalDate.parse(jp.getValueAsString(), DateUtil.DATE_ONLY_FORMAT);
				}
			});
			
			module.addSerializer(ZoneId.class, new StdSerializer<ZoneId>(ZoneId.class) {
				private static final long serialVersionUID = -805229080847366922L;

				@Override
				public void serialize(ZoneId value, JsonGenerator jgen, SerializerProvider provider)
						throws IOException, JsonGenerationException {
					jgen.writeString(value.getId());
				}
			});
			module.addDeserializer(ZoneId.class, new StdDeserializer<ZoneId>(ZoneId.class) {
				private static final long serialVersionUID = -3290548677012921823L;

				@Override
				public ZoneId deserialize(JsonParser jp, DeserializationContext ctxt)
						throws IOException, JsonProcessingException {
					return ZoneId.of(jp.getValueAsString());
				}
			});

			registerModule(module);
		}
	}

}
