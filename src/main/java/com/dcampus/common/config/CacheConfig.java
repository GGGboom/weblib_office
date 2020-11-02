package com.dcampus.common.config;


import net.sf.ehcache.CacheManager;
import org.springframework.cache.ehcache.EhCacheManagerFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

/**
 * 缓存的配置类
 */
@Configuration
public class CacheConfig {

    @Bean(name = "customEhcacheManager")
    public CacheManager cacheManager(EhCacheManagerFactoryBean bean) {
        return bean.getObject();
    }

    @Bean
    public EhCacheManagerFactoryBean ehCacheManagerFactoryBean() {
        EhCacheManagerFactoryBean cacheManagerFactoryBean = new EhCacheManagerFactoryBean();
        cacheManagerFactoryBean.setConfigLocation(new ClassPathResource("ehcache/ehcache-custom.xml"));
        cacheManagerFactoryBean.setShared(true);
        return cacheManagerFactoryBean;
    }
}
