package org.bocogop.wr.config;

import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.ehcache.EhCacheCacheManager;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import org.bocogop.wr.util.cache.AppCacheKeyGenerator;
import net.sf.ehcache.config.ConfigurationFactory;

@Configuration
@EnableCaching(proxyTargetClass = true)
public class CacheConfig extends CachingConfigurerSupport {

	@Autowired
	private Environment env;

	@Bean
	@Override
	public CacheManager cacheManager() {
		EhCacheCacheManager cm = new EhCacheCacheManager();
		net.sf.ehcache.config.Configuration configuration = ConfigurationFactory.parseConfiguration();
		String[] activeProfiles = env.getActiveProfiles();
		configuration.setName(ArrayUtils.isNotEmpty(activeProfiles) ? activeProfiles[0] : "default");
		net.sf.ehcache.CacheManager b = net.sf.ehcache.CacheManager.newInstance(configuration);
		cm.setCacheManager(b);

		return cm;
	}

	@Bean
	@Override
	public KeyGenerator keyGenerator() {
		return new AppCacheKeyGenerator();
	}

}
