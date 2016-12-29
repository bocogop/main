package org.bocogop.wr.util.cache;

import java.lang.reflect.Method;

import org.springframework.cache.interceptor.KeyGenerator;

/**
 * A customized cache key generator for Spring's caching facilities that
 * considers the class and method name. Spring's default is only to consider the
 * method params.
 * 
 * @author vhaisdbarryc
 */
public class AppCacheKeyGenerator implements KeyGenerator {

	@Override
	public Object generate(Object target, Method method, Object... params) {
		return generateKey(target, method, params);
	}

	/**
	 * Generate a key based on the specified parameters.
	 */
	public static Object generateKey(Object target, Method method, Object... params) {
		return new AppKey(target, method, params);
	}

}
