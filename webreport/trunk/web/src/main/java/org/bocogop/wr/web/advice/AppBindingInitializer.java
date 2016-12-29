package org.bocogop.wr.web.advice;

import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.InitBinder;

/*
 * Advice to all controllers to add this initBinding. For example, all Strings
 * entered as empty will be trimmed and converted to null before persisting in the command objects. CPB
 */
@ControllerAdvice
@Controller
public class AppBindingInitializer {

	@InitBinder
	public void initBinder(WebDataBinder binder) {
		binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
	}

}