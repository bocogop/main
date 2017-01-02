package org.bocogop.shared.util.aop;

import java.time.ZonedDateTime;
import java.util.Collection;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ClassUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.reflect.MethodSignature;
import org.bocogop.shared.model.AuditLogEntry;
import org.bocogop.shared.service.audit.AuditLogEntryService;
import org.bocogop.shared.util.SecurityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.base.Joiner;

/* Configured in XML due to the inability to pass in a ${} properties lookup in the @Before value - CPB */
@Component
public class AuditLoggingAspect {
	private static final Logger log = LoggerFactory.getLogger(AuditLoggingAspect.class);

	@Autowired
	private AuditLogEntryService service;

	public void logBefore(JoinPoint joinPoint) {
		Signature s = joinPoint.getSignature();
		if (s instanceof MethodSignature == false) {
			log.error("ERROR: {} joinPoint expression was not a method signature; cannot log this event.",
					AuditLoggingAspect.class.getSimpleName());
			return;
		}

		String valStr;
		try {
			valStr = buildParamString(joinPoint);
		} catch (Exception e) {
			log.error("Error building param string", e);
			return;
		}

		AuditLogEntry ale = new AuditLogEntry();
		ale.setMethod(s.toShortString());

		String userId = SecurityUtil.getCurrentUserName();
		ale.setAppUserId(userId);
		ZonedDateTime now = ZonedDateTime.now();
		ale.setDate(now);
		ale.setParamValues(valStr);
		ale = service.saveOrUpdate(ale);
		log.debug("User {} executed method {} on {} with params {}", ale.getAppUserId(), ale.getMethod(), ale.getDate(),
				ale.getParamValues());
	}

	public String buildParamString(JoinPoint joinPoint) throws Exception {
		MethodSignature signature = (MethodSignature) joinPoint.getSignature();
		String[] parameterNames = signature.getParameterNames();
		Object[] parameterValues = joinPoint.getArgs();

		if (parameterNames == null)
			return null;

		if (parameterValues == null || parameterValues.length != parameterNames.length) {
			/* shouldn't happen, I assume - CPB */
			throw new Exception("There were " + parameterNames.length + " parameter names defined but "
					+ (parameterValues == null ? 0 : parameterValues.length)
					+ " parameter values - unexpected, exiting");
		}

		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < parameterNames.length; i++) {
			String name = parameterNames[i];
			Object val = parameterValues[i];

			if (i > 0)
				sb.append(", ");
			sb.append(name).append("=");
			if (val == null) {
				sb.append("null");
			} else {
				Class<? extends Object> clazz = val.getClass();
				if (ClassUtils.isPrimitiveOrWrapper(clazz)) {
					sb.append(val);
				} else if (clazz.isArray()) {
					sb.append(ArrayUtils.toString(val));
				} else if (Collection.class.isAssignableFrom(clazz)) {
					sb.append("{");
					Joiner.on(", ").appendTo(sb, (Collection<?>) val);
					sb.append("}");
				} else {
					sb.append(String.valueOf(val));
				}
			}
		}

		return sb.toString();
	}
}
