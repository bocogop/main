package org.bocogop.shared.web.ajax;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface AjaxCustomizations {

	/**
	 * By default, the app wraps whatever object the method returns in a map
	 * entry with the key "ajaxResult". Set this to true if you want to override
	 * the default JSON serialization behavior and write the object directly to
	 * the response.
	 * 
	 * @return
	 */
	boolean serializeRawObject() default false;

}