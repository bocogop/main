package org.bocogop.wr.web;

import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.bocogop.wr.service.validation.ServiceValidationException;
import org.bocogop.wr.util.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.DateDeserializers.DateDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.DateSerializer;
import com.fasterxml.jackson.datatype.hibernate5.Hibernate5Module;
import com.fasterxml.jackson.datatype.hibernate5.Hibernate5Module.Feature;

@Component
public class CoreAjaxRequestHandler {

	public static final String AJAX_RESULT_MODEL_KEY = "ajaxResult";
	public static final String AJAX_STATUS_MESSAGE_KEY = "statusMessage";
	public static final String AJAX_STATUS_DETAILS_KEY = "statusDetails";
	public static final String AJAX_ERROR_STATUS_KEY = "hasError";

	@Autowired
	protected MessageSource messageSource;
	@Resource(name = "coreAjaxRequestHandler")
	private CoreAjaxRequestHandler selfProxy;

	/**
	 * Utility method which returns true iff the HttpServletRequest was an AJAX
	 * request
	 * 
	 * @param r
	 * @return
	 */
	public static boolean isAjax(HttpServletRequest r) {
		return r.getServletPath().equals("/rest");
	}

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

	private void populateAjaxMap(MessageSourceResolvable e, Map<String, Object> retMap) {
		if (e instanceof ServiceValidationException) {
			ServiceValidationException f = (ServiceValidationException) e;
			f.setTimeZoneOverride(SecurityUtil.getCurrentUser().getTimeZone());

			retMap.put("serviceValidationError", true);
		}

		retMap.put(AJAX_STATUS_MESSAGE_KEY, messageSource.getMessage(e, Locale.getDefault()));
	}

	private void populateResponseForAjaxException(Map<String, Object> retMap, Throwable e) {
		if (e instanceof MessageSourceResolvable) {
			populateAjaxMap((MessageSourceResolvable) e, retMap);
		} else {
			retMap.put(AJAX_STATUS_MESSAGE_KEY, e.getMessage());
			retMap.put(AJAX_STATUS_DETAILS_KEY, ExceptionUtils.getStackTrace(e));
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

			registerModule(module);
		}
	}

}
