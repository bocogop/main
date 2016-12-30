package org.bocogop.wr.web;

import javax.persistence.OptimisticLockException;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.bocogop.wr.model.AppUser;
import org.bocogop.wr.util.SecurityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.servlet.ModelAndView;

public abstract class AbstractAppController extends AbstractCommonAppController {
	private static final Logger log = LoggerFactory.getLogger(AbstractAppController.class);

	protected AppUser getCurrentUser() {
		return SecurityUtil.getCurrentUserAs(AppUser.class);
	}

	@ExceptionHandler(AccessDeniedException.class)
	public ModelAndView processAuthorizationException(AccessDeniedException ex) {
		return new ModelAndView("authorizationException", "exceptionStackTrace", ExceptionUtils.getFullStackTrace(ex));
	}

	@ExceptionHandler({ OptimisticLockException.class, OptimisticLockingFailureException.class })
	public ModelAndView processOptimisticLockException(OptimisticLockException ex) {
		return new ModelAndView("optimisticLockException", "exceptionStackTrace", ExceptionUtils.getFullStackTrace(ex));
	}

	@InitBinder
	public final void initBinder(WebDataBinder binder) {
		binder.setDisallowedFields(getDisallowedBinderFields());
	}

	protected String[] getDisallowedBinderFields() {
		return new String[] {};
	}

}
