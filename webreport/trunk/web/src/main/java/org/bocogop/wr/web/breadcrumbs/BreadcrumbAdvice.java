package org.bocogop.wr.web.breadcrumbs;

import javax.servlet.http.HttpServletRequest;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import org.bocogop.wr.web.interceptor.BreadcrumbsInterceptor;

@Component
@Aspect
@Profile("attended")
public class BreadcrumbAdvice {

	@Before(value = "@annotation(breadcrumb)", argNames = "breadcrumb")
	public void generateBreadcrumb(Breadcrumb breadcrumb) throws Throwable {
		RequestAttributes requestAttr = RequestContextHolder.currentRequestAttributes();

		ServletRequestAttributes pra = (ServletRequestAttributes) requestAttr;
		HttpServletRequest request = pra.getRequest();

		BreadcrumbsInterceptor.setRequestBreadcrumb(request, breadcrumb.value());
	}
}
