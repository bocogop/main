package org.bocogop.wr.web.stationDropdown;

import javax.servlet.http.HttpServletRequest;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
@Aspect
@Profile("attended")
/* This isn't used yet, but was intended to listen for station changes via a dropdown select on the header bar, so we could refresh whatever page we were on intelligently - CPB */
public class StationChangeAdvice {

	public static final String REQUEST_ATTR_STATION_CHANGE_STRATEGY = "stationChangeStrategy";
	public static final String REQUEST_ATTR_STATION_CHANGE_JS_METHOD = "stationChangeJavascriptMethod";

	@Before(value = "@annotation(stationChangeStrategy)", argNames = "stationChangeStrategy")
	public void generateBreadcrumb(StationChangeStrategy stationChangeStrategy) throws Throwable {
		RequestAttributes requestAttr = RequestContextHolder.currentRequestAttributes();
		ServletRequestAttributes pra = (ServletRequestAttributes) requestAttr;
		HttpServletRequest request = pra.getRequest();

		StationChangeStrategyType value = stationChangeStrategy.value();
		request.setAttribute(REQUEST_ATTR_STATION_CHANGE_STRATEGY, value);
		if (value == StationChangeStrategyType.JAVASCRIPT_CALLBACK)
			request.setAttribute(REQUEST_ATTR_STATION_CHANGE_JS_METHOD, stationChangeStrategy.javascriptMethod());
	}
}
